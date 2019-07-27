package com.github.rossdanderson.backlight.screensample

import com.github.rossdanderson.backlight.applyContrast
import com.github.rossdanderson.backlight.applySaturation
import com.github.rossdanderson.backlight.config.ConfigService
import com.github.rossdanderson.backlight.data.Image
import com.github.rossdanderson.backlight.data.IntRange2D
import com.github.rossdanderson.backlight.data.UColor
import com.github.rossdanderson.backlight.flattenSwitch
import com.github.rossdanderson.backlight.greyscaleLuminosity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import java.awt.Dimension
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage

@FlowPreview
@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
class ScreenSampleService(
    configService: ConfigService
) {
    private val robot = Robot()

    private val logger = KotlinLogging.logger { }

    // TODO Should listen for changes
    private val screenDimensionsFlow: Flow<Dimension> = flowOf(Toolkit.getDefaultToolkit().screenSize)

    private val minDelayMillisFlow = configService.configFlow.map { it.minDelayMillis }.distinctUntilChanged()
    private val ledCountFlow = configService.configFlow.map { it.ledCount }.distinctUntilChanged()

    val screenFlow: Flow<BufferedImage> = minDelayMillisFlow
        .combineLatest(screenDimensionsFlow) { minDelayMillis, dimensions ->
            val screenRect = Rectangle(dimensions)
            flow {
                while (true) {
                    emit(robot.createScreenCapture(screenRect))
                    delay(minDelayMillis)
                }
            }.flowOn(Dispatchers.IO)
        }
        .flattenSwitch()

    val ledColorFlow: Flow<List<UColor>> = ledCountFlow
        .switchMap { ledCount ->
            var prevImageWidth: Int? = null
            var prevImageHeight: Int? = null
            var screenSections: List<IntRange2D>? = null

            screenFlow
                .map { Image(it) }
                .map { image ->
                    // If the image dimensions or the number of LEDs changes, remap the screen sections to sample from
                    if (screenSections == null || prevImageHeight != image.height || prevImageWidth != image.width) {
                        prevImageWidth = image.width
                        prevImageHeight = image.height

                        val sampleHeight = 256
                        val sampleWidth = (image.width.toDouble() / ledCount.toDouble()).toInt()

                        screenSections = (0 until ledCount).map {
                            IntRange2D(
                                xRange = offsetIntRange(it * sampleWidth, sampleWidth),
                                yRange = offsetIntRange(image.height - sampleHeight, sampleHeight)
                            )
                        }
                    }

                    screenSections!!
                        .map { intRange2d ->
                            var red = 0
                            var green = 0
                            var blue = 0
                            var count = 0

                            intRange2d.forEach { x, y ->
                                val rgb = image[x, y]
                                val greyscaleLuminosity = rgb.greyscaleLuminosity()
                                red += rgb.red.applySaturation(greyscaleLuminosity).applyContrast()
                                green += rgb.green.applySaturation(greyscaleLuminosity).applyContrast()
                                blue += rgb.blue.applySaturation(greyscaleLuminosity).applyContrast()
                                count++
                            }

                            val redAvg = (red / count).toUByte()
                            val greenAvg = (green / count).toUByte()
                            val blueAvg = (blue / count).toUByte()

                            UColor(redAvg, greenAvg, blueAvg)
                        }
                        .toList()
                }
        }
        .conflate()

    private fun offsetIntRange(start: Int, length: Int): IntRange = start until (start + length)
}
