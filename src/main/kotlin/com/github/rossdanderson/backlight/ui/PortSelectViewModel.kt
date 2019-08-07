package com.github.rossdanderson.backlight.ui

import com.github.rossdanderson.backlight.serial.ConnectResult
import com.github.rossdanderson.backlight.serial.ISerialService
import com.github.rossdanderson.backlight.ui.PortSelectViewModel.PortSelectEvent.CloseEvent
import com.github.rossdanderson.backlight.ui.PortSelectViewModel.PortSelectEvent.ConnectionFailedAlertEvent
import com.github.rossdanderson.backlight.ui.base.BaseViewModel
import com.github.rossdanderson.backlight.ui.command.command
import javafx.beans.property.ReadOnlyListProperty
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
        data class ConnectionFailedAlertEvent(val portDescriptor: String) : PortSelectEvent()
        object CloseEvent : PortSelectEvent()
    }

    private val serialService by di<ISerialService>()

    private val _ports: SimpleListProperty<String> = SimpleListProperty()
    val ports: ReadOnlyListProperty<String> = _ports

    lateinit var subscriptionsJob: Job

    val startSubscriptions = command {
        subscriptionsJob = launch { serialService.availablePortDescriptorsFlow.collect { _ports.set(it.observable()) } }
    }

    val stopSubscriptions = command {
        subscriptionsJob.cancel()
    }

    val connectCommand = command<String> {
        when (serialService.connect(it)) {
            ConnectResult.Success -> eventBus.fire(CloseEvent)
            ConnectResult.Failure -> eventBus.fire(ConnectionFailedAlertEvent(it))
        }
    }
}