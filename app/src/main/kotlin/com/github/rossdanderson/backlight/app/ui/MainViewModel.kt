@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.ui

import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.led.LEDService
import com.github.rossdanderson.backlight.app.ui.MainViewModel.MainEvent.ShowPortSelectModalEvent
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime

@ExperimentalTime
class MainViewModel(
    configService: ConfigService,
    ledService: LEDService,
) {

    sealed class MainEvent {
        object ShowPortSelectModalEvent : MainEvent()
    }

    val showPortSelectEventFlow = configService.configFlow
        .map { it.defaultPort }
        .distinctUntilChanged()
        .filter { it == null }
        .map { ShowPortSelectModalEvent }

    val ledColorsFlow = ledService.ledColorsFlow
}
