@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.ui

import com.github.rossdanderson.backlight.app.config.Config.Companion.brightnessLens
import com.github.rossdanderson.backlight.app.config.Config.Companion.contrastLens
import com.github.rossdanderson.backlight.app.config.Config.Companion.sampleStepLens
import com.github.rossdanderson.backlight.app.config.Config.Companion.saturationAlphaLens
import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.ui.base.BaseViewModel
import com.github.rossdanderson.backlight.app.ui.command.command
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

@ExperimentalTime
class ControlViewModel : BaseViewModel() {

    private val configService by di<ConfigService>()

    val brightnessFlow = configService.configFlow
        .map { it.brightness }

    val updateBrightness = command<Double>(debounce = 100.milliseconds) { brightness ->
        configService.set(brightnessLens, brightness)
    }

    val sampleStepFlow = configService.configFlow
        .map { it.sampleStep }

    val updateSampleStep = command<Int>(debounce = 100.milliseconds) { sampleStep ->
        configService.set(sampleStepLens, sampleStep)
    }

    val saturationFlow = configService.configFlow
        .map { it.saturationAlpha }

    val updateSaturation = command<Double>(debounce = 100.milliseconds) { saturationAlpha ->
        configService.set(saturationAlphaLens, saturationAlpha)
    }

    val contrastFlow = configService.configFlow
        .map { it.contrast }

    val updateContrast = command<Double>(debounce = 100.milliseconds) { contrast ->
        configService.set(contrastLens, contrast)
    }
}
