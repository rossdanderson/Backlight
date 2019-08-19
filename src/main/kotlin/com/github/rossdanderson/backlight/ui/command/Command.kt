package com.github.rossdanderson.backlight.ui.command

import javafx.beans.binding.BooleanExpression
import javafx.beans.property.SimpleBooleanProperty
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import java.time.Duration

interface ICommand {
    val enabled: BooleanExpression
    val running: BooleanExpression
}

class Command(
    private val parameterisedCommand: ParameterisedCommand<Unit>
) : ICommand by parameterisedCommand {
    suspend operator fun invoke() = parameterisedCommand.invoke(Unit)
}

class ParameterisedCommand<in T> internal constructor(
    override val enabled: BooleanExpression,
    private val debounce: Duration?,
    private val disableWhileRunning: Boolean,
    private val action: suspend CoroutineScope.(T) -> Unit
) : ICommand {
    private var debounceJob: Job? = null
    private val _running = SimpleBooleanProperty(false)
    override val running: BooleanExpression = _running

    suspend operator fun invoke(param: T): Unit = coroutineScope {
        if (enabled.value && !(running.value && disableWhileRunning)) {
            debounceJob?.cancel()

            debounceJob = launch(Default) {
                _running.value = true
                if (debounce != null) delay(debounce.toMillis())
                try {
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
fun <T> command(
    enabled: BooleanExpression = SimpleBooleanProperty(true),
    debounce: Duration? = null,
    disableWhileRunning: Boolean = false,
    action: suspend CoroutineScope.(T) -> Unit
): ParameterisedCommand<T> = ParameterisedCommand(enabled, debounce, disableWhileRunning, action)

@ExperimentalCoroutinesApi
@FlowPreview
fun command(
    enabled: BooleanExpression = SimpleBooleanProperty(true),
    debounce: Duration? = null,
    disableWhileRunning: Boolean = false,
    action: suspend CoroutineScope.() -> Unit
): Command = Command(command<Unit>(enabled, debounce, disableWhileRunning) { action() })
