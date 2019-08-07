package com.github.rossdanderson.backlight.ui.base

import com.github.rossdanderson.backlight.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.MainScope
import tornadofx.ViewModel

@FlowPreview
@ExperimentalCoroutinesApi
abstract class BaseViewModel(
    coroutineScope: CoroutineScope = MainScope()
) : ViewModel(), CoroutineScope by coroutineScope {

    val eventBus = EventBus<Any>()
}