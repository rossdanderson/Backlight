@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.ui

import com.github.rossdanderson.backlight.app.application.ApplicationState
import com.github.rossdanderson.backlight.app.ui.base.BaseView
import javafx.geometry.Insets
import javafx.scene.effect.GaussianBlur
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import tornadofx.*
import kotlin.time.ExperimentalTime

const val colorFormatMultiplier = 1.0 / 255.0

@ExperimentalTime
class MainView : BaseView() {

    private val vm by inject<MainViewModel>()
    private val eventBus by di<ApplicationState>()

    override val root = borderpane {
        prefWidth = 800.0
        prefHeight = 600.0
        center {
            paddingTop = 10
            paddingLeft = 10
            hbox(5) {
                vbox {
                    vbox {
                        background = Background(BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))
                        maxWidth = 512.0
                        imageview {
                            launch {
                                eventBus.minimisedFlow
                                    .flatMapLatest { minimised -> if (minimised) emptyFlow() else vm.imageFlow }
                                    .collect { image = it }
                            }
                            isPreserveRatio = true
                            fitWidth = 512.0
//                            effect = GaussianBlur()
                        }
                    }

                    val previewSlider = label("Preview:")
                    rectangle {
                        width = 512.0
                        height = 32.0
                        effect = GaussianBlur()
                        launch {
                            vm.ledColorsFlow.collect { colors ->
                                val step = 1.0 / (colors.size - 1)
                                val stops = colors.mapIndexed { index, color ->
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
                                fill = LinearGradient(
                                    0.0,
                                    0.0,
                                    this@rectangle.width,
                                    0.0,
                                    false,
                                    CycleMethod.NO_CYCLE,
                                    stops
                                )
                            }
                        }
                    }
                    previewSlider.labelFor = previewSlider
                }

                this += find<ControlView>()
            }
        }
        bottom {
            separator()
            this += find<FooterView>()
        }
    }

    override fun onDock() {
        launch { vm.showPortSelectEventFlow.collect { find<PortSelectFragment>().openModal() } }
    }
}
