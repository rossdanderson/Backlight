@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.ui

import com.github.rossdanderson.backlight.app.config.Config.Companion.contrastLens
import com.github.rossdanderson.backlight.app.config.Config.Companion.saturationAlphaLens
import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.ui.base.BaseViewModel
import com.github.rossdanderson.backlight.app.ui.command.command
import kotlinx.coroutines.flow.map
import java.time.Duration

class ControlViewModel : BaseViewModel() {

    private val configService by di<ConfigService>()

    val saturationFlow = configService.configFlow
        .map { it.saturationAlpha }

    val updateSaturation = command<Double>(debounce = Duration.ofMillis(100)) { saturationAlpha ->
        configService.set(saturationAlphaLens, saturationAlpha)
    }

    val contrastFlow = configService.configFlow
        .map { it.contrast }

    val updateContrast = command<Double>(debounce = Duration.ofMillis(100)) { contrast ->
        configService.set(contrastLens, contrast)
    }
}
