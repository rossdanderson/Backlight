@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight.app.serial.jserialcomm

import com.fazecast.jSerialComm.SerialPort
import com.github.rossdanderson.backlight.app.cobsEncode
import com.github.rossdanderson.backlight.app.messages.*
import com.github.rossdanderson.backlight.app.serial.ConnectResult
import com.github.rossdanderson.backlight.app.serial.ConnectionState
import com.github.rossdanderson.backlight.app.serial.ConnectionState.*
import com.github.rossdanderson.backlight.app.serial.ISerialService
import com.github.rossdanderson.backlight.app.serial.jserialcomm.JSerialCommService.ConnectionActorMessage.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class JSerialCommService(scope: CoroutineScope) : ISerialService {

    private val logger = KotlinLogging.logger {}
    private val lastHeartbeatSent = AtomicLong()
    private val sendHeartbeat = AtomicBoolean()
    private val sendHeartbeatAck = AtomicBoolean()

    sealed class ConnectionActorMessage {
        data class Connect(
            val descriptivePortName: String,
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
                    logger.info { "Attempting connecting to ${message.descriptivePortName}" }

                    connectionStateChannel.send(Connecting(message.descriptivePortName))

                    val attemptSerialPort =
                        SerialPort.getCommPorts().singleOrNull { it.descriptivePortName == message.descriptivePortName }
                            ?.apply {
                                baudRate = 115200
                                setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 1000, 1000)
                            }

                    if (attemptSerialPort != null) {
                        if (attemptSerialPort.openPort()) {
                            delay(5000)

                            logger.info { "Port opened to ${message.descriptivePortName}" }

                            attemptSerialPort.addDataListener(MessageListener(receiveFlowChannel))

                            val deferredHandshakeResponse =
                                async { receiveFlow.filterIsInstance<HandshakeResponseMessage>().first() }
                            attemptSerialPort.writeMessage(HandshakeRequestMessage)

                            val handshakeResponse = withTimeoutOrNull(1000) { deferredHandshakeResponse.await() }

                            if (handshakeResponse != null) {
                                logger.info { "Connected to ${message.descriptivePortName}" }
                                connectionStateChannel.send(
                                    Connected(
                                        message.descriptivePortName,
                                        handshakeResponse.ledCount
                                    )
                                )

                                serialPort = attemptSerialPort

                                ConnectResult.Success
                            } else {
                                logger.warn { "Cannot connect to ${message.descriptivePortName} - handshake failed" }
                                connectionStateChannel.send(Disconnected)
                                ConnectResult.Failure("Handshake failed")
                            }
                        } else {
                            logger.warn { "Cannot connect to ${message.descriptivePortName} - unable to open port" }
                            connectionStateChannel.send(Disconnected)
                            ConnectResult.Failure("Unable to open port")
                        }
                    } else {
                        logger.warn { "Cannot connect to ${message.descriptivePortName} - unable to find port" }
                        connectionStateChannel.send(Disconnected)
                        ConnectResult.Failure("Unable to find port")
                    }
                }
                else -> {
                    logger.warn {
                        "Cannot connect to ${message.descriptivePortName} - already connected to ${currentSerialPort.descriptivePortName}"
                    }
                    ConnectResult.Failure("Already connected to ${currentSerialPort.descriptivePortName}")
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
                emit(Unit)
                delay(5000)
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
        logger.info { "Encoding and writing bytes ${backingArray.contentToString()}" }
        val encoded = backingArray.cobsEncode().toByteArray()
        val bytes = encoded + 0u.toUByte().toByte()
        if (writeBytes(bytes, bytes.size.toLong()) == -1) throw IllegalStateException("Failure to write")
    }
}
