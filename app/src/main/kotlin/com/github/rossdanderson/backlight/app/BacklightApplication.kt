@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app

import com.github.rossdanderson.backlight.app.application.ApplicationState
import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.daemon.DaemonJobManager
import com.github.rossdanderson.backlight.app.led.LEDService
import com.github.rossdanderson.backlight.app.screen.IScreenService
import com.github.rossdanderson.backlight.app.screen.source.dxgi.DXGIScreenService
import com.github.rossdanderson.backlight.app.serial.jserialcomm.JSerialCommService
import com.github.rossdanderson.backlight.app.serial.mock.MockSerialService
import com.github.rossdanderson.backlight.app.ui.MainView
import javafx.application.Platform
import javafx.stage.Stage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.dsl.module
import tornadofx.App
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.launch
import kotlin.reflect.KClass
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime

@ExperimentalTime
class BacklightApp : App(MainView::class) {

    private val coroutineScope = MainScope()

    object KoinKotlinLogger : Logger() {
        override fun log(level: Level, msg: MESSAGE) {
            when (level) {
                Level.DEBUG -> logger.debug { msg }
                Level.INFO -> logger.info { msg }
                Level.ERROR -> logger.error { msg }
            }
        }
    }

    private val applicationState: ApplicationState

    init {
        val koinApplication = startKoin {
            logger(KoinKotlinLogger)

            environmentProperties()

            modules(
                module {
                    single { Json { prettyPrint = true } }
                    single<IScreenService> { DXGIScreenService(get()) }
                    single { ApplicationState() }
                    single { LEDService(get(), get(), get()) }
                    single {
                        if (getPropertyOrNull<String>("mock-serial-connection")?.toBoolean() == true) MockSerialService()
                        else JSerialCommService(GlobalScope)
                    }

                    single(createdAtStart = true) { ConfigService(get()) }
                    single(createdAtStart = true) { DaemonJobManager(get(), get(), get(), GlobalScope) }
                }
            )
        }

        FX.dicontainer = object : DIContainer {
            override fun <T : Any> getInstance(type: KClass<T>): T = koinApplication.koin.get(type, null, null)
        }

        applicationState = koinApplication.koin.get()
    }

    override fun start(stage: Stage) {
        super.start(stage)
        stage.iconifiedProperty().asFlow().onEach { applicationState.minimised.value = it }.launchIn(coroutineScope)
    }

    override fun stop() {
        Platform.exit()
        exitProcess(0)
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}

@ExperimentalTime
fun main() {
    launch<BacklightApp>()
}
