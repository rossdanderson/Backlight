@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight

import com.github.rossdanderson.backlight.config.ConfigService
import com.github.rossdanderson.backlight.screen.ScreenService
import com.github.rossdanderson.backlight.serial.SerialService
import com.github.rossdanderson.backlight.serial.mock.MockSerialService
import com.github.rossdanderson.backlight.ui.BacklightApp
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import mu.KotlinLogging
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.dsl.module
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.launch
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger { }

object KoinKotlinLogger : Logger() {
    override fun log(level: Level, msg: MESSAGE) {
        when (level) {
            Level.DEBUG -> logger.debug { msg }
            Level.INFO -> logger.info { msg }
            Level.ERROR -> logger.error { msg }
        }
    }
}

fun main() = runBlocking {
    val scope = this@runBlocking
    val koinApplication = startKoin {

        logger(KoinKotlinLogger)

        environmentProperties()

        modules(
            module {
                single { Json(JsonConfiguration.Default.copy(prettyPrint = true)) }
                single { EventBus<Any>() }
                single { ConfigService(get()).apply { initialise() } }
                single { ScreenService(get()) }
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