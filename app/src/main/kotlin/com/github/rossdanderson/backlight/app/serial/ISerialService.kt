@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.serial

import com.github.rossdanderson.backlight.app.messages.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ISerialService {

    val connectionState: StateFlow<ConnectionState>

    val availablePortDescriptorsFlow: Flow<List<String>>

    val receiveFlow: Flow<Message>

    suspend fun connect(portDescriptor: String): ConnectResult

    suspend fun disconnect()

    suspend fun send(message: Message)
}
