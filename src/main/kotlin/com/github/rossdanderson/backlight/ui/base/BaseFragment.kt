package com.github.rossdanderson.backlight.ui.base

import javafx.scene.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.runBlocking
import tornadofx.Fragment

abstract class BaseFragment(
    title: String? = null,
    icon: Node? = null,
    scope: CoroutineScope = MainScope()
) : Fragment(title, icon), CoroutineScope by scope {

    final override fun onDock() = runBlocking {
        onAttach()
    }

    final override fun onUndock() = runBlocking {
        onDetach()
    }

    open suspend fun onAttach() {}

    open suspend fun onDetach() {}
}