package com.github.rossdanderson.backlight.app.config

import com.github.rossdanderson.backlight.app.data.Lens
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Serializable
data class Config(
    val defaultPort: String? = null,
    val minDelayMillis: Long = 5,
    val saturationAlpha: Double = 1.0,
    val contrast: Double = 1.0,
    val brightness: Double = 5.0,
    val sampleStep: Int = 32
) {
    @Transient
    val contrastFactor: Double = (259.0 * (contrast + 255.0)) / (255.0 * (259.0 - contrast))

    companion object {
        val defaultPortLens = Lens<Config, String?>(
            get = { s -> s.defaultPort },
            set = { s, a -> s.copy(defaultPort = a) }
        )

        val minDelayMillisLens = Lens<Config, Long>(
            get = { s -> s.minDelayMillis },
            set = { s, a -> s.copy(minDelayMillis = a) }
        )

        val brightnessLens = Lens<Config, Double>(
            get = { s -> s.brightness },
            set = { s, a -> s.copy(brightness = a) }
        )

        val saturationAlphaLens = Lens<Config, Double>(
            get = { s -> s.saturationAlpha },
            set = { s, a -> s.copy(saturationAlpha = a) }
        )

        val contrastLens = Lens<Config, Double>(
            get = { s -> s.contrast },
            set = { s, a -> s.copy(contrast = a) }
        )

        val sampleStepLens = Lens<Config, Int>(
            get = { s -> s.sampleStep },
            set = { s, a -> s.copy(sampleStep = a) }
        )
    }
}
