@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.switchMap
import java.awt.Color


fun Int.applySaturation(alpha: Double, greyscaleLuminosity: Double): Int =
    maxOf(0, minOf(255, (alpha * this + (1.0 - alpha) * greyscaleLuminosity).toInt()))

fun Int.applyContrast(contrastFactor: Double): Int =
    maxOf(0, minOf((contrastFactor * (this - 128) + 128).toInt(), 255))

fun Color.greyscaleLuminosity() = red * 0.299 + green * 0.587 + blue * 0.114

fun <U : Any> Flow<Flow<U>>.flattenSwitch(): Flow<U> = switchMap { it }

fun UByteArray.cobsEncode(): UByteArray {
    var readIndex = 0
    var writeIndex = 1
    var codeIndex = 0
    var code: UByte = 1u

    val encodedBufferSize = size + size / 254 + 1
    val encodedBuffer = UByteArray(encodedBufferSize)

    while (readIndex < size) {
        if (get(readIndex) == 0.toUByte()) {
            encodedBuffer[codeIndex] = code
            code = 1u
            codeIndex = writeIndex++
            readIndex++
        } else {
            encodedBuffer[writeIndex++] = get(readIndex++)
            code++

            if (code == 0xFFu.toUByte()) {
                encodedBuffer[codeIndex] = code
                code = 1u
                codeIndex = writeIndex++
            }
        }
    }

    encodedBuffer[codeIndex] = code

    return encodedBuffer
}