//@file:Suppress("EXPERIMENTAL_API_USAGE")
//
//package com.github.rossdanderson.backlight.app.ui
//
//import com.github.rossdanderson.backlight.app.config.ConfigService
//import com.github.rossdanderson.backlight.app.led.LEDService
//import com.github.rossdanderson.backlight.app.ui.MainViewModel.MainEvent.ShowPortSelectModalEvent
//import com.github.rossdanderson.backlight.app.ui.base.BaseViewModel
//import kotlinx.coroutines.flow.distinctUntilChanged
//import kotlinx.coroutines.flow.filter
//import kotlinx.coroutines.flow.map
//import kotlin.time.ExperimentalTime
//
//@ExperimentalTime
//class MainViewModel : BaseViewModel() {
//
//    sealed class MainEvent {
//        object ShowPortSelectModalEvent : MainEvent()
//    }
//
//    private val configService by di<ConfigService>()
//    private val ledService by di<LEDService>()
//
//    val showPortSelectEventFlow = configService.configFlow
//        .map { it.defaultPort }
//        .distinctUntilChanged()
//        .filter { it == null }
//        .map { ShowPortSelectModalEvent }
//
//    val ledColorsFlow = ledService.ledColorsFlow
//}
