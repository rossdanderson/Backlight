package com.github.rossdanderson.backlight.ui

import com.github.rossdanderson.backlight.ui.base.BaseView
import javafx.geometry.Orientation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import tornadofx.*

@FlowPreview
@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
class MainView : BaseView() {

    private val mainViewModel by inject<MainViewModel>()

    override val root = borderpane {
        top {
            button {
                setOnMouseClicked {
                    find<PortSelectView>().openModal()
                }
            }
        }
        center {
            vbox {
                imageview(mainViewModel.image) {
                    isPreserveRatio = true
                    fitWidth = 800.0
                    fitHeight = 600.0
                }
            }
        }
        bottom {
            hbox(5) {

                text(mainViewModel.connectionStatus)

                separator(Orientation.VERTICAL)
                label("Port: ")
                text("-")
            }
        }
    }

    override suspend fun onAttach() {
        mainViewModel.startSubscriptions.execute()
    }


    override suspend fun onDetach() {
        mainViewModel.stopSubscriptions.execute()
    }
}