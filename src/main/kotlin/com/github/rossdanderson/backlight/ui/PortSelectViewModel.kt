package com.github.rossdanderson.backlight.ui

import com.github.rossdanderson.backlight.config.Config.Companion.defaultPortLens
import com.github.rossdanderson.backlight.config.ConfigService
import com.github.rossdanderson.backlight.serial.ConnectResult
import com.github.rossdanderson.backlight.serial.ISerialService
import com.github.rossdanderson.backlight.ui.base.BaseViewModel
import com.github.rossdanderson.backlight.ui.command.command
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow

@FlowPreview
@ExperimentalCoroutinesApi
class PortSelectViewModel : BaseViewModel() {

    data class ConnectionFailedAlertEvent(val portDescriptor: String)

    private val serialService by di<ISerialService>()
    private val configService by di<ConfigService>()

    val ports = serialService.availablePortDescriptorsFlow

    private val connectionFailedAlertEventBroadcastChannel = BroadcastChannel<ConnectionFailedAlertEvent>(1)
    val connectionFailedAlertEventFlow = connectionFailedAlertEventBroadcastChannel.asFlow()

    private val closeEventBroadcastChannel = BroadcastChannel<Unit>(1)
    val closeEventFlow = closeEventBroadcastChannel.asFlow()

    val connectCommand = command<String> { portDescriptor ->
        when (serialService.connect(portDescriptor)) {
            ConnectResult.Success -> {
                closeEventBroadcastChannel.offer(Unit)
                configService.set(defaultPortLens, portDescriptor)
            }
            ConnectResult.Failure -> connectionFailedAlertEventBroadcastChannel
                .offer(ConnectionFailedAlertEvent(portDescriptor))
        }
    }
}