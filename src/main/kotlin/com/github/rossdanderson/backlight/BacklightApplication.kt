@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_RECEIVED
import com.fazecast.jSerialComm.SerialPort.getCommPorts
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortMessageListener
import kotlinx.coroutines.runBlocking
import java.awt.Color
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.lang.Integer.max
import java.lang.Integer.min
import java.nio.charset.Charset
import java.nio.file.Files
import javax.imageio.ImageIO

private val writeLED: UByte = 0u

private const val sampleRateMillis = 10000L
private const val minSampleRateMillis = 5000L
private const val step = 16

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

    val robot = Robot()

    // TODO Listen for changes to the screen size
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val screenRect = Rectangle(screenSize)


    captureScreen(robot, screenRect)

    val width = screenSize.width
    val height = screenSize.height

    println("$width x $height")

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


    val serialPort = selectSerialPort()

    serialPort.addDataListener(LISTENING_EVENT_DATA_RECEIVED) {
        println(it.receivedData!!.toString(Charset.forName("ASCII")).dropLast(1))
    }

    while (true) {
        val screenCapture = captureScreen(robot, screenRect)

        listOf(
            screenCapture.getSubimage(
                0,
                0,
                sideRowWidth,
                sideRowHeight
            ),
            screenCapture.getSubimage(
                sideRowWidth,
                0,
                midRowWidth,
                midRowHeight
            ),
            screenCapture.getSubimage(
                sideRowWidth + midRowWidth,
                0,
                midRowWidth,
                midRowHeight
            ),
            screenCapture.getSubimage(
                sideRowWidth + midRowWidth + midRowWidth,
                0,
                midRowWidth,
                midRowHeight
            ),
            screenCapture.getSubimage(
                sideRowWidth + midRowWidth + midRowWidth + midRowWidth,
                0,
                sideRowWidth,
                sideRowHeight
            ),
            screenCapture.getSubimage(
                sideRowWidth + midRowWidth + midRowWidth + midRowWidth,
                sideRowHeight,
                sideRowWidth,
                sideRowHeight
            ),
            screenCapture.getSubimage(
                sideRowWidth + midRowWidth + midRowWidth + midRowWidth,
                sideRowHeight + sideRowHeight,
                sideRowWidth,
                sideRowHeight
            ),
            screenCapture.getSubimage(
                sideRowWidth + midRowWidth + midRowWidth,
                midRowHeight,
                midRowWidth,
                midRowHeight
            ),
            screenCapture.getSubimage(
                sideRowWidth + midRowWidth,
                midRowHeight,
                midRowWidth,
                midRowHeight
            ),
            screenCapture.getSubimage(
                sideRowWidth,
                midRowHeight,
                midRowWidth,
                midRowHeight
            ),
            screenCapture.getSubimage(
                0,
                sideRowHeight + sideRowHeight,
                sideRowWidth,
                sideRowHeight
            ),
            screenCapture.getSubimage(
                0,
                sideRowHeight,
                sideRowWidth,
                sideRowHeight
            )
        )
            .mapIndexed { index, bufferedImage ->
                var red = 0
                var green = 0
                var blue = 0
                var count = 0

                (0 until bufferedImage.width).forEach { x ->
                    (0 until bufferedImage.height).map { y ->
                        count++
                        val color = Color(bufferedImage.getRGB(x, y))
                        red += color.red
                        green += color.green
                        blue += color.blue
                    }
                }

                WriteLEDMessage(
                    index.toUByte(),
                    (red / count).toUByte(),
                    (green / count).toUByte(),
                    (blue / count).toUByte(),
                    0.toUByte()
                )
            }
            .forEach {
                serialPort.writeMessage(it)
            }
    }
}

private fun captureScreen(robot: Robot, screenRect: Rectangle): BufferedImage {
    val screenCapture: BufferedImage = robot.createScreenCapture(screenRect)

    val saturatedCapture = BufferedImage(screenCapture.width, screenCapture.height, TYPE_INT_RGB)

    (0 until screenCapture.width).forEach { x ->
        (0 until screenCapture.height).forEach { y ->
            val rgb = Color(screenCapture.getRGB(x, y))
            val greyscaleLuminosity = (rgb.red * 0.299).toInt() +
                    (rgb.green * 0.587).toInt() +
                    (rgb.blue * 0.114).toInt()

            val alpha = 2f

            fun Int.applySaturation(): Int =
                max(0, min(255, (alpha * this + (1.0 - alpha) * greyscaleLuminosity).toInt()))


            val contrast = 10

            fun Int.applyContrast(): Int {
                val factor: Float = (259f * (contrast + 255f)) / (255f * (259f - contrast))
                return max(0, min((factor * (this - 128) + 128).toInt(), 255))
            }

            val r = rgb.red.applySaturation().applyContrast()
            val g = rgb.green.applySaturation().applyContrast()
            val b = rgb.blue.applySaturation().applyContrast()

            saturatedCapture.setRGB(
                x,
                y,
                1,
                1,
                intArrayOf(
                    Color(
                        r,
                        g,
                        b
                    ).rgb
                ),
                0,
                0
            )
        }
    }

    val tempFilePath = Files.createTempFile("file", ".png").toAbsolutePath()
    println(tempFilePath.toUri())
    val tempFile = tempFilePath.toFile()
    ImageIO.write(saturatedCapture, "png", tempFile)
    return saturatedCapture
}

private fun SerialPort.addDataListener(
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
        System.exit(-1)
    }

    println("Select a device:")
    commPorts.forEach { (index, commPort) -> println("  $index. ${commPort.descriptivePortName}") }

    return runCatching { readLine()!!.toInt() }
        .mapCatching {
            commPorts.getValue(it).apply {
                if (!openPort()) throw IllegalStateException("Unable to connect")
            }
        }
        .onSuccess { println("Connected to ${it.descriptivePortName}") }
        .onFailure(Throwable::printStackTrace)
        .getOrNull() ?: selectSerialPort()
}