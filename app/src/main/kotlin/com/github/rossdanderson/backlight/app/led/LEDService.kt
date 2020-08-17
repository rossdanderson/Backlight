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
    serialService: ISerialService,
) {
    private val ledCountFlow =
        flowOf(
            flowOf(10),
            serialService.connectionState
                .filterIsInstance<ConnectionState.Connected>()
                .map { it.ledCount }
                .distinctUntilChanged()
        )
            .flattenConcat()

    private val contrastFactorFlow = configService.configFlow
        .map { config -> config.contrastFactor }
        .distinctUntilChanged()

    private val saturationAlphaFlow = configService.configFlow
        .map { config -> config.saturationAlpha }
        .distinctUntilChanged()

    val ledColorsFlow: Flow<LEDColors> = ledCountFlow
        .flatMapLatest { ledCount ->
            var prevImageWidth: Int? = null
            var prevImageHeight: Int? = null
            var screenSections: List<IntRange2D>? = null

            combine(
                screenService.screenFlow,
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

                val ledColorData: List<UColor> = screenSections!!
                    .map { intRange2D ->
                        var red = 0
                        var green = 0
                        var blue = 0
                        var count = 0

                        intRange2D.forEach { x, y ->
                            val rgb = image[x, y]
                            val greyscaleLuminosity = rgb.greyscaleLuminosity()

                            fun UByte.apply() =
                                toInt()
                                    .applySaturation(saturationAlpha, greyscaleLuminosity)
                                    .applyContrast(contrastFactor)
                                    .let { it * it }

                            red += rgb.red.apply()
                            green += rgb.green.apply()
                            blue += rgb.blue.apply()
                            count++
                        }

                        val mul = 1.0 / count

                        fun Int.avg() = sqrt(this * mul).toInt().toUByte()

                        UColor(red.avg(), green.avg(), blue.avg())
                    }
                    .toList()

                LEDColors(screenData.sourceTimestamp, ledColorData)
            }
        }
        .distinctUntilChanged()
        .conflate()
        .flowOn(Default)

    private fun offsetIntRange(start: Int, length: Int): IntRange = start until (start + length)
}
