package com.github.rossdanderson.backlight.ui.command

import javafx.beans.binding.BooleanExpression
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class Command internal constructor(
    val enabled: BooleanExpression = SimpleBooleanProperty(true),
    private val scope: CoroutineScope,
    private val action: suspend CoroutineScope.() -> Unit
) {
    private val _running = SimpleBooleanProperty(false)
    val running: ReadOnlyBooleanProperty = _running
    val isRunning: Boolean get() = running.value
    val isEnabled: Boolean get() = enabled.value

    private val disabledProperty = enabled.not().or(running)

    suspend fun execute() {
        if (!isRunning && !disabledProperty.value) {
            _running.value = true
            try {
                scope.action()
            } finally {
                _running.value = false
            }
        }
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
fun CoroutineScope.command(
    enabled: BooleanExpression = SimpleBooleanProperty(true),
    action: suspend CoroutineScope.() -> Unit
): Command = Command(enabled, this, action)
