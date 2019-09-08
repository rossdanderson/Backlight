@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.screen.source.robot

import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.delay
import com.github.rossdanderson.backlight.app.flatMapLatest
import com.github.rossdanderson.backlight.app.screen.IScreenService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

@ExperimentalTime
class RobotScreenService(
    configService: ConfigService
) : IScreenService {

    private val robot = Robot()

    // TODO Should listen for changes
    private val screenDimensionsFlow: Flow<Rectangle> =
        flowOf(Toolkit.getDefaultToolkit().screenSize).map { Rectangle(it) }

    private val minDelayMillisFlow = configService.configFlow.map { it.minDelayMillis }.distinctUntilChanged()

    override val screenFlow: Flow<BufferedImage> =
        combine(minDelayMillisFlow, screenDimensionsFlow) { minDelayMillis, dimensions ->
            flow {
                while (true) {
                    emit(robot.createScreenCapture(dimensions))
                    delay(minDelayMillis.milliseconds)
                }
            }.flowOn(Dispatchers.IO)
        }.flatMapLatest()
}
