@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.screen

import com.github.rossdanderson.backlight.config.ConfigService
import com.github.rossdanderson.backlight.flattenSwitch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.awt.Dimension
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage

class RobotScreenService(
    configService: ConfigService
) : IScreenService {
    private val robot = Robot()

    // TODO Should listen for changes
    private val screenDimensionsFlow: Flow<Dimension> = flowOf(Toolkit.getDefaultToolkit().screenSize)

    private val minDelayMillisFlow = configService.configFlow.map { it.minDelayMillis }.distinctUntilChanged()

    override val screenFlow: Flow<BufferedImage> = minDelayMillisFlow
        .combineLatest(screenDimensionsFlow) { minDelayMillis, dimensions ->
            val screenRect = Rectangle(dimensions)
            flow {
                while (true) {
                    emit(robot.createScreenCapture(screenRect))
                    delay(minDelayMillis)
                }
            }.flowOn(Dispatchers.IO)
        }
        .flattenSwitch()
}
