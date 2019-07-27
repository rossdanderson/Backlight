package com.github.rossdanderson.backlight.ui.command

import javafx.beans.binding.BooleanExpression
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class ParameterisedCommand<in T> internal constructor(
    val enabled: BooleanExpression = SimpleBooleanProperty(true),
    private val scope: CoroutineScope,
    private val action: suspend CoroutineScope.(T) -> Unit
) {
    private val _running = SimpleBooleanProperty(false)
    val running: ReadOnlyBooleanProperty = _running
    val isRunning: Boolean get() = running.value
    val isEnabled: Boolean get() = enabled.value

    private val disabledProperty = enabled.not().or(running)

    suspend fun execute(param: T) {
        if (!isRunning && !disabledProperty.value) {
            _running.value = true
            try {
                scope.action(param)
            } finally {
                _running.value = false
            }
        }
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
fun <T> CoroutineScope.command(
    enabled: BooleanExpression = SimpleBooleanProperty(true),
    action: suspend CoroutineScope.(T) -> Unit
): ParameterisedCommand<T> = ParameterisedCommand(enabled, this, action)