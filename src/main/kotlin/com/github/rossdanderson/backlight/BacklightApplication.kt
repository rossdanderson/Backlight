@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPort.TIMEOUT_WRITE_BLOCKING
import com.fazecast.jSerialComm.SerialPort.getCommPorts
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortMessageListener
import com.github.rossdanderson.backlight.messages.Message
import com.github.rossdanderson.backlight.messages.WriteAllMessage
import com.github.rossdanderson.backlight.messages.writeAll
import com.github.rossdanderson.backlight.messages.writeLED
import kotlinx.coroutines.runBlocking
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.lang.Thread.sleep
import java.nio.charset.Charset
import java.time.Duration
import java.time.Instant
import kotlin.Result.Companion.success
import kotlin.streams.toList


private const val ledCount = 60

private class MessageListener : SerialPortMessageListener {
    override fun getListeningEvents(): Int {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED
    }

    override fun getMessageDelimiter(): ByteArray {
        return byteArrayOf(0)
    }

    override fun delimiterIndicatesEndOfMessage(): Boolean {
        return true
    }

    override fun serialEvent(event: SerialPortEvent) {
        val encoded = event.receivedData!!
        val decoded = encoded.cobsDecode()
        println("-> rx dec: ${decoded.take(decoded.size).toByteArray().contentToString()} (${decoded.size})")
        println("-> rx enc: ${encoded.contentToString()} (${encoded.size})")
        println("-> rx str: ${decoded.toString(Charset.forName("ASCII"))}")
        println()
    }
}

fun main() = runBlocking {
    println("Write LED ($writeLED) messages will be ${3 + 1} bytes")
    println("Write all ($writeAll) messages will be ${ledCount * 3 + 1} bytes")

    val serialPort = selectSerialPort()

    val robot = Robot()

    // TODO Listen for changes to the screen size
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val screenRect = Rectangle(screenSize)

    val width = screenSize.width
    val height = screenSize.height

    println("Screen dimensions: $width x $height")

    val sampleHeight = 256
    val sampleWidth = (width.toDouble() / ledCount.toDouble()).toInt()

    val screenSections = (0 until ledCount).map {
        IntRange2D(
            xRange = offsetIntRange(it * sampleWidth, sampleWidth),
            yRange = offsetIntRange(height - sampleHeight, sampleHeight)
        )
    }

    while (true) {
        val start = Instant.now()
        val screenCapture = captureScreen(robot, screenRect)

        screenSections
            .stream()
            .parallel()
            .map { intRange2d ->
                var red = 0
                var green = 0
                var blue = 0
                var count = 0

                intRange2d.forEach { x, y ->
                    val rgb = screenCapture[x, y]
                    val greyscaleLuminosity = rgb.greyscaleLuminosity()
                    red += rgb.red.applySaturation(greyscaleLuminosity).applyContrast()
                    green += rgb.green.applySaturation(greyscaleLuminosity).applyContrast()
                    blue += rgb.blue.applySaturation(greyscaleLuminosity).applyContrast()
                    count++
                }

                val redAvg = (red / count).toUByte()
                val greenAvg = (green / count).toUByte()
                val blueAvg = (blue / count).toUByte()

                Color(redAvg, greenAvg, blueAvg)
            }
            .sequential()
            .toList()
            .let { serialPort.writeMessage(WriteAllMessage.from(it)) }

        println("Full update took: ${Duration.between(start, Instant.now()).toMillis()}ms")

        // TODO need to implement ACKs/Semaphores so we don't overload the ESP and drop messages
    }
}

private fun captureScreen(robot: Robot, screenRect: Rectangle): FastRGB {
    val captureStart = Instant.now()
    val screenCapture = FastRGB(robot.createScreenCapture(screenRect))
    println("Capture took :${Duration.between(captureStart, Instant.now()).toMillis()}ms")
    return screenCapture
}

private fun SerialPort.writeMessage(message: Message) {
    val decoded = message.backingArray.toByteArray()
    println("<- tx dec: ${decoded.contentToString()} (${decoded.size})")
    val encoded = decoded.cobsEncode()
    println("<- tx enc: ${encoded.contentToString()} (${encoded.size})")
    if (writeBytes(encoded, encoded.size.toLong()) == -1) throw IllegalStateException("Failure to write")
    if (writeBytes(byteArrayOf(0), 1L) == -1) throw IllegalStateException("Failure to write")
}

private tailrec fun selectSerialPort(): SerialPort {
    val commPorts = getCommPorts()
        .withIndex()
        .associateBy({ it.index }) { it.value }

    if (commPorts.isEmpty()) {
        System.err.println("No devices found")
        throw IllegalStateException("No serial ports found")
    }

    val result: Result<SerialPort> = if (commPorts.size == 1) {
        success(commPorts.values.single())
    } else runCatching {
        println("Select a device:")
        commPorts.forEach { (index, commPort) -> println("  $index. ${commPort.descriptivePortName}") }
        commPorts.getValue(readLine()!!.toInt())
    }

    return result
        .mapCatching {
            it.apply {
                baudRate = 115200
                setComPortTimeouts(TIMEOUT_WRITE_BLOCKING, 1000, 1000)
                if (!openPort()) throw IllegalStateException("Unable to connect")
                addDataListener(MessageListener())
                sleep(5000)
            }
        }
        .onSuccess { println("Connected to ${it.descriptivePortName}") }
        .onFailure(Throwable::printStackTrace)
        .getOrNull() ?: selectSerialPort()
}

fun offsetIntRange(start: Int, length: Int): IntRange = start until (start + length)
