@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.serial.jserialcomm

import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_RECEIVED
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortMessageListener
import com.github.rossdanderson.backlight.app.cobsDecode
import com.github.rossdanderson.backlight.app.messages.Message
import kotlinx.coroutines.channels.SendChannel

class MessageListener(
    private val sendChannel: SendChannel<Message>
) : SerialPortMessageListener {
    override fun delimiterIndicatesEndOfMessage(): Boolean = true

    override fun getMessageDelimiter(): ByteArray = ByteArray(0)

    override fun serialEvent(event: SerialPortEvent) {
        val byteArray = event.receivedData
        runCatching { sendChannel.offer(Message.from(byteArray.cobsDecode().toUByteArray())) }
    }

    override fun getListeningEvents(): Int = LISTENING_EVENT_DATA_RECEIVED
}
