@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.serial

import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_RECEIVED
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortMessageListener
import com.github.rossdanderson.backlight.messages.Message
import kotlinx.coroutines.channels.SendChannel
import java.nio.charset.Charset

class MessageListener(
    private val sendChannel: SendChannel<Message>
) : SerialPortMessageListener {
    override fun delimiterIndicatesEndOfMessage(): Boolean = true

    override fun getMessageDelimiter(): ByteArray =
        "\n".toByteArray(Charset.forName("ASCII"))

    override fun serialEvent(event: SerialPortEvent) {
        sendChannel.offer(Message.from(event.receivedData.toUByteArray()))
    }

    override fun getListeningEvents(): Int = LISTENING_EVENT_DATA_RECEIVED
}