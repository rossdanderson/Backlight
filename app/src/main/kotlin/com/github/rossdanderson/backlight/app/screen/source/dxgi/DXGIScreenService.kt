@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.screen.source.dxgi

import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.data.Image
import com.github.rossdanderson.backlight.app.delay
import com.github.rossdanderson.backlight.app.logDurations
import com.github.rossdanderson.backlight.app.screen.IScreenService
import com.github.rossdanderson.backlight.app.screen.source.dxgi.generated.Capture
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
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import kotlin.time.milliseconds

@ExperimentalTime
class DXGIScreenService(
    configService: ConfigService
) : IScreenService {

    private val capture = Capture(nativeLogger)

    init {
        capture.init()
    }

    private val minDelayMillisFlow = configService.configFlow.map { it.minDelayMillis }.distinctUntilChanged()

    override val screenFlow: Flow<BufferedImage> =
        flow<BufferedImage> {
            val captureLogger = logger.logDurations("Captures", 10)
            emitAll(
                minDelayMillisFlow
                    .flatMapLatest { minDelayMillis ->
                        flow {
                            while (true) {
                                delay(minDelayMillis.milliseconds)
                                val size = capture.getOutputBits(byteArrayOf(1), 1)
                                val array = ByteArray(size.toInt())

                                val timedValue = measureTimedValue {
                                    capture.getOutputBits(array, size)
                                }
                                captureLogger(timedValue.duration)

                                if (timedValue.value >= size) {
                                    val dimensions = capture.dimensions
                                    val sample = 4 // TODO have this as config
                                    val width = dimensions.point2.x / sample
                                    val height = dimensions.point2.y / sample
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
                                                    Color(
                                                        r.toInt(),
                                                        g.toInt(),
                                                        b.toInt(),
                                                        a.toInt()
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    emit(bufferedImage)
                                }
                            }
                        }.flowOn(Dispatchers.IO)
                    }
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
