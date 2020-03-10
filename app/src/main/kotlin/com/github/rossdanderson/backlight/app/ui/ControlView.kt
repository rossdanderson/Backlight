@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.ui

import com.github.rossdanderson.backlight.app.ui.base.BaseView
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tornadofx.*
import kotlin.time.ExperimentalTime

@ExperimentalTime
class ControlView : BaseView() {

    private val controlViewModel by inject<ControlViewModel>()

    override val root = vbox {
        val brightnessLabel = label("Brightness:")
        hbox {
            val brightnessSlider = slider(0.0, 10.0) {
                controlViewModel.brightnessFlow.onEach { value = it }.launchIn(coroutineScope)
                valueProperty().onChange { coroutineScope.launch { controlViewModel.updateBrightness(it) } }
            }
            button("Reset") {
                setOnMouseClicked { coroutineScope.launch { controlViewModel.updateBrightness(1.0) } }
            }
            brightnessLabel.labelFor = brightnessSlider
        }

        val sampleStepLabel = label("Sample step:")
        hbox {
            val sampleStepSlider = slider(1, 50) {
                controlViewModel.sampleStepFlow.onEach { value = it.toDouble() }.launchIn(coroutineScope)
                valueProperty().onChange { coroutineScope.launch { controlViewModel.updateSampleStep(it.toInt()) } }
            }
            button("Reset") {
                setOnMouseClicked { coroutineScope.launch { controlViewModel.updateSampleStep(1) } }
            }
            sampleStepLabel.labelFor = sampleStepSlider
        }

        val saturationLabel = label("Saturation:")
        hbox {
            val saturationSlider = slider(0.0, 10.0) {
                controlViewModel.saturationFlow.onEach { value = it }.launchIn(coroutineScope)
                valueProperty().onChange { coroutineScope.launch { controlViewModel.updateSaturation(it) } }
            }
            button("Reset") {
                setOnMouseClicked { coroutineScope.launch { controlViewModel.updateSaturation(1.0) } }
            }
            saturationLabel.labelFor = saturationSlider
        }

        val contrastLabel = label("Contrast:")
        hbox {
            val contrastSlider = slider(0.0, 100.0) {
                controlViewModel.contrastFlow.onEach { value = it }.launchIn(coroutineScope)
                valueProperty().onChange { coroutineScope.launch { controlViewModel.updateContrast(it) } }
            }
            button("Reset") {
                setOnMouseClicked { coroutineScope.launch { controlViewModel.updateContrast(1.0) } }
            }
            contrastLabel.labelFor = contrastSlider
        }
    }
}
