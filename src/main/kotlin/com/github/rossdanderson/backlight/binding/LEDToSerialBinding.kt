@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.binding

import com.github.rossdanderson.backlight.led.LEDService
import com.github.rossdanderson.backlight.messages.WriteAllMessage
import com.github.rossdanderson.backlight.serial.ISerialService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LEDToSerialBinding(
    private val ledService: LEDService,
    private val serialService: ISerialService,
    scope: CoroutineScope
): CoroutineScope by scope {

    fun initialise() {
        launch {
            ledService.ledColorsFlow.collect { serialService.send(WriteAllMessage(it)) }
        }
    }
}