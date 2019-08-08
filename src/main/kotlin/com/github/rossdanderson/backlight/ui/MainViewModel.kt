package com.github.rossdanderson.backlight.ui

import com.github.rossdanderson.backlight.config.Config
import com.github.rossdanderson.backlight.config.ConfigService
import com.github.rossdanderson.backlight.config.saturationAlpha
import com.github.rossdanderson.backlight.data.UColor
import com.github.rossdanderson.backlight.screensample.ScreenSampleService
import com.github.rossdanderson.backlight.serial.ConnectionState
import com.github.rossdanderson.backlight.serial.ISerialService
import com.github.rossdanderson.backlight.ui.MainViewModel.MainEvent.LEDUpdateEvent
import com.github.rossdanderson.backlight.ui.MainViewModel.MainEvent.ShowPortSelectModalEvent
import com.github.rossdanderson.backlight.ui.base.BaseViewModel
import com.github.rossdanderson.backlight.ui.command.command
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableDoubleValue
import javafx.beans.value.ObservableStringValue
import javafx.beans.value.ObservableValue
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
@FlowPreview
class MainViewModel : BaseViewModel() {

    sealed class MainEvent {
        object ShowPortSelectModalEvent : MainEvent()
        data class LEDUpdateEvent(val colors: List<UColor>) : MainEvent()
    }

    private val configService by di<ConfigService>()
    private val serialService by di<ISerialService>()
    private val screenSampleService by di<ScreenSampleService>()

    private val _connectionStatus = SimpleStringProperty()
    val connectionStatus: ObservableStringValue = _connectionStatus

    private val _image: SimpleObjectProperty<Image?> = SimpleObjectProperty(null)
    val image: ObservableValue<Image?> = _image

    private val _saturation = SimpleDoubleProperty()
    val saturation: ObservableDoubleValue = _saturation

    private var subscriptionsJob: Job? = null

    val startSubscriptions = command {
        subscriptionsJob = launch {
            launch {
                configService.configFlow
                    .map { it.defaultPort }
                    .distinctUntilChanged()
                    .filter { it == null }
                    .collect { eventBus.fire(ShowPortSelectModalEvent) }
            }

            launch {
                screenSampleService.ledColorFlow
                    .collect { eventBus.fire(LEDUpdateEvent(it)) }
            }

            launch { screenSampleService.screenFlow.collect { _image.set(SwingFXUtils.toFXImage(it, null)) } }

            launch {
                serialService.connectionStateFlow.collect {
                    when (it) {
                        is ConnectionState.Connected -> _connectionStatus.set("Connected to ${it.portDescriptor}")
                        is ConnectionState.Disconnected -> _connectionStatus.set("Disconnected")
                    }
                }
            }
        }
    }

    val stopSubscriptions = command {
        subscriptionsJob?.cancel()
    }

    val updateSaturation = command<Double> { saturationAlpha ->
        configService.set(Config.saturationAlpha.asSetter(), saturationAlpha)
    }
}
