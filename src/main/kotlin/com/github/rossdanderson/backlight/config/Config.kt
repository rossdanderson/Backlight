package com.github.rossdanderson.backlight.config

import arrow.optics.optics
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@optics
@Serializable
data class Config(
    val defaultPort: String? = null,
    val minDelayMillis: Long = 50,
    val ledCount: Int = 60,
    val saturationAlpha: Double = 1.5,
    val contrast: Double = 7.0
) {
    @Transient
    val contrastFactor: Double = (259.0 * (contrast + 255.0)) / (255.0 * (259.0 - contrast))

    companion object
}