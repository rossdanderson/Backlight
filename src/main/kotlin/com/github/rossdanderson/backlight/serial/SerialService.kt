@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight.serial

import com.fazecast.jSerialComm.SerialPort
import com.github.rossdanderson.backlight.cobsEncode
import com.github.rossdanderson.backlight.messages.*
import com.github.rossdanderson.backlight.serial.ConnectionState.Disconnected
import com.github.rossdanderson.backlight.serial.SerialService.ConnectionActorMessage.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class SerialService(scope: CoroutineScope) : ISerialService {

    private val logger = KotlinLogging.logger {}
    private val lastHeartbeatSent = AtomicLong()
    private val sendHeartbeat = AtomicBoolean()
    private val sendHeartbeatAck = AtomicBoolean()

    sealed class ConnectionActorMessage {
        data class Connect(
            val portDescriptor: String,
            val response: CompletableDeferred<ConnectResult>
        ) : ConnectionActorMessage()

        data class Disconnect(
            val response: CompletableJob
        ) : ConnectionActorMessage()

        data class SendMessage(
            val message: Message
        ) : ConnectionActorMessage()
    }

    private val connectionActor = scope.actor<ConnectionActorMessage>(Dispatchers.IO, BUFFERED) {
        var serialPort: SerialPort? = null

        suspend fun handleConnect(message: Connect) {
            val connectResult = when (val currentSerialPort = serialPort) {
                null -> {
                    logger.info { "Attempting connecting to ${message.portDescriptor}" }

                    connectionStateChannel.send(ConnectionState.Connecting(message.portDescriptor))

                    val attemptSerialPort = SerialPort.getCommPort(message.portDescriptor)

                    val handshakeResponse = if (attemptSerialPort?.openPort(0) == true) {
                        delay(5000)

                        logger.info { "Port opened to ${message.portDescriptor}" }

                        serialPort = attemptSerialPort

                        attemptSerialPort.addDataListener(MessageListener(receiveFlowChannel))

                        val deferredHandshakeResponse =
                            async { receiveFlow.filterIsInstance<HandshakeResponseMessage>().first() }
                        attemptSerialPort.writeMessage(HandshakeRequestMessage)

                        withTimeoutOrNull(1000) { deferredHandshakeResponse.await() }
                    } else null

                    if (handshakeResponse != null) {
                        logger.info { "Connected to ${message.portDescriptor}" }
                        connectionStateChannel.send(
                            ConnectionState.Connected(
                                message.portDescriptor,
                                handshakeResponse.ledCount
                            )
                        )
                        ConnectResult.Success
                    } else {
                        logger.warn { "Cannot connect to ${message.portDescriptor}" }
                        ConnectResult.Failure
                    }
                }
                else -> {
                    logger.warn {
                        "Cannot connect to ${message.portDescriptor} - already connected to ${currentSerialPort.descriptivePortName}"
                    }
                    ConnectResult.Failure
                }
            }
            message.response.complete(connectResult)
        }

        suspend fun handleDisconnect(message: Disconnect) {
            when (val currentSerialPort = serialPort) {
                null -> logger.warn { "Cannot disconnect - not currently connected" }
                else -> {
                    connectionStateChannel.send(Disconnected)
                    currentSerialPort.removeDataListener()
                    currentSerialPort.closePort()
                    logger.info { "Disconnected from ${currentSerialPort.descriptivePortName}" }
                    serialPort = null
                }
            }
            message.response.complete()
        }

        fun handleSendMessage(message: SendMessage) {
            when (val currentSerialPort = serialPort) {
                null -> logger.warn { "Message dropped - ${message.message} - not currently connected" }
                else -> currentSerialPort.writeMessage(message.message)
            }
        }

        while (true) {
            when {
                sendHeartbeat.getAndSet(false) -> serialPort?.writeMessage(HeartbeatMessage)
                sendHeartbeatAck.getAndSet(false) -> serialPort?.writeMessage(HeartbeatAckMessage)
                else -> when (val message = channel.receiveOrNull()) {
                    is Connect -> handleConnect(message)
                    is Disconnect -> handleDisconnect(message)
                    is SendMessage -> handleSendMessage(message)
                }
            }
            yield()
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


    private val receiveFlowChannel = BroadcastChannel<Message>(BUFFERED)
    override val receiveFlow: Flow<Message> = receiveFlowChannel.asFlow()

    override suspend fun send(message: Message) {
        connectionActor.send(SendMessage(message))
    }

    private fun SerialPort.writeMessage(message: Message) {
        val backingArray = message.backingArray
        logger.info { "Writing bytes ${backingArray.contentToString()}" }
        val encoded = backingArray.cobsEncode().toByteArray()
        val bytes = encoded + 0u.toUByte().toByte()
        if (writeBytes(bytes, bytes.size.toLong()) == -1) throw IllegalStateException("Failure to write")
    }
}