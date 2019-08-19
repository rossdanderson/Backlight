package com.github.rossdanderson.backlight.ui.base

import javafx.scene.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import tornadofx.View

abstract class BaseView(
    title: String? = null,
    icon: Node? = null,
    scope: CoroutineScope = MainScope()
) : View(title, icon), CoroutineScope by scope