@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app

import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.github.rossdanderson.backlight.app.application.ApplicationState
import com.github.rossdanderson.backlight.app.config.ConfigService
import com.github.rossdanderson.backlight.app.daemon.DaemonJobManager
import com.github.rossdanderson.backlight.app.led.LEDService
import com.github.rossdanderson.backlight.app.screen.IScreenService
import com.github.rossdanderson.backlight.app.screen.source.dxgi.DXGIScreenService
import com.github.rossdanderson.backlight.app.serial.jserialcomm.JSerialCommService
import com.github.rossdanderson.backlight.app.serial.mock.MockSerialService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.dsl.module
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime

const val colorFormatMultiplier = 1.0 / 255.0

private val logger = KotlinLogging.logger { }

val koinKotlinLogger = object : Logger() {
    override fun log(level: Level, msg: MESSAGE) {
        when (level) {
            Level.DEBUG -> logger.debug { msg }
            Level.INFO -> logger.info { msg }
            Level.ERROR -> logger.error { msg }
        }
    }
}

val koin = startKoin {
    logger(koinKotlinLogger)

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
}.koin

val applicationState: ApplicationState = koin.get()

@ExperimentalTime
fun main() = Window(
    events = WindowEvents(
        onMinimize = { applicationState.minimised.value = true },
        onRestore = { applicationState.minimised.value = false },
        onClose = { exitProcess(0) },
    ),
) {
    val coroutineScope = MainScope()
    Row {
        Column {

        }
        Column {
            Row {
                Column {
                    val remember = remember { mutableStateOf(0f) }
                    Text(text = remember.value.toString())
                    Slider(
                        value = remember.value,
                        onValueChange = {
                            remember.value = it
                        },
                        onValueChangeEnd = {

                        }
                    )
                }
            }
        }
    }
}


//@ExperimentalTime
//class MainView {
//
//    override val root = borderpane {
//        center {
//            hbox(5) {
//                paddingTop = 10
//                paddingLeft = 10
//                paddingRight = 10
//                paddingBottom = 10
//                vbox {
//                    val previewSlider = label("Preview:")
//                    rectangle {
//                        width = 512.0
//                        height = 32.0
//                        arcWidth = 15.0
//                        arcHeight = 15.0
//                        vm.ledColorsFlow
//                            .onEach { ledData ->
//                                val colors = ledData.colors
//                                val step = 1.0 / (colors.size - 1)
//                                val stops = colors.mapIndexed { index, color ->
//                                    Stop(
//                                        step * index,
//                                        Color.rgb(
//                                            color.red.toInt(),
//                                            color.green.toInt(),
//                                            color.blue.toInt()
//                                        )
//                                    )
//                                }
//                                fill = LinearGradient(
//                                    0.0,
//                                    0.0,
//                                    this@rectangle.width,
//                                    0.0,
//                                    false,
//                                    CycleMethod.NO_CYCLE,
//                                    stops
//                                )
//                            }
//                            .launchIn(coroutineScope)
//                    }
//                    previewSlider.labelFor = previewSlider
//                }
//            }
//        }
//        right {
//            paddingTop = 10
//            paddingRight = 10
//            this += find<ControlView>()
//        }
//        bottom {
//            vbox {
//                paddingLeft = 10
//                separator()
//                this += find<FooterView>()
//            }
//        }
//    }
//
////    override fun onDock() {
////        vm.showPortSelectEventFlow
////            .onEach { find<PortSelectFragment>().openModal() }
////            .launchIn(coroutineScope)
////    }
//}
