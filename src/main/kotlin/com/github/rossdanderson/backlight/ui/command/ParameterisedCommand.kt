package com.github.rossdanderson.backlight.ui.command

import javafx.beans.binding.BooleanExpression
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import kotlinx.coroutines.*

class ParameterisedCommand<in T> internal constructor(
    val enabled: BooleanExpression = SimpleBooleanProperty(true),
    private val scope: CoroutineScope,
    private val action: suspend (T) -> Unit
) {
    private val _running = SimpleBooleanProperty(false)
    val running: ReadOnlyBooleanProperty = _running
    val isRunning: Boolean get() = running.value
    val isEnabled: Boolean get() = enabled.value

    private val disabledProperty = enabled.not().or(running)

    fun execute(param: T) {
        if (!isRunning && !disabledProperty.value) {
            scope.launch(Dispatchers.Unconfined) {
                try {
                    _running.value = true
                    action(param)
                } finally {
                    _running.value = false
                }
            }
        }
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
fun <T> CoroutineScope.command(
    enabled: BooleanExpression = SimpleBooleanProperty(true),
    action: suspend (T) -> Unit
): ParameterisedCommand<T> = ParameterisedCommand(enabled, this, action)