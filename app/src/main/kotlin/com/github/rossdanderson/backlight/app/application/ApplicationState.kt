@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.application

import kotlinx.coroutines.flow.MutableStateFlow

class ApplicationState {

    val minimised = MutableStateFlow(false)
}
