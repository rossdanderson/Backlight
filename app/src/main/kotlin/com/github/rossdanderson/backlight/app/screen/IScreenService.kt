@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.screen

import com.github.rossdanderson.backlight.app.data.ScreenData
import kotlinx.coroutines.flow.Flow

interface IScreenService {
    val screenFlow: Flow<ScreenData>
}
