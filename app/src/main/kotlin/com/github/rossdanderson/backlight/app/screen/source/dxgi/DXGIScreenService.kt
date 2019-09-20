@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.screen.source.dxgi

import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.delay
import com.github.rossdanderson.backlight.app.screen.IScreenService
import com.github.rossdanderson.backlight.app.screen.source.dxgi.generated.Capture
import com.github.rossdanderson.backlight.app.screen.source.dxgi.generated.Logger
import com.github.rossdanderson.backlight.app.share
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import java.awt.image.BufferedImage
import java.nio.file.Paths
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

@ExperimentalTime
class DXGIScreenService(
    configService: ConfigService
) : IScreenService {

    private val minDelayMillisFlow = configService.configFlow.map { it.minDelayMillis }.distinctUntilChanged()


    override val screenFlow: Flow<BufferedImage> =
        minDelayMillisFlow
            .flatMapLatest { minDelayMillis ->
                flow<BufferedImage> {
                    while (true) {
                        delay(100.milliseconds)
                        val captured = capture.outputBits
                        val data1: String? = captured.data

                        println(data1 == null)

                        data1?.toByteArray(Charsets.US_ASCII)?.toUByteArray()?.let { data ->
                            logger.info { data1.length }
                            logger.info { data.size }
                            logger.info { data.take(100) }
                        }

                        captured.releaseData()
                    }
                }.flowOn(Dispatchers.IO)
            }
            .share(GlobalScope)

    private val capture = Capture(nativeLogger)

    init {
        capture.init()
    }

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
