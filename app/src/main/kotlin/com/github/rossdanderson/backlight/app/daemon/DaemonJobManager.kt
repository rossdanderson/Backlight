@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.daemon

import com.github.rossdanderson.backlight.app.config.Config
import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.data.UColor
import com.github.rossdanderson.backlight.app.led.LEDService
import com.github.rossdanderson.backlight.app.messages.WriteAllMessage
import com.github.rossdanderson.backlight.app.serial.ConnectResult
import com.github.rossdanderson.backlight.app.serial.ConnectionState
import com.github.rossdanderson.backlight.app.serial.ISerialService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@ExperimentalTime
class DaemonJobManager(
    private val ledService: LEDService,
    private val configService: ConfigService,
    private val serialService: ISerialService,
    daemonScope: CoroutineScope
) : CoroutineScope by daemonScope {

    init {
        // Bind the led flow to serial out
        launch {
            serialService.connectionStateFlow
                .transformLatest<ConnectionState, List<UColor>> {
                    when (it) {
                        is ConnectionState.Connected -> emitAll(ledService.ledColorsFlow)
                    }
                }
                .collect { serialService.send(WriteAllMessage(it)) }
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
