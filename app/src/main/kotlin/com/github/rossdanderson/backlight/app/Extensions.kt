@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import java.awt.Color
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

fun Int.applySaturation(alpha: Double, greyscaleLuminosity: Double): Int =
    maxOf(0, minOf(255, (alpha * this + (1.0 - alpha) * greyscaleLuminosity).toInt()))

fun Int.applyContrast(contrastFactor: Double): Int =
    maxOf(0, minOf((contrastFactor * (this - 128) + 128).toInt(), 255))

fun Color.greyscaleLuminosity() = red * 0.299 + green * 0.587 + blue * 0.114

fun <U : Any> Flow<Flow<U>>.flatMapLatest(): Flow<U> = flatMapLatest { it }

fun <T> ObservableValue<T>.asFlow(): Flow<T> = callbackFlow {
    val listener = ChangeListener<T> { _, _, newValue -> offer(newValue) }
    addListener(listener)
    awaitClose {
        removeListener(listener)
    }
}

@ExperimentalTime
suspend fun delay(duration: Duration) {
    delay(duration.toLongMilliseconds())
}

fun ByteArray.cobsEncode(
    size: Int = this.size
): ByteArray {
    var readIndex = 0
    var writeIndex = 1
    var codeIndex = 0
    var code = 1

    val encodedBuffer = ByteArray(getEncodedBufferSize(size))

    while (readIndex < size) {
        if ((this[readIndex].toInt() and 0xFF) == 0) {
            encodedBuffer[codeIndex] = code.toByte()
            code = 1
            codeIndex = writeIndex++
            readIndex++
        } else {
            encodedBuffer[writeIndex++] = this[readIndex++]
            code++

            if (code == 0xFF) {
                encodedBuffer[codeIndex] = code.toByte()
                code = 1
                codeIndex = writeIndex++
            }
        }
    }

    encodedBuffer[codeIndex] = code.toByte()

    return encodedBuffer.take(writeIndex).toByteArray()
}

fun ByteArray.cobsDecode(
    size: Int = this.size
): ByteArray {
    if (size == 0)
        return ByteArray(0)

    var readIndex = 0
    var writeIndex = 0

    val decodedBuffer = ByteArray(this.size)
    var code: Int
    var i: Int
    while (readIndex < size) {
        code = this[readIndex].toInt() and 0xFF

        if (readIndex + code > size && code != 1) {
            return ByteArray(0)
        }

        readIndex++

        i = 1
        while (i < code) {
            decodedBuffer[writeIndex++] = this[readIndex++]
            i++
        }

        if (code != 0xFF && readIndex != size) {
            decodedBuffer[writeIndex++] = 0
        }
    }

    return decodedBuffer.take(writeIndex).toByteArray()
}

fun getEncodedBufferSize(unencodedBufferSize: Int): Int {
    return unencodedBufferSize + unencodedBufferSize / 254 + 1
}
