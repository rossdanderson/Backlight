@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.screen.source.dxgi

import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.data.Image
import com.github.rossdanderson.backlight.app.delay
import com.github.rossdanderson.backlight.app.flatMapLatest
import com.github.rossdanderson.backlight.app.logDurations
import com.github.rossdanderson.backlight.app.screen.IScreenService
import com.github.rossdanderson.backlight.app.screen.source.dxgi.generated.Capture
import com.github.rossdanderson.backlight.app.screen.source.dxgi.generated.CaptureResult.*
import com.github.rossdanderson.backlight.app.screen.source.dxgi.generated.Logger
import com.github.rossdanderson.backlight.app.share
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.nio.file.Paths
import kotlin.time.*

@ExperimentalTime
class DXGIScreenService(
    configService: ConfigService
) : IScreenService {

    private val minDelayMillisFlow = configService.configFlow.map { it.minDelayMillis }.distinctUntilChanged()
    private val sampleStepFlow = configService.configFlow.map { it.sampleStep }.distinctUntilChanged()

    override val screenFlow: Flow<BufferedImage> =
        flow<BufferedImage> {
            val capture = Capture(nativeLogger)
            val captureLogger = logger.logDurations("Captures", 100)
            emitAll(
                combine(sampleStepFlow, minDelayMillisFlow) { sampleStep, minDelayMillis ->
                    flow {
                        initLoop@ while (true) {
                            val arraySize = capture.init(sampleStep)
                            if (arraySize == 0L) {
                                val duration = 1.seconds
                                logger.warn { "Unable to initialise, retrying in $duration" }
                                delay(duration)
                                continue@initLoop
                            }
                            logger.info { "Using buffer array size: $arraySize" }

                            val dimensions = capture.dimensions
                            val width = dimensions.width()
                            val height = dimensions.height()

                            captureLoop@ while (true) {
                                val startTime = System.currentTimeMillis()
                                val array = ByteArray(arraySize.toInt())
                                val (captureResult, captureDuration) = measureTimedValue {
                                    capture.getOutputBits(array, arraySize)
                                }
                                captureLogger(captureDuration)

                                when (captureResult) {
                                    Success -> {
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
                                    FailureBufferTooSmall -> continue@initLoop
                                    FailureInitRequired -> continue@initLoop
                                    FailureNotImplemented -> TODO("Not currently implemented")
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

        // Must not allow this to be GC'd - thus held as static instance.
        private val nativeLogger = object : Logger() {
            override fun info(message: String) {
                logger.info(message)
            }

            override fun warn(message: String) {
                logger.warn(message)
            }

            override fun error(message: String) {
                logger.error(message)
            }
        }
    }
}
