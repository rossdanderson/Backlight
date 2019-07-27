package com.github.rossdanderson.backlight.ui.base

import javafx.scene.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.runBlocking
import tornadofx.View

abstract class BaseView(
    title: String? = null,
    icon: Node? = null,
    coroutineScope: CoroutineScope = MainScope()
) : View(title, icon), CoroutineScope by coroutineScope {

    final override fun onDock() = runBlocking {
        onAttach()
    }

    final override fun onUndock() = runBlocking {
        onDetach()
    }

    open suspend fun onAttach() {}

    open suspend fun onDetach() {}
}