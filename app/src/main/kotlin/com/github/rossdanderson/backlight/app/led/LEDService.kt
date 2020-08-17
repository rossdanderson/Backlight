@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight.app.led

import com.github.rossdanderson.backlight.app.applyContrast
import com.github.rossdanderson.backlight.app.applySaturation
import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.data.IntRange2D
import com.github.rossdanderson.backlight.app.data.LEDColors
import com.github.rossdanderson.backlight.app.data.UColor
import com.github.rossdanderson.backlight.app.greyscaleLuminosity
import com.github.rossdanderson.backlight.app.screen.IScreenService
import com.github.rossdanderson.backlight.app.serial.ConnectionState
import com.github.rossdanderson.backlight.app.serial.ISerialService
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.flow.*
import java.lang.Double.max
import kotlin.math.sqrt
import kotlin.time.ExperimentalTime

@ExperimentalTime
class LEDService(
    private val screenService: IScreenService,
    configService: ConfigService,
    serialService: ISerialService
) {
    private val ledCountFlow = flowOf(
        flowOf(10),
        serialService.connectionState
            .filterIsInstance<ConnectionState.Connected>()
            .map { it.ledCount }
            .distinctUntilChanged()
    ).flattenConcat()

    private val contrastFactorFlow = configService.configFlow.map { it.contrastFactor }.distinctUntilChanged()
    private val saturationAlphaFlow = configService.configFlow.map { it.saturationAlpha }.distinctUntilChanged()

    val ledColorsFlow: Flow<LEDColors> = ledCountFlow
        .flatMapLatest { ledCount ->
            var prevImageWidth: Int? = null
            var prevImageHeight: Int? = null
            var screenSections: List<IntRange2D>? = null

            combine(
                screenService.screenFlow.map { it },
                contrastFactorFlow,
                saturationAlphaFlow
            ) { screenData, contrastFactor, saturationAlpha ->
                val image = screenData.image
                // If the image dimensions or the number of LEDs changes, remap the screen sections to sample from
                if (screenSections == null || prevImageHeight != image.height || prevImageWidth != image.width) {
                    prevImageWidth = image.width
                    prevImageHeight = image.height

                    val sampleWidthSteps = minOf(ledCount, image.width)

                    val sampleHeight = (max(image.height * 0.10, 1.0)).toInt()
                    val sampleWidth = image.width.toDouble() / sampleWidthSteps.toDouble()

                    screenSections = (0 until ledCount).map {
                        val left = (it * sampleWidth).toInt()
                        val right = minOf(image.width, ((it + 1) * sampleWidth).toInt())
                        IntRange2D(
                            xRange = offsetIntRange(left, right - left),
                            yRange = offsetIntRange(image.height - (image.height * 0.25).toInt(), sampleHeight)
                        )
                    }
                }

                val toList: List<UColor> = screenSections!!
                    .map { intRange2D ->
                        var red = 0
                        var green = 0
                        var blue = 0
                        var count = 0

                        intRange2D.forEach { x, y ->
                            val rgb = image[x, y]
                            val greyscaleLuminosity = rgb.greyscaleLuminosity()
                            red += rgb.red.toInt()
                                .applySaturation(saturationAlpha, greyscaleLuminosity)
                                .applyContrast(contrastFactor)
                                .let { it * it }
                            green += rgb.green.toInt()
                                .applySaturation(saturationAlpha, greyscaleLuminosity)
                                .applyContrast(contrastFactor)
                                .let { it * it }
                            blue += rgb.blue.toInt()
                                .applySaturation(saturationAlpha, greyscaleLuminosity)
                                .applyContrast(contrastFactor)
                                .let { it * it }
                            count++
                        }

                        val mul = 1.0 / count

                        val redAvg = sqrt(red * mul).toInt().toUByte()
                        val greenAvg = sqrt(green * mul).toInt().toUByte()
                        val blueAvg = sqrt(blue * mul).toInt().toUByte()

                        UColor(redAvg, greenAvg, blueAvg)
                    }
                    .toList()


                LEDColors(screenData.sourceTimestamp, toList)
            }
        }
        .distinctUntilChanged()
        .conflate()
        .flowOn(Default)

    private fun offsetIntRange(start: Int, length: Int): IntRange = start until (start + length)
}
