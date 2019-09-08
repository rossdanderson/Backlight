@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.ui

import com.github.rossdanderson.backlight.app.ui.base.BaseView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tornadofx.*
import kotlin.time.ExperimentalTime

@ExperimentalTime
class ControlView : BaseView() {

    private val controlViewModel by inject<ControlViewModel>()

    override val root = vbox {
        val saturationLabel = label("Saturation:")
        hbox {
            val saturationSlider = slider(0.0, 10.0) {
                launch { controlViewModel.saturationFlow.collect { value = it } }
                valueProperty().onChange { launch { controlViewModel.updateSaturation(it) } }
            }
            button("Reset") {
                setOnMouseClicked { launch { controlViewModel.updateSaturation(1.0) } }
            }
            saturationLabel.labelFor = saturationSlider
        }

        val contrastLabel = label("Contrast:")
        hbox {
            val contrastSlider = slider(0.0, 10.0) {
                launch { controlViewModel.contrastFlow.collect { value = it } }
                valueProperty().onChange { launch { controlViewModel.updateContrast(it) } }
            }
            button("Reset") {
                setOnMouseClicked { launch { controlViewModel.updateContrast(1.0) } }
            }
            contrastLabel.labelFor = contrastSlider
        }
    }
}
