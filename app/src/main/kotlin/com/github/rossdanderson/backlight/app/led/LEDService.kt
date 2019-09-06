@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.led

import com.github.rossdanderson.backlight.app.applyContrast
import com.github.rossdanderson.backlight.app.applySaturation
import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.data.Image
import com.github.rossdanderson.backlight.app.data.IntRange2D
import com.github.rossdanderson.backlight.app.data.UColor
import com.github.rossdanderson.backlight.app.greyscaleLuminosity
import com.github.rossdanderson.backlight.app.screen.IScreenService
import com.github.rossdanderson.backlight.app.serial.ConnectionState
import com.github.rossdanderson.backlight.app.serial.ISerialService
import kotlinx.coroutines.flow.*

class LEDService(
    private val screenService: IScreenService,
    configService: ConfigService,
    serialService: ISerialService
) {
    private val ledCountFlow = flowOf(
        flowOf(10),
        serialService.connectionStateFlow
            .filterIsInstance<ConnectionState.Connected>()
            .map { it.ledCount }
            .distinctUntilChanged()
    ).flattenConcat()

    private val contrastFactorFlow = configService.configFlow.map { it.contrastFactor }.distinctUntilChanged()
    private val saturationAlphaFlow = configService.configFlow.map { it.saturationAlpha }.distinctUntilChanged()

    val ledColorsFlow: Flow<List<UColor>> = ledCountFlow
        .switchMap { ledCount ->
            var prevImageWidth: Int? = null
            var prevImageHeight: Int? = null
            var screenSections: List<IntRange2D>? = null

            screenService.screenFlow
                .map { Image(it) }
                .combineLatest(contrastFactorFlow, saturationAlphaFlow) { image, contrastFactor, saturationAlpha ->
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
                        .map { intRange2D ->
                            var red = 0
                            var green = 0
                            var blue = 0
                            var count = 0

                            intRange2D.forEach { x, y ->
                                val rgb = image[x, y]
                                val greyscaleLuminosity = rgb.greyscaleLuminosity()
                                red += rgb.red
                                    .applySaturation(saturationAlpha, greyscaleLuminosity)
                                    .applyContrast(contrastFactor)
                                green += rgb.green
                                    .applySaturation(saturationAlpha, greyscaleLuminosity)
                                    .applyContrast(contrastFactor)
                                blue += rgb.blue
                                    .applySaturation(saturationAlpha, greyscaleLuminosity)
                                    .applyContrast(contrastFactor)
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
        .distinctUntilChanged()
        .conflate()

    private fun offsetIntRange(start: Int, length: Int): IntRange = start until (start + length)
}
