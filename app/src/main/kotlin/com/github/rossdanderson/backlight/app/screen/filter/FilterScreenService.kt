@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.screen.filter

import com.github.rossdanderson.backlight.app.applyContrast
import com.github.rossdanderson.backlight.app.applySaturation
import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.data.Image
import com.github.rossdanderson.backlight.app.greyscaleLuminosity
import com.github.rossdanderson.backlight.app.screen.IScreenService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.time.ExperimentalTime

@ExperimentalTime
class FilterScreenService(
    configService: ConfigService,
    sourceScreenService: IScreenService
) : IScreenService {

    private val saturationAlphaFlow = configService.configFlow.map { it.saturationAlpha }.distinctUntilChanged()
    private val contrastFactorFlow = configService.configFlow.map { it.contrastFactor }.distinctUntilChanged()

    override val screenFlow: Flow<BufferedImage> =
        combine(
            sourceScreenService.screenFlow,
            saturationAlphaFlow,
            contrastFactorFlow
        ) { bufferedImage, saturationAlpha, contrastFactor ->
            if (saturationAlpha != 1.0 || contrastFactor != 1.0) {
                Image(bufferedImage)
                    .map { rgb ->
                        val greyscaleLuminosity = rgb.greyscaleLuminosity()
                        Color(
                            rgb.red
                                .applySaturation(saturationAlpha, greyscaleLuminosity)
                                .applyContrast(contrastFactor),
                            rgb.green
                                .applySaturation(saturationAlpha, greyscaleLuminosity)
                                .applyContrast(contrastFactor),
                            rgb.blue
                                .applySaturation(saturationAlpha, greyscaleLuminosity)
                                .applyContrast(contrastFactor)
                        )
                    }
            }
            bufferedImage
        }
            .flowOn(Dispatchers.Default)
}
