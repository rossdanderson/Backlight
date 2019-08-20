package com.github.rossdanderson.backlight.ui

import com.github.rossdanderson.backlight.config.ConfigService
import com.github.rossdanderson.backlight.screen.ScreenService
import com.github.rossdanderson.backlight.serial.ISerialService
import com.github.rossdanderson.backlight.ui.MainViewModel.MainEvent.ShowPortSelectModalEvent
import com.github.rossdanderson.backlight.ui.base.BaseViewModel
import javafx.embed.swing.SwingFXUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import mu.KotlinLogging

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
@FlowPreview
class MainViewModel : BaseViewModel() {

    sealed class MainEvent {
        object ShowPortSelectModalEvent : MainEvent()
    }

    private val logger = KotlinLogging.logger { }
    private val configService by di<ConfigService>()
    private val serialService by di<ISerialService>()
    private val screenSampleService by di<ScreenService>()

    val showPortSelectEventFlow = configService.configFlow
        .map { it.defaultPort }
        .distinctUntilChanged()
        .filter { it == null }
        .map { ShowPortSelectModalEvent }

    val ledColorsFlow = screenSampleService.ledColorsFlow

    val imageFlow = screenSampleService.screenFlow
        .map { SwingFXUtils.toFXImage(it, null) }
}