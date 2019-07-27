package com.github.rossdanderson.backlight.serial

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_RECEIVED
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortMessageListener
import com.github.rossdanderson.backlight.cobsEncode
import com.github.rossdanderson.backlight.messages.Message
import com.github.rossdanderson.backlight.serial.ConnectResult.Failure
import com.github.rossdanderson.backlight.serial.ConnectResult.Success
import com.github.rossdanderson.backlight.serial.ConnectionState.Connected
import com.github.rossdanderson.backlight.serial.ConnectionState.Disconnected
import com.github.rossdanderson.backlight.serial.SerialService.ConnectionActorMessage.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import java.nio.charset.Charset

@ExperimentalUnsignedTypes
@FlowPreview
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class SerialService(scope: CoroutineScope) : ISerialService {

    private val logger = KotlinLogging.logger {}

    sealed class ConnectionActorMessage {
        data class Connect(
            val portDescriptor: String,
            val response: CompletableDeferred<ConnectResult>
        ) : ConnectionActorMessage()

        data class Disconnect(
            val response: CompletableJob
        ) : ConnectionActorMessage()

        object SendHeartbeat : ConnectionActorMessage()

        data class SendMessage(
            val message: Message,
            val response: CompletableDeferred<SendMessageResult>
        ) : ConnectionActorMessage()
    }

    data class SerialPortConnection(
        val serialPort: SerialPort,
        val receiveJob: Job
    )

    private val connectionActor = scope.actor<ConnectionActorMessage>(Dispatchers.IO, BUFFERED) {
        var serialPortConnection: SerialPortConnection? = null

        channel.consumeEach { message ->
            when (message) {
                is Connect -> {
                    val result = when (val currentSerialPortConnection = serialPortConnection) {
                        null -> {
                            logger.info { "Attempting connecting to ${message.portDescriptor}" }

                            val attemptSerialPort = SerialPort.getCommPort(message.portDescriptor)

                            val result = attemptSerialPort
                                ?.openPort(0)
                                ?.let { connected ->
                                    // TODO perform handshake
                                    if (connected) Success else Failure
                                } ?: Failure

                            if (result is Success) {
                                logger.info { "Connected to ${message.portDescriptor}" }
                                serialPortConnection = SerialPortConnection(
                                    attemptSerialPort,
                                    launch { attemptSerialPort.receiveFlow.collect { receiveFlowChannel.offer(it) } }
                                )

                                connectionStateChannel.send(Connected(message.portDescriptor))
                            } else {
                                logger.warn { "Cannot connect to ${message.portDescriptor}" }
                            }
                            result
                        }
                        else -> {
                            val serialPort = currentSerialPortConnection.serialPort
                            logger.warn { "Cannot connect to ${message.portDescriptor} - already connected to ${serialPort.descriptivePortName}" }
                            Failure
                        }
                    }
                    message.response.complete(result)
                }
                is Disconnect -> {
                    when (val currentSerialPortConnection = serialPortConnection) {
                        null -> logger.warn { "Cannot disconnect - not currently connected" }
                        else -> {
                            currentSerialPortConnection.receiveJob.cancel()
                            val serialPort = currentSerialPortConnection.serialPort
                            serialPort.closePort()
                            logger.info { "Disconnected from ${serialPort.descriptivePortName}" }
                            serialPortConnection = null
                        }
                    }
                    message.response.complete()
                }
                SendHeartbeat -> TODO()
                is SendMessage -> when (val currentSerialPortConnection = serialPortConnection) {
                    null -> logger.warn { "Message dropped - ${message.message} - not currently connected" }
                    else -> currentSerialPortConnection.serialPort.writeMessage(message.message)
                }
            }
        }
    }

    override val availablePortDescriptorsFlow: Flow<List<String>> =
        flow {
            while (true) {
                emit(Signal)
                delay(1000)
            }
        }
            .map { SerialPort.getCommPorts().map { it.descriptivePortName } }
            .conflate()
            .flowOn(Dispatchers.IO)
            .onEach { logger.info { "Port refresh discovered: $it" } }
            .distinctUntilChanged()

    override suspend fun connect(portDescriptor: String): ConnectResult =
        CompletableDeferred<ConnectResult>().also { connectionActor.send(Connect(portDescriptor, it)) }.await()

    override suspend fun disconnect() {
        Job().also { connectionActor.send(Disconnect(it)) }.join()
    }

    private val connectionStateChannel = ConflatedBroadcastChannel<ConnectionState>(Disconnected)
    override val connectionStateFlow: Flow<ConnectionState> = connectionStateChannel.asFlow().distinctUntilChanged()

    private val receiveFlowChannel = BroadcastChannel<ReceiveMessage>(BUFFERED)
    override val receiveFlow: Flow<ReceiveMessage> = receiveFlowChannel.asFlow()

    override suspend fun send(message: Message): SendMessageResult =
        CompletableDeferred<SendMessageResult>().also { connectionActor.send(SendMessage(message, it)) }.await()

    private fun SerialPort.writeMessage(message: Message) {
        val backingArray = message.backingArray
        logger.info { "Writing bytes ${backingArray.contentToString()}" }
        val encoded = backingArray.cobsEncode().toByteArray()
        val bytes = encoded + 0u.toUByte().toByte()
        if (writeBytes(bytes, bytes.size.toLong()) == -1) throw IllegalStateException("Failure to write")
    }

    private val SerialPort.receiveFlow: Flow<ReceiveMessage>
        get() = callbackFlow<SerialPortEvent> {
            val success = addDataListener(
                object : SerialPortMessageListener {
                    override fun delimiterIndicatesEndOfMessage(): Boolean = true

                    override fun getMessageDelimiter(): ByteArray = "\n".toByteArray(Charset.forName("ASCII"))

                    override fun serialEvent(event: SerialPortEvent) {
                        offer(event)
                    }

                    override fun getListeningEvents(): Int = LISTENING_EVENT_DATA_RECEIVED
                }
            )
            awaitClose { removeDataListener() }

            if (!success) close()
        }
            .buffer()
            .map { it.toString() }
}