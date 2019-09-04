package com.github.rossdanderson.backlight.app.data

data class IntRange2D(
    val xRange: IntRange,
    val yRange: IntRange
) {
    inline fun forEach(function: (x: Int, y: Int) -> Unit) {
        xRange.forEach { x -> yRange.forEach { y -> function(x, y) } }
    }
}
