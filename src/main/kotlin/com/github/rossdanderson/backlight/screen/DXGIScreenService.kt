@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.screen

import kotlinx.coroutines.flow.Flow
import java.awt.image.BufferedImage

class DXGIScreenService : IScreenService {
    override val screenFlow: Flow<BufferedImage>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}