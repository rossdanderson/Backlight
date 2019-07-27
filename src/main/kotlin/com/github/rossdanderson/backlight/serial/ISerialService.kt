package com.github.rossdanderson.backlight.serial

import com.github.rossdanderson.backlight.messages.Message
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

typealias ReceiveMessage = String

@ExperimentalCoroutinesApi
interface ISerialService {

    val connectionStateFlow: Flow<ConnectionState>

    val availablePortDescriptorsFlow: Flow<List<String>>

    val receiveFlow: Flow<ReceiveMessage>

    suspend fun connect(portDescriptor: String): ConnectResult

    suspend fun disconnect()

    suspend fun send(message: Message): SendMessageResult
}