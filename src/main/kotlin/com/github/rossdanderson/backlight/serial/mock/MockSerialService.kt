package com.github.rossdanderson.backlight.serial.mock

import com.github.rossdanderson.backlight.messages.Message
import com.github.rossdanderson.backlight.serial.*
import com.github.rossdanderson.backlight.serial.ConnectResult.Failure
import com.github.rossdanderson.backlight.serial.ConnectResult.Success
import com.github.rossdanderson.backlight.serial.ConnectionState.Disconnected
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

@ExperimentalCoroutinesApi
@FlowPreview
class MockSerialService : ISerialService {

    private val logger = KotlinLogging.logger {}

    private val connectedPort = AtomicReference<String?>(null)

    private val connectionStateChannel: ConflatedBroadcastChannel<ConnectionState> =
        ConflatedBroadcastChannel(Disconnected)

    override val connectionStateFlow: Flow<ConnectionState> = connectionStateChannel.asFlow()

    override val receiveFlow: Flow<ReceiveMessage> = flowOf()

    override val availablePortDescriptorsFlow: Flow<List<String>> =
        flow {
            while (true) {
                emit((1..Random.nextInt(3, 8)).step(Random.nextInt(1, 3)).map { id -> "Port - $id" })
                delay(5000)
            }
        }
            .flowOn(Dispatchers.Default)

    override suspend fun connect(portDescriptor: String): ConnectResult = withContext(Dispatchers.IO) {
        delay(1000)
        val result = when (portDescriptor) {
            "Port - 1" -> {
                logger.warn { "Cannot connect to $portDescriptor - magic port" }
                Failure
            }
            else -> when (val previousConnection = connectedPort.compareAndExchange(null, portDescriptor)) {
                null -> {
                    connectionStateChannel.send(ConnectionState.Connected(portDescriptor))
                    logger.info { "Connected to $portDescriptor" }
                    Success
                }
                else -> {
                    logger.warn { "Cannot connect to $portDescriptor - already connected to $previousConnection" }
                    Failure
                }
            }
        }
        result
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        when (val previousConnection = connectedPort.getAndSet(null)) {
            null -> logger.warn { "Cannot disconnect - not currently connected" }
            else -> {
                connectionStateChannel.send(Disconnected)
                logger.info { "Disconnected from $previousConnection" }
            }
        }
        delay(100)
    }

    override suspend fun send(message: Message): SendMessageResult = withContext(Dispatchers.IO) {
        if (connectedPort.get() != null) {
            logger.warn { "Message sent - $message" }
            SendMessageResult.Success
        } else {
            logger.warn { "Message dropped - $message - not currently connected" }
            SendMessageResult.Failure("Not currently connected")
        }
    }
}