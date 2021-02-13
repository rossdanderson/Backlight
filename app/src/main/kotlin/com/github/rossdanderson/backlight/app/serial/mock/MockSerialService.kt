@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.serial.mock

import com.github.rossdanderson.backlight.app.delay
import com.github.rossdanderson.backlight.app.messages.Message
import com.github.rossdanderson.backlight.app.serial.ConnectResult
import com.github.rossdanderson.backlight.app.serial.ConnectResult.Failure
import com.github.rossdanderson.backlight.app.serial.ConnectResult.Success
import com.github.rossdanderson.backlight.app.serial.ConnectionState
import com.github.rossdanderson.backlight.app.serial.ConnectionState.Disconnected
import com.github.rossdanderson.backlight.app.serial.ISerialService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

@OptIn(ExperimentalTime::class)
class MockSerialService : ISerialService {

    private val logger = KotlinLogging.logger {}

    private val connectedPort = AtomicReference<String?>(null)

    private val _connectionState: MutableStateFlow<ConnectionState> = MutableStateFlow(Disconnected)

    override val connectionState: StateFlow<ConnectionState>
        get() = _connectionState

    override val receiveFlow: Flow<Message> = flowOf()

    override val availablePortDescriptorsFlow: Flow<List<String>> =
        flow {
            while (true) {
                emit((1..Random.nextInt(3, 8)).step(Random.nextInt(1, 3)).map { id -> "Port - $id" })
                delay(5000.milliseconds)
            }
        }
            .flowOn(Dispatchers.Default)

    override suspend fun connect(portDescriptor: String): ConnectResult = withContext(Dispatchers.IO) {
        delay(1000.milliseconds)
        val result = when (portDescriptor) {
            "Port - 1" -> {
                logger.warn { "Cannot connect to $portDescriptor - magic port" }
                Failure("Magic port")
            }
            else -> when (val previousConnection = connectedPort.compareAndExchange(null, portDescriptor)) {
                null -> {
                    _connectionState.value = ConnectionState.Connected(portDescriptor, 60)
                    logger.info { "Connected to $portDescriptor" }
                    Success
                }
                else -> {
                    logger.warn { "Cannot connect to $portDescriptor - already connected to $previousConnection" }
                    Failure("Already connected to $previousConnection")
                }
            }
        }
        result
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        when (val previousConnection = connectedPort.getAndSet(null)) {
            null -> logger.warn { "Cannot disconnect - not currently connected" }
            else -> {
                _connectionState.value = Disconnected
                logger.info { "Disconnected from $previousConnection" }
            }
        }
        delay(100.milliseconds)
    }

    override suspend fun send(message: Message) = withContext(Dispatchers.IO) {
        if (connectedPort.get() != null) {
            logger.debug { "Message sent - $message" }
        } else {
            logger.warn { "Message dropped - $message - not currently connected" }
        }
    }
}
