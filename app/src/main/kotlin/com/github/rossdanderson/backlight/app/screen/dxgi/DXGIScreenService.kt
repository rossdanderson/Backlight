@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.screen.dxgi

import com.github.rossdanderson.backlight.app.generated.screen.dxgi.Capture
import com.github.rossdanderson.backlight.app.screen.IScreenService
import kotlinx.coroutines.flow.Flow
import java.awt.image.BufferedImage

class DXGIScreenService : IScreenService {
    override val screenFlow: Flow<BufferedImage>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    private val capture = Capture()


}
