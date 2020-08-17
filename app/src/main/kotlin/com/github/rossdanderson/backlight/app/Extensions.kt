@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight.app

import com.github.rossdanderson.backlight.app.data.UColor
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import mu.KLogger
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlin.time.Duration.Companion.INFINITE
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.ExperimentalTime

fun Int.applySaturation(alpha: Double, greyscaleLuminosity: Double): Int =
    maxOf(0, minOf(255, ((alpha * this + (1.0 - alpha) * greyscaleLuminosity).toInt())))

fun Int.applyContrast(contrastFactor: Double): Int =
    maxOf(0, minOf((contrastFactor * (this - 128) + 128).toInt(), 255))

fun UColor.greyscaleLuminosity(): Double = red.toDouble() * 0.299 + green.toDouble() * 0.587 + blue.toDouble() * 0.114

fun <U : Any> Flow<Flow<U>>.flatMapLatest(): Flow<U> = flatMapLatest { it }

fun <U : Any> Flow<U>.share(scope: CoroutineScope): Flow<U> {

    val references = AtomicInteger()
    val subscription = AtomicReference<Job>()

    val sharedBroadcastChannel = BroadcastChannel<U>(Channel.BUFFERED)

    return flow {
        if (references.getAndIncrement() == 0) {
            subscription.set(this@share.onEach { sharedBroadcastChannel.send(it) }.launchIn(scope))
        }
        onCompletion {
            if (references.getAndDecrement() == 1) {
                subscription.getAndSet(null).cancel()
            }
        }
        emitAll(sharedBroadcastChannel.openSubscription())
    }
}

fun <T> ObservableValue<T>.asFlow(): Flow<T> = callbackFlow {
    val listener = ChangeListener<T> { _, _, newValue -> offer(newValue) }
    addListener(listener)
    awaitClose {
        removeListener(listener)
    }
}

// Should be called from single thread
@ExperimentalTime
fun KLogger.logDurations(message: String, times: Int): (Duration) -> Unit {
    var count = 0
    var totalDuration = ZERO
    var minDuration = INFINITE
    var maxDuration = ZERO

    return { duration ->
        count++
        totalDuration += duration
        minDuration = minOf(minDuration, duration)
        maxDuration = maxOf(maxDuration, duration)
        if (count >= times) {
            info { "$message - count: $count total: $totalDuration - avg: ${totalDuration / count} - min: $minDuration - max: $maxDuration" }
            count = 0
            totalDuration = ZERO
            minDuration = INFINITE
            maxDuration = ZERO
        }
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
