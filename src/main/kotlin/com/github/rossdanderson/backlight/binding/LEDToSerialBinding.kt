package com.github.rossdanderson.backlight.binding

import com.github.rossdanderson.backlight.led.LEDService
import com.github.rossdanderson.backlight.serial.ISerialService
import kotlinx.coroutines.CoroutineScope

class LEDToSerialBinding(
    private val ledService: LEDService,
    private val serialService: ISerialService,
    private val scope: CoroutineScope
) {
}