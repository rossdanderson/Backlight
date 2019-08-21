@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.ui

import com.github.rossdanderson.backlight.ui.base.BaseView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tornadofx.hbox
import tornadofx.text
import tornadofx.vbox

class FooterView : BaseView() {

    private val vm by inject<FooterViewModel>()

    override val root = vbox {
        hbox(5) {
            text {
                launch { vm.connectionStatusFlow.collect { text = it } }
            }
        }
    }
}