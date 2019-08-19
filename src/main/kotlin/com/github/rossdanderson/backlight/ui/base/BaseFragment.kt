package com.github.rossdanderson.backlight.ui.base

import javafx.scene.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import tornadofx.Fragment

abstract class BaseFragment(
    title: String? = null,
    icon: Node? = null,
    scope: CoroutineScope = MainScope()
) : Fragment(title, icon), CoroutineScope by scope {

    override fun onUndock() {
        super.onUndock()
        cancel()
    }
}