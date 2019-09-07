@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.screen.dxgi

import com.github.rossdanderson.backlight.app.generated.screen.dxgi.Capture
import com.github.rossdanderson.backlight.app.generated.screen.dxgi.Logger
import com.github.rossdanderson.backlight.app.screen.IScreenService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import mu.KotlinLogging
import java.awt.image.BufferedImage
import java.nio.file.Paths

class DXGIScreenService : IScreenService {
    override val screenFlow: Flow<BufferedImage> = flowOf()

    private val capture = Capture(object : Logger() {
        override fun info(message: String) {
            logger.info(message)
        }

        override fun warn(message: String) {
            logger.warn(message)
        }

        override fun error(message: String) {
            logger.error(message)
        }
    })

    init {
        capture.init()
    }

    companion object {
        init {
            System.load(Paths.get("native-libs/dxgi-capture-java.dll").toAbsolutePath().toString())
        }

        private val logger = KotlinLogging.logger { }
    }
}
