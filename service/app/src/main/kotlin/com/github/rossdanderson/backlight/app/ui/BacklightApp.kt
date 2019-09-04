@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import tornadofx.App

class BacklightApp : App(MainView::class), CoroutineScope by MainScope()
