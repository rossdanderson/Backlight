package com.github.rossdanderson.backlight.app.data

import java.time.Instant

data class LEDColors(
    val sourceTimestamp: Instant,
    val colors: List<UColor>,
)
