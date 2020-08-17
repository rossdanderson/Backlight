package com.github.rossdanderson.backlight.app.data

import java.time.Instant

data class ScreenData(
    val sourceTimestamp: Instant,
    val image: ImageArray
)
