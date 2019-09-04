@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.ui

import com.github.rossdanderson.backlight.app.serial.ConnectionState.*
import com.github.rossdanderson.backlight.app.serial.ISerialService
import com.github.rossdanderson.backlight.app.ui.base.BaseViewModel
import kotlinx.coroutines.flow.map

class FooterViewModel : BaseViewModel() {

    private val serialService by di<ISerialService>()

    val connectionStatusFlow = serialService.connectionStateFlow.map {
        when (it) {
            is Connected -> "Connected to ${it.portDescriptor}"
            is Connecting -> "Connecting to ${it.portDescriptor}..."
            is Disconnected -> "Disconnected"
        }
    }
}
