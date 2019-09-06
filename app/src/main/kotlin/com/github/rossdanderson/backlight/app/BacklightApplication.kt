@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app

import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.daemon.DaemonJobManager
import com.github.rossdanderson.backlight.app.led.LEDService
import com.github.rossdanderson.backlight.app.screen.dxgi.DXGIScreenService
import com.github.rossdanderson.backlight.app.screen.robot.RobotScreenService
import com.github.rossdanderson.backlight.app.serial.jserialcomm.JSerialCommService
import com.github.rossdanderson.backlight.app.serial.mock.MockSerialService
import com.github.rossdanderson.backlight.app.ui.BacklightApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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

fun main() {
    runBlocking(Dispatchers.Default) {
        val scope = this@runBlocking

        val koinApplication = startKoin {

            logger(KoinKotlinLogger)

            environmentProperties()

            modules(
                module {
                    single { Json(JsonConfiguration.Default.copy(prettyPrint = true)) }
                    single {
                        ConfigService(get())
                            .apply { runBlocking { initialise() } }
                    }
                    single {
                        if (getPropertyOrNull<String>("legacy-screen-capture")?.toBoolean() == true) RobotScreenService(get())
                        else DXGIScreenService()
                    }
                    single { LEDService(get(), get(), get()) }
                    single {
                        if (getPropertyOrNull<String>("mock-serial-connection")?.toBoolean() == true) MockSerialService()
                        else JSerialCommService(scope)
                    }

                    single(createdAtStart = true) {
                        DaemonJobManager(get(), get(), get(), GlobalScope)
                            .apply { initialise() }
                    }
                }
            )
        }

        val koin = koinApplication.koin

        FX.dicontainer = object : DIContainer {
            override fun <T : Any> getInstance(type: KClass<T>): T = koin.get(type, null, null)
        }

        // This blocks for the duration
        launch<BacklightApp>()
    }
}
