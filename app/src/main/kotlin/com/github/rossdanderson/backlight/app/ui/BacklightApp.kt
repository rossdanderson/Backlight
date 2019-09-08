@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.ui

import javafx.application.Platform
import javafx.stage.Stage
import tornadofx.App
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime

@ExperimentalTime
class BacklightApp : App(MainView::class) {

    override fun start(stage: Stage) {
        super.start(stage)
    }

    override fun stop() {
        Platform.exit();
        exitProcess(0);
    }
}
