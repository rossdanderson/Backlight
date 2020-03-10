@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.ui

import com.github.rossdanderson.backlight.app.ui.base.BaseView
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tornadofx.hbox
import tornadofx.text
import tornadofx.vbox

class FooterView : BaseView() {

    private val vm by inject<FooterViewModel>()

    override val root = vbox {
        hbox(5) {
            text {
                 vm.connectionStatusFlow.onEach { text = it }.launchIn(coroutineScope)
            }
        }
    }
}
