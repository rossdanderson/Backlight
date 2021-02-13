package com.github.rossdanderson.backlight.app.screen.source.dxgi

import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.data.ImageArray
import com.github.rossdanderson.backlight.app.data.ScreenData
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
import java.nio.file.Paths
import java.time.Instant
import kotlin.math.abs
import kotlin.time.*

private const val success = 0

@OptIn(ExperimentalTime::class, ExperimentalUnsignedTypes::class)
class DXGIScreenService(
    configService: ConfigService,
) : IScreenService {

    private val minDelayMillisFlow = configService.configFlow
        .map { config -> config.minDelayMillis }
        .distinctUntilChanged()
    private val sampleStepFlow = configService.configFlow
        .map { config -> config.sampleStep }
        .distinctUntilChanged()


    override val screenFlow: Flow<ScreenData> =
        flow {
            val capture = Capture()
            val captureLogger = logger.logDurations("Captures", 100)
            emitAll(
                combine(sampleStepFlow, minDelayMillisFlow) { sampleStep, minDelayMillis ->
                    flow {
                        initLoop@ while (true) {

                            val retryDuration = 1.seconds
                            val bufferSizeArray = LongArray(1)
                            if (capture.init(sampleStep, bufferSizeArray) != 0) {
                                logger.warn { "Unable to initialise, retrying in $retryDuration" }
                                delay(retryDuration)
                                continue@initLoop
                            }
                            val arraySize = bufferSizeArray[0]
                            if (arraySize == 0L) {
                                logger.warn { "Invalid array size, retrying in $retryDuration" }
                                delay(retryDuration)
                                continue@initLoop
                            }
                            logger.info { "Using buffer array size: $arraySize" }

                            val dimensions = capture.dimensions

                            val width = abs(dimensions.point1.x - dimensions.point2.x)
                            val height = abs(dimensions.point1.y - dimensions.point2.y)

                            captureLoop@ while (true) {

                                val now = Instant.now()
                                val startTime = System.currentTimeMillis()
                                val array = ByteArray(arraySize.toInt())
                                val (captureResult, captureDuration) = measureTimedValue {
                                    capture.getOutputBits(array, arraySize)
                                }
                                captureLogger(captureDuration)

                                when (captureResult) {
                                    success -> {
                                        emit(ScreenData(now, ImageArray(width, height, array.toUByteArray())))
                                    }
                                    else -> {
                                        logger.warn { "Unable to get capture bits: $captureResult" }
                                        delay(retryDuration)
                                        continue@initLoop
                                    }
                                }

                                val delayDuration =
                                    minDelayMillis.milliseconds - (System.currentTimeMillis() - startTime).milliseconds
                                if (delayDuration > Duration.ZERO) {
                                    delay(delayDuration)
                                }
                            }
                        }
                    }
                        .flowOn(Dispatchers.IO)
                }
                    .flatMapLatest()
            )
        }
            .share(GlobalScope)
            .conflate()

    companion object {
        init {
            System.load(Paths.get("native-libs/dxgi-capture-java.dll").toAbsolutePath().toString())
        }

        private val logger = KotlinLogging.logger { }
    }
}
