@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.serial.jserialcomm

import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_RECEIVED
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortMessageListener
import com.github.rossdanderson.backlight.app.cobsDecode
import com.github.rossdanderson.backlight.app.messages.Message
import kotlinx.coroutines.channels.SendChannel
import java.nio.charset.Charset

class MessageListener(
    private val sendChannel: SendChannel<Message>
) : SerialPortMessageListener {
    override fun delimiterIndicatesEndOfMessage(): Boolean = true

    override fun getMessageDelimiter(): ByteArray =
        "\n".toByteArray(Charset.forName("ASCII"))

    override fun serialEvent(event: SerialPortEvent) {
        val uByteArray = event.receivedData.toUByteArray()
        println(uByteArray.contentToString())
        println(uByteArray.cobsDecode().contentToString())
        sendChannel.offer(Message.from(uByteArray.cobsDecode()))
    }

    override fun getListeningEvents(): Int = LISTENING_EVENT_DATA_RECEIVED
}
