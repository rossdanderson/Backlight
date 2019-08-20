@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.ui

import com.github.rossdanderson.backlight.config.ConfigService
import com.github.rossdanderson.backlight.led.LEDService
import com.github.rossdanderson.backlight.screen.IScreenService
import com.github.rossdanderson.backlight.ui.MainViewModel.MainEvent.ShowPortSelectModalEvent
import com.github.rossdanderson.backlight.ui.base.BaseViewModel
import javafx.embed.swing.SwingFXUtils
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class MainViewModel : BaseViewModel() {

    sealed class MainEvent {
        object ShowPortSelectModalEvent : MainEvent()
    }

    private val configService by di<ConfigService>()
    private val ledService by di<LEDService>()
    private val screenService by di<IScreenService>()

    val showPortSelectEventFlow = configService.configFlow
        .map { it.defaultPort }
        .distinctUntilChanged()
        .filter { it == null }
        .map { ShowPortSelectModalEvent }

    val ledColorsFlow = ledService.ledColorsFlow

    val imageFlow = screenService.screenFlow
        .map { SwingFXUtils.toFXImage(it, null) }
}