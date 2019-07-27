package com.github.rossdanderson.backlight.ui

import com.github.rossdanderson.backlight.screensample.ScreenSampleService
import com.github.rossdanderson.backlight.serial.ConnectionState
import com.github.rossdanderson.backlight.serial.ISerialService
import com.github.rossdanderson.backlight.ui.base.BaseViewModel
import com.github.rossdanderson.backlight.ui.command.command
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
@FlowPreview
class MainViewModel : BaseViewModel() {

    private val serialService by di<ISerialService>()
    private val screenSampleService by di<ScreenSampleService>()

    private val _connectionStatus = SimpleStringProperty()
    val connectionStatus: ReadOnlyStringProperty = _connectionStatus

    private val _image: SimpleObjectProperty<Image?> = SimpleObjectProperty(null)
    val image: ObservableValue<Image?> = _image

    private var subscriptionsJob: Job? = null

    val startSubscriptions = command {
        subscriptionsJob = launch {
            launch { screenSampleService.screenFlow.collect { _image.set(SwingFXUtils.toFXImage(it, null)) } }

            launch {
                println("Subscribed")
                serialService.connectionStateFlow.onEach { println(it) }.onCompletion { println("Complete") }.collect {
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
}
