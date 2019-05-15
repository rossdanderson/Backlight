@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_RECEIVED
import com.fazecast.jSerialComm.SerialPort.getCommPorts
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.awt.Color
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.lang.Integer.max
import java.lang.Integer.min
import java.nio.charset.Charset
import java.time.Duration
import java.time.Instant
import kotlin.Result.Companion.success
import kotlin.streams.asSequence
import kotlin.streams.asStream

private const val alpha = 2.0
private const val contrast = 10.0
private const val contrastFactor: Double = (259.0 * (contrast + 255.0)) / (255.0 * (259.0 - contrast))


fun Int.applySaturation(greyscaleLuminosity: Double): Int =
    max(0, min(255, (alpha * this + (1.0 - alpha) * greyscaleLuminosity).toInt()))

fun Int.applyContrast(): Int =
    max(0, min((contrastFactor * (this - 128) + 128).toInt(), 255))

/*
Message headers
 */
private const val writeLED: UByte = 0u
// TODO handshake, heartbeats, reconnection

interface Message {
    val backingArray: UByteArray
}

inline class WriteLEDMessage(
    override val backingArray: UByteArray
) : Message {
    constructor(
        index: UByte,
        red: UByte,
        green: UByte,
        blue: UByte,
        white: UByte
    ) : this(
        ubyteArrayOf(
            writeLED,
            index,
            red,
            green,
            blue,
            white
        )
    )
}

fun main() = runBlocking {

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

    val sideRowWidth = width / 4
    val midRowWidth = width / 4 * 2 / 3
    val sideRowHeight = height / 3
    val midRowHeight = height / 2

    /*
     __ __ __ __ __
    | 1| 2| 3| 4| 5|
    |__|  |  |  |__|
    |12|__|__|__| 6|
    |__|10| 9| 8|__|
    |11|  |  |  | 7|
    |__|__|__|__|__|
     */

    val screenSections = listOf(
        createIndices(
            0,
            0,
            sideRowWidth,
            sideRowHeight
        ),
        createIndices(
            sideRowWidth,
            0,
            midRowWidth,
            midRowHeight
        ),
        createIndices(
            sideRowWidth + midRowWidth,
            0,
            midRowWidth,
            midRowHeight
        ),
        createIndices(
            sideRowWidth + midRowWidth + midRowWidth,
            0,
            midRowWidth,
            midRowHeight
        ),
        createIndices(
            sideRowWidth + midRowWidth + midRowWidth + midRowWidth,
            0,
            sideRowWidth,
            sideRowHeight
        ),
        createIndices(
            sideRowWidth + midRowWidth + midRowWidth + midRowWidth,
            sideRowHeight,
            sideRowWidth,
            sideRowHeight
        ),
        createIndices(
            sideRowWidth + midRowWidth + midRowWidth + midRowWidth,
            sideRowHeight + sideRowHeight,
            sideRowWidth,
            sideRowHeight
        ),
        createIndices(
            sideRowWidth + midRowWidth + midRowWidth,
            midRowHeight,
            midRowWidth,
            midRowHeight
        ),
        createIndices(
            sideRowWidth + midRowWidth,
            midRowHeight,
            midRowWidth,
            midRowHeight
        ),
        createIndices(
            sideRowWidth,
            midRowHeight,
            midRowWidth,
            midRowHeight
        ),
        createIndices(
            0,
            sideRowHeight + sideRowHeight,
            sideRowWidth,
            sideRowHeight
        ),
        createIndices(
            0,
            sideRowHeight,
            sideRowWidth,
            sideRowHeight
        )
    )

    while (true) {
        val start = Instant.now()
        val screenCapture = captureScreen(robot, screenRect)

        screenSections.asSequence()
            .mapIndexed { index, pair -> index to pair }
            .asStream()
            .parallel()
            .map { (index, intRange2d) ->

                var red = 0
                var green = 0
                var blue = 0
                var count = 0

                intRange2d.forEach { x, y ->
                    val color = screenCapture[x, y]
                    count++
                    red += color.red
                    green += color.green
                    blue += color.blue
                }

                WriteLEDMessage(
                    index.toUByte(),
                    (red / count).toUByte(),
                    (green / count).toUByte(),
                    (blue / count).toUByte(),
                    0.toUByte()
                )
            }
            .sequential()
            .asSequence()
            .forEach { serialPort.writeMessage(it) }
        println("Full update took: ${Duration.between(start, Instant.now()).toMillis()}ms")

        // TODO need to implement ACKs/Semaphores so we don't overload the ESP and drop messages
        delay(200)
    }
}

private fun captureScreen(robot: Robot, screenRect: Rectangle): Array2D<Color> {
    val captureStart = Instant.now()
    val screenCapture = FastRGB(robot.createScreenCapture(screenRect))
    println("Capture took :${Duration.between(captureStart, Instant.now()).toMillis()}ms")

    val simpleProcessingStart = Instant.now()
    val saturatedCaptureArray2D = Array2D(screenRect.width, screenRect.height) { x, y ->

        val rgb = screenCapture.getRGB(x, y)

        val red = rgb.red
        val green = rgb.green
        val blue = rgb.blue

        val greyscaleLuminosity = red * 0.299 + green * 0.587 + blue * 0.114

        val r = red.applySaturation(greyscaleLuminosity).applyContrast()
        val g = green.applySaturation(greyscaleLuminosity).applyContrast()
        val b = blue.applySaturation(greyscaleLuminosity).applyContrast()

        Color(r, g, b)
    }

    println("Simple processing took :${Duration.between(simpleProcessingStart, Instant.now()).toMillis()}ms")

    return saturatedCaptureArray2D
}

private fun SerialPort.writeMessage(message: Message) {
    val encoded = message.backingArray.cobsEncode().toByteArray()
    val bytes = encoded + 0u.toUByte().toByte()
    writeBytes(bytes, bytes.size.toLong())
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
        .mapCatching { it.apply { if (!it.openPort()) throw IllegalStateException("Unable to connect") } }
        .onSuccess { println("Connected to ${it.descriptivePortName}") }
        .onFailure(Throwable::printStackTrace)
        .getOrNull() ?: selectSerialPort()
}

data class IntRange2D(
    val xRange: IntRange,
    val yRange: IntRange
) {
    inline fun forEach(function: (x: Int, y: Int) -> Unit) {
        xRange.forEach { x -> yRange.forEach { y -> function(x, y) } }
    }
}

fun createIndices(x: Int, y: Int, xWidth: Int, yWidth: Int): IntRange2D =
    IntRange2D(createIndices(x, xWidth), createIndices(y, yWidth))

fun createIndices(start: Int, length: Int): IntRange = start until (start + length)