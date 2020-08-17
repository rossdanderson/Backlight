@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.daemon

import com.github.rossdanderson.backlight.app.config.Config
import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.data.LEDColors
import com.github.rossdanderson.backlight.app.led.LEDService
import com.github.rossdanderson.backlight.app.messages.Message
import com.github.rossdanderson.backlight.app.messages.PrintMessage
import com.github.rossdanderson.backlight.app.messages.SetBrightnessMessage
import com.github.rossdanderson.backlight.app.messages.WriteAllMessage
import com.github.rossdanderson.backlight.app.serial.ConnectResult
import com.github.rossdanderson.backlight.app.serial.ConnectionState
import com.github.rossdanderson.backlight.app.serial.ISerialService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.time.ExperimentalTime

@ExperimentalTime
class DaemonJobManager(
    private val ledService: LEDService,
    private val configService: ConfigService,
    private val serialService: ISerialService,
    daemonScope: CoroutineScope
) : CoroutineScope by daemonScope {

    private val logger = KotlinLogging.logger {}

    init {
        // Bind the led flow to serial out
        launch {
            serialService.connectionState
                .flatMapLatest {
                    if (it is ConnectionState.Connected) ledService.ledColorsFlow.map<LEDColors, Message> {
                        WriteAllMessage(
                            it.colors
                        )
                    }.onStart { emit(SetBrightnessMessage((configService.configFlow.value.brightness / 10 * 255).toInt().toUByte())) } else emptyFlow()
                }
                .conflate()
                .collect {
                    serialService.send(it)
                }
        }

        // Bind the led flow to serial out
        launch {
            configService.configFlow.map { it.brightness }
                .collect { serialService.send(SetBrightnessMessage((it / 10 * 255).toInt().toUByte())) }
        }

        // Log print messages from the serial connection
        launch {
            serialService.receiveFlow
                .filterIsInstance<PrintMessage>()
                .collect { logger.info { it.contents } }
        }

        // On startup, attempt to connect to the last known port
        launch {
            configService.configFlow.map { it.defaultPort }.first()?.let {
                when (serialService.connect(it)) {
                    is ConnectResult.Failure -> configService.set(Config.defaultPortLens, null)
                }
            }
        }
    }
}
