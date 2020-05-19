@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.screen.source.dxgi

import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.data.Image
import com.github.rossdanderson.backlight.app.delay
import com.github.rossdanderson.backlight.app.flatMapLatest
import com.github.rossdanderson.backlight.app.logDurations
import com.github.rossdanderson.backlight.app.screen.IScreenService
import com.github.rossdanderson.backlight.app.screen.source.dxgi.generated.Capture
import com.github.rossdanderson.backlight.app.share
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.nio.file.Paths
import kotlin.math.abs
import kotlin.time.*

@ExperimentalTime
class DXGIScreenService(
    configService: ConfigService
) : IScreenService {

    private val minDelayMillisFlow = configService.configFlow.map { it.minDelayMillis }.distinctUntilChanged()
    private val sampleStepFlow = configService.configFlow.map { it.sampleStep }.distinctUntilChanged()

    override val screenFlow: Flow<BufferedImage> =
        flow<BufferedImage> {
            val capture = Capture()
            val captureLogger = logger.logDurations("Captures", 100)
            emitAll(
                combine(sampleStepFlow, minDelayMillisFlow) { sampleStep, minDelayMillis ->
                    flow {
                        initLoop@ while (true) {
                            val bufferSizeArray = LongArray(1)
                            if (capture.init(sampleStep, bufferSizeArray) != 0) {
                                continue@initLoop
                            }
                            val arraySize = bufferSizeArray[0]
                            if (arraySize == 0L) {
                                val duration = 1.seconds
                                logger.warn { "Unable to initialise, retrying in $duration" }
                                delay(duration)
                                continue@initLoop
                            }
                            logger.info { "Using buffer array size: $arraySize" }

                            val dimensions = capture.dimensions

                            val width = abs(dimensions.point1.x - dimensions.point2.x)
                            val height = abs(dimensions.point1.y - dimensions.point2.y)

                            captureLoop@ while (true) {
                                val startTime = System.currentTimeMillis()
                                val array = ByteArray(arraySize.toInt())
                                val (captureResult, captureDuration) = measureTimedValue {
                                    capture.getOutputBits(array, arraySize)
                                }
                                captureLogger(captureDuration)

                                when (captureResult) {
                                    0 -> {
                                        val bufferedImage = BufferedImage(width, height, TYPE_INT_RGB)
                                        Image(bufferedImage).apply {
                                            (0 until height).forEach { y ->
                                                val yOffset = y * width * 4
                                                (0 until width).forEach { x ->
                                                    val xOffset = x * 4
                                                    val r = array[yOffset + xOffset + 2].toUByte()
                                                    val g = array[yOffset + xOffset + 1].toUByte()
                                                    val b = array[yOffset + xOffset].toUByte()
                                                    val a = array[yOffset + xOffset + 3].toUByte()
                                                    set(
                                                        x,
                                                        y,
                                                        Color(r.toInt(), g.toInt(), b.toInt(), a.toInt())
                                                    )
                                                }
                                            }
                                            emit(bufferedImage)
                                        }
                                    }
                                    else -> TODO("Not currently implemented")
                                }

                                val delayDuration =
                                    minDelayMillis.milliseconds - (System.currentTimeMillis() - startTime).milliseconds
                                if (delayDuration > Duration.ZERO) {
                                    delay(delayDuration)
                                }
                            }
                        }
                    }.flowOn(Dispatchers.IO)
                }
                    .flatMapLatest()
            )
        }
            .share(GlobalScope)

    companion object {
        init {
            System.load(Paths.get("native-libs/dxgi-capture-java.dll").toAbsolutePath().toString())
        }

        private val logger = KotlinLogging.logger { }
    }
}
