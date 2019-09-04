@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.serial

import com.github.rossdanderson.backlight.app.messages.Message
import kotlinx.coroutines.flow.Flow

interface ISerialService {

    val connectionStateFlow: Flow<ConnectionState>

    val availablePortDescriptorsFlow: Flow<List<String>>

    val receiveFlow: Flow<Message>

    suspend fun connect(portDescriptor: String): ConnectResult

    suspend fun disconnect()

    suspend fun send(message: Message)
}
