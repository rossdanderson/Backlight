@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.ui

import com.github.rossdanderson.backlight.ui.base.BaseView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tornadofx.label
import tornadofx.onChange
import tornadofx.slider
import tornadofx.vbox

class ControlView : BaseView() {

    private val controlViewModel by inject<ControlViewModel>()

    override val root = vbox {
        val saturationLabel = label("Saturation:")
        val saturationSlider = slider(0, 255) {
            launch { controlViewModel.saturationFlow.collect { value = it } }
            valueProperty().onChange { launch { controlViewModel.updateSaturation(it) } }
        }
        saturationLabel.labelFor = saturationSlider

        val contrastLabel = label("Contrast:")
        val contrastSlider = slider(0, 255) {
            launch { controlViewModel.contrastFlow.collect { value = it } }
            valueProperty().onChange { launch { controlViewModel.updateContrast(it) } }
        }
        contrastLabel.labelFor = contrastSlider
    }
}