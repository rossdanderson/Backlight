package com.github.rossdanderson.backlight.app.ui.base

import javafx.scene.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import tornadofx.Fragment

abstract class BaseFragment(
    title: String? = null,
    icon: Node? = null,
    val coroutineScope: CoroutineScope = MainScope(),
) : Fragment(title, icon) {

    override fun onUndock() {
        super.onUndock()
        coroutineScope.cancel()
    }
}
