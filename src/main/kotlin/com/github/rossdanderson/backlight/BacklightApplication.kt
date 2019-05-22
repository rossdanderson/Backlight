@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPort.*
import com.github.rossdanderson.backlight.messages.Message
import com.github.rossdanderson.backlight.messages.WriteAllMessage
import com.github.rossdanderson.backlight.messages.writeAll
import com.github.rossdanderson.backlight.messages.writeLED
import kotlinx.coroutines.runBlocking
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.nio.charset.Charset
import java.time.Duration
import java.time.Instant
import kotlin.Result.Companion.success
import kotlin.streams.toList

private const val ledCount = 60

fun main() = runBlocking {
    println("Write LED ($writeLED) messages will be ${3 + 1} bytes")
    println("Write all ($writeAll) messages will be ${ledCount * 3 + 1} bytes")

    val serialPort = selectSerialPort()

    serialPort.addDataListener(LISTENING_EVENT_DATA_RECEIVED) {
        val message = it.receivedData!!.toString(Charset.forName("ASCII")).dropLast(1)
        if (message.contains("ignoring", ignoreCase = true)) {
            System.err.println("Response -> $message")
        }
    }

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
            .let { serialPort.writeMessage(WriteAllMessage.from(ledCount, it)) }

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
    val encoded = message.backingArray.cobsEncode().toByteArray()
    val bytes = encoded + 0u.toUByte().toByte()
    if (writeBytes(bytes, bytes.size.toLong()) == -1) throw IllegalStateException("Failure to write")
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
            }
        }
        .onSuccess { println("Connected to ${it.descriptivePortName}") }
        .onFailure(Throwable::printStackTrace)
        .getOrNull() ?: selectSerialPort()
}

fun offsetIntRange(start: Int, length: Int): IntRange = start until (start + length)