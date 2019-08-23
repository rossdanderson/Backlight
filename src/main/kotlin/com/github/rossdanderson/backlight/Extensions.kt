@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortMessageListener
import java.awt.Color
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

private const val alpha = 1.5
private const val contrast = 7.0
private const val contrastFactor: Double = (259.0 * (contrast + 255.0)) / (255.0 * (259.0 - contrast))

fun Int.applySaturation(greyscaleLuminosity: Double): Int =
    maxOf(0, minOf(255, (alpha * this + (1.0 - alpha) * greyscaleLuminosity).toInt()))

fun Int.applyContrast(): Int =
    maxOf(0, minOf((contrastFactor * (this - 128) + 128).toInt(), 255))

fun Color.greyscaleLuminosity() = red * 0.299 + green * 0.587 + blue * 0.114

fun ByteArray.cobsEncode(
    size: Int = this.size
): ByteArray {
    var readIndex = 0
    var writeIndex = 1
    var codeIndex = 0
    var code = 1

    val encodedBuffer = ByteArray(getEncodedBufferSize(size))

    while (readIndex < size) {
        if ((this[readIndex].toInt() and 0xFF) == 0) {
            encodedBuffer[codeIndex] = code.toByte()
            code = 1
            codeIndex = writeIndex++
            readIndex++
        } else {
            encodedBuffer[writeIndex++] = this[readIndex++]
            code++

            if (code == 0xFF) {
                encodedBuffer[codeIndex] = code.toByte()
                code = 1
                codeIndex = writeIndex++
            }
        }
    }

    encodedBuffer[codeIndex] = code.toByte()

    return encodedBuffer.take(writeIndex).toByteArray()
}

fun ByteArray.cobsDecode(
    size: Int = this.size
): ByteArray {
    if (size == 0)
        return ByteArray(0)

    var readIndex = 0
    var writeIndex = 0

    val decodedBuffer = ByteArray(this.size)
    var code: Int
    var i: Int
    while (readIndex < size) {
        code = this[readIndex].toInt() and 0xFF

        if (readIndex + code > size && code != 1) {
            return ByteArray(0)
        }

        readIndex++

        i = 1
        while (i < code) {
            decodedBuffer[writeIndex++] = this[readIndex++]
            i++
        }

        if (code != 0xFF && readIndex != size) {
            decodedBuffer[writeIndex++] = 0
        }
    }

    return decodedBuffer.take(writeIndex).toByteArray()
}

fun getEncodedBufferSize(unencodedBufferSize: Int): Int {
    return unencodedBufferSize.toInt() + unencodedBufferSize / 254 + 1
}
