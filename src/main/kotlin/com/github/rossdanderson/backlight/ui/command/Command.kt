package com.github.rossdanderson.backlight.ui.command

import javafx.beans.binding.BooleanExpression
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import kotlinx.coroutines.*

class Command internal constructor(
    val enabled: BooleanExpression = SimpleBooleanProperty(true),
    private val scope: CoroutineScope,
    private val action: suspend () -> Unit
) {
    private val _running = SimpleBooleanProperty(false)
    val running: ReadOnlyBooleanProperty = _running
    val isRunning: Boolean get() = running.value
    val isEnabled: Boolean get() = enabled.value

    private val disabledProperty = enabled.not().or(running)

    fun execute() {
        if (!isRunning && !disabledProperty.value) {
            scope.launch(Dispatchers.Unconfined) {
                try {
                    _running.value = true
                    action()
                } finally {
                    _running.value = false
                }
            }
        }
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
fun CoroutineScope.command(
    enabled: BooleanExpression = SimpleBooleanProperty(true),
    action: suspend () -> Unit
): Command = Command(enabled, this, action)
