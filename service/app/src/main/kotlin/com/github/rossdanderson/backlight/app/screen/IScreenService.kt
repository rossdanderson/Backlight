@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.screen

import kotlinx.coroutines.flow.Flow
import java.awt.image.BufferedImage

interface IScreenService {
    val screenFlow: Flow<BufferedImage>
}
