package com.github.rossdanderson.backlight.ui

import com.github.rossdanderson.backlight.ui.MainViewModel.MainEvent.LEDUpdateEvent
import com.github.rossdanderson.backlight.ui.MainViewModel.MainEvent.ShowPortSelectModalEvent
import com.github.rossdanderson.backlight.ui.base.BaseView
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import javafx.scene.shape.Rectangle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tornadofx.*

const val colorFormatMultiplier = 1.0 / 255.0

@FlowPreview
@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
class MainView : BaseView() {

    private val mainViewModel by inject<MainViewModel>()

    private lateinit var ledGradientStrip: Rectangle

    init {
        launch {
            mainViewModel.eventBus.receive
                .filterIsInstance<ShowPortSelectModalEvent>()
                .collect { find<PortSelectView>().openModal() }
        }
        launch {
            mainViewModel.eventBus.receive
                .filterIsInstance<LEDUpdateEvent>()
                .map { it.colors }
                .collect { colors ->
                    val step = 1.0 / (colors.size - 1)
                    ledGradientStrip.fill = LinearGradient(
                        0.0,
                        0.0,
                        800.0,
                        0.0,
                        false,
                        CycleMethod.NO_CYCLE,
                        colors.mapIndexed { index, color ->
                            Stop(
                                step * index,
                                Color(
                                    color.red.toDouble() * colorFormatMultiplier,
                                    color.green.toDouble() * colorFormatMultiplier,
                                    color.blue.toDouble() * colorFormatMultiplier,
                                    1.0
                                )
                            )
                        }
                    )
                }
        }
    }

    override val root = borderpane {
        prefWidth = 800.0
        prefHeight = 600.0
        center {
            vbox(5) {
                imageview(mainViewModel.image) {
                    isPreserveRatio = true
                    fitWidth = 800.0
                    fitHeight = 600.0
                }
                val previewSlider = label("Preview:")
                ledGradientStrip = rectangle {
                    width = 800.0
                    height = 32.0
                }
                previewSlider.labelFor = previewSlider

                val saturationLabel = label("Saturation:")
                val slider = slider(0, 255) {
                    bind(mainViewModel.saturation)

                    valueProperty().onChange {
                        launch {
                            mainViewModel.updateSaturation.execute(it)
                        }
                    }
//                    this.on
//                    this.setOnDragDone {
//                        mainViewModel.updateSaturation.execute(it.)
//                    }
                }
                saturationLabel.labelFor = slider
            }
        }
        bottom {
            vbox {
                separator()
                hbox(5) {
                    text(mainViewModel.connectionStatus)
                }
            }
        }
    }

    override fun onDock() {
        mainViewModel.startSubscriptions.execute()
    }

    override fun onUndock() {
        mainViewModel.stopSubscriptions.execute()
    }
}