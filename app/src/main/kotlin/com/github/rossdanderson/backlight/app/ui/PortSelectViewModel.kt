@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.ui

import com.github.rossdanderson.backlight.app.config.Config.Companion.defaultPortLens
import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.serial.ConnectResult
import com.github.rossdanderson.backlight.app.serial.ISerialService
import com.github.rossdanderson.backlight.app.ui.base.BaseViewModel
import com.github.rossdanderson.backlight.app.ui.command.command
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlin.time.ExperimentalTime

@ExperimentalTime
class PortSelectViewModel : BaseViewModel() {

    data class ConnectionFailedAlertEvent(val portDescriptor: String, val reason: String)

    private val serialService by di<ISerialService>()
    private val configService by di<ConfigService>()

    val ports = serialService.availablePortDescriptorsFlow

    private val connectionFailedAlertEventBroadcastChannel = BroadcastChannel<ConnectionFailedAlertEvent>(1)
    val connectionFailedAlertEventFlow = connectionFailedAlertEventBroadcastChannel.asFlow()

    private val closeEventBroadcastChannel = BroadcastChannel<Unit>(1)
    val closeEventFlow = closeEventBroadcastChannel.asFlow()

    val connectCommand = command<String> { portDescriptor ->
        when (val connectResult = serialService.connect(portDescriptor)) {
            is ConnectResult.Success -> {
                closeEventBroadcastChannel.offer(Unit)
                configService.set(defaultPortLens, portDescriptor)
            }
            is ConnectResult.Failure -> connectionFailedAlertEventBroadcastChannel
                .offer(ConnectionFailedAlertEvent(portDescriptor, connectResult.reason))
        }
    }
}
