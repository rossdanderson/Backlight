@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight

import com.github.rossdanderson.backlight.config.Config
import com.github.rossdanderson.backlight.config.ConfigService
import com.github.rossdanderson.backlight.screensample.ScreenSampleService
import com.github.rossdanderson.backlight.serial.SerialService
import com.github.rossdanderson.backlight.serial.mock.MockSerialService
import com.github.rossdanderson.backlight.ui.BacklightApp
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.dsl.module
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.launch
import kotlin.reflect.KClass

fun main() = runBlocking {
    val scope = this@runBlocking
    val koinApplication = startKoin {
        printLogger()

        environmentProperties()

        modules(
            module {
                single { EventBus<Any>() }
                single { ConfigService(Config()) }
                single { ScreenSampleService(get()) }
                single {
                    if (getProperty<String>("mock-serial-connection").toBoolean()) MockSerialService()
                    else SerialService(scope)
                }
            }
        )
    }

    FX.dicontainer = object : DIContainer {
        override fun <T : Any> getInstance(type: KClass<T>): T =
            koinApplication.koin.get(type, null, null)
    }

    launch<BacklightApp>()
}