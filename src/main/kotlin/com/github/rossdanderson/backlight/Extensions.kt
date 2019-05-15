@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortMessageListener
import java.nio.charset.Charset

fun SerialPort.addDataListener(
    types: Int,
    function: (SerialPortEvent) -> Unit
) {
    addDataListener(
        object : SerialPortMessageListener {
            override fun delimiterIndicatesEndOfMessage(): Boolean = true

            override fun getMessageDelimiter(): ByteArray = "\n".toByteArray(Charset.forName("ASCII"))

            override fun serialEvent(event: SerialPortEvent) {
                function(event)
            }

            override fun getListeningEvents(): Int = types
        }
    )
}

fun UByteArray.cobsEncode(): UByteArray {
    var readIndex = 0
    var writeIndex = 1
    var codeIndex = 0
    var code: UByte = 1u

    val encodedBufferSize = size + size / 254 + 1
    val encodedBuffer = UByteArray(encodedBufferSize)

    while (readIndex < size) {
        if (get(readIndex) == 0.toUByte()) {
            encodedBuffer[codeIndex] = code
            code = 1u
            codeIndex = writeIndex++
            readIndex++
        } else {
            encodedBuffer[writeIndex++] = get(readIndex++)
            code++

            if (code == 0xFFu.toUByte()) {
                encodedBuffer[codeIndex] = code
                code = 1u
                codeIndex = writeIndex++
            }
        }
    }

    encodedBuffer[codeIndex] = code

    return encodedBuffer
}