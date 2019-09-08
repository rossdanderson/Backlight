package com.github.rossdanderson.backlight.app.config

import com.github.rossdanderson.backlight.app.data.Lens
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Serializable
data class Config(
    val defaultPort: String? = null,
    val minDelayMillis: Long = 50,
    val saturationAlpha: Double = 1.5,
    val contrast: Double = 7.0
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

        val saturationAlphaLens = Lens<Config, Double>(
            get = { s -> s.saturationAlpha },
            set = { s, a -> s.copy(saturationAlpha = a) }
        )

        val contrastLens = Lens<Config, Double>(
            get = { s -> s.contrast },
            set = { s, a -> s.copy(contrast = a) }
        )
    }
}
