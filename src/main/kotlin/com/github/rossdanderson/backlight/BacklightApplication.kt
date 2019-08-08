@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight

import com.github.rossdanderson.backlight.config.ConfigService
import com.github.rossdanderson.backlight.screensample.ScreenSampleService
import com.github.rossdanderson.backlight.serial.SerialService
import com.github.rossdanderson.backlight.serial.mock.MockSerialService
import com.github.rossdanderson.backlight.ui.BacklightApp
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
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
                single { Json(JsonConfiguration.Default.copy(prettyPrint = true)) }
                single { EventBus<Any>() }
                single { ConfigService(get()).apply { initialise() } }
                single { ScreenSampleService(get()) }
                single {
                    if (getPropertyOrNull<String>("mock-serial-connection")?.toBoolean() == true) MockSerialService()
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