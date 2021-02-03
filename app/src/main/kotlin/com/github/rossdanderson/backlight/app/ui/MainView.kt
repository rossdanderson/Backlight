//@file:Suppress("EXPERIMENTAL_API_USAGE")
//
//package com.github.rossdanderson.backlight.app.ui
//
//import com.github.rossdanderson.backlight.app.ui.base.BaseView
//import javafx.scene.paint.Color
//import javafx.scene.paint.CycleMethod
//import javafx.scene.paint.LinearGradient
//import javafx.scene.paint.Stop
//import kotlinx.coroutines.flow.launchIn
//import kotlinx.coroutines.flow.onEach
//import tornadofx.*
//import kotlin.time.ExperimentalTime
//
//const val colorFormatMultiplier = 1.0 / 255.0
//
//@ExperimentalTime
//class MainView : BaseView("Backlight") {
//
//    private val vm by inject<MainViewModel>()
//
//    override val root = borderpane {
//        center {
//            hbox(5) {
//                paddingTop = 10
//                paddingLeft = 10
//                paddingRight = 10
//                paddingBottom = 10
//                vbox {
//                    val previewSlider = label("Preview:")
//                    rectangle {
//                        width = 512.0
//                        height = 32.0
//                        arcWidth = 15.0
//                        arcHeight = 15.0
//                        vm.ledColorsFlow
//                            .onEach { ledData ->
//                                val colors = ledData.colors
//                                val step = 1.0 / (colors.size - 1)
//                                val stops = colors.mapIndexed { index, color ->
//                                    Stop(
//                                        step * index,
//                                        Color.rgb(
//                                            color.red.toInt(),
//                                            color.green.toInt(),
//                                            color.blue.toInt()
//                                        )
//                                    )
//                                }
//                                fill = LinearGradient(
//                                    0.0,
//                                    0.0,
//                                    this@rectangle.width,
//                                    0.0,
//                                    false,
//                                    CycleMethod.NO_CYCLE,
//                                    stops
//                                )
//                            }
//                            .launchIn(coroutineScope)
//                    }
//                    previewSlider.labelFor = previewSlider
//                }
//            }
//        }
//        right {
//            paddingTop = 10
//            paddingRight = 10
//            this += find<ControlView>()
//        }
//        bottom {
//            vbox {
//                paddingLeft = 10
//                separator()
//                this += find<FooterView>()
//            }
//        }
//    }
//
//    override fun onDock() {
//        vm.showPortSelectEventFlow
//            .onEach { find<PortSelectFragment>().openModal() }
//            .launchIn(coroutineScope)
//    }
//}
