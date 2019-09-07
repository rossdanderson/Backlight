@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.screen.dxgi

import com.github.rossdanderson.backlight.app.generated.screen.dxgi.Capture
import com.github.rossdanderson.backlight.app.screen.IScreenService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.awt.image.BufferedImage
import java.nio.file.Paths

class DXGIScreenService : IScreenService {
    override val screenFlow: Flow<BufferedImage> = flowOf()

    private val capture = Capture()
    init {
        capture.init()
    }

    companion object {
        init {
            System.load(Paths.get("native-libs/dxgi-capture-java.dll").toAbsolutePath().toString())
        }
    }
}
