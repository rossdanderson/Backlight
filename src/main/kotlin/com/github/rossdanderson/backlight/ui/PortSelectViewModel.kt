package com.github.rossdanderson.backlight.ui

import com.github.rossdanderson.backlight.serial.ConnectResult
import com.github.rossdanderson.backlight.serial.ISerialService
import com.github.rossdanderson.backlight.ui.base.BaseViewModel
import com.github.rossdanderson.backlight.ui.command.command
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tornadofx.observable

@FlowPreview
@ExperimentalCoroutinesApi
class PortSelectViewModel : BaseViewModel() {

    sealed class PortSelectEvent {
        object ConnectionFailedAlertEvent : PortSelectEvent()
        object CloseEvent : PortSelectEvent()
    }

    private val serialService by di<ISerialService>()

    private val _ports: SimpleListProperty<String> = SimpleListProperty()
    val ports: ReadOnlyListProperty<String> = _ports

    private val _connecting = SimpleBooleanProperty(false)
    val connecting: ReadOnlyBooleanProperty = _connecting

    lateinit var subscriptionsJob: Job

    val startSubscriptions = command {
        subscriptionsJob = launch { serialService.availablePortDescriptorsFlow.collect { _ports.set(it.observable()) } }
    }

    val stopSubscriptions = command {
        subscriptionsJob.cancel()
    }

    val connectCommand = command<String> {
        when (serialService.connect(it)) {
            ConnectResult.Success -> fire(PortSelectEvent.CloseEvent)
            ConnectResult.Failure -> fire(PortSelectEvent.ConnectionFailedAlertEvent)
        }
    }
}