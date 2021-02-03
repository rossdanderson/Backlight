//@file:Suppress("EXPERIMENTAL_API_USAGE")
//
//package com.github.rossdanderson.backlight.app.ui
//
//import com.github.rossdanderson.backlight.app.ui.base.BaseFragment
//import javafx.geometry.Pos.CENTER
//import javafx.scene.control.Alert
//import kotlinx.coroutines.flow.launchIn
//import kotlinx.coroutines.flow.onEach
//import kotlinx.coroutines.launch
//import tornadofx.*
//import kotlin.time.ExperimentalTime
//
//@ExperimentalTime
//class PortSelectFragment : BaseFragment("Port Select") {
//
//    private val vm by inject<PortSelectViewModel>()
//
//    override val root =
//        stackpane {
//            vbox {
//                enableWhen(vm.connectCommand.running.not())
//
//                label("Select a port to use:")
//
//                listview<String> {
//                    vm.ports.onEach { items = it.asObservable() }.launchIn(coroutineScope)
//                    onUserSelect {
//                        coroutineScope.launch { vm.connectCommand(it) }
//                    }
//                }
//            }
//
//            vbox(alignment = CENTER) {
//                visibleWhen(vm.connectCommand.running)
//
//                progressindicator()
//                label("Connecting...")
//            }
//        }
//
//    override fun onDock() {
//        vm.connectionFailedAlertEventFlow
//            .onEach { alert(Alert.AlertType.WARNING, "Unable to connect to ${it.portDescriptor}", it.reason) }
//            .launchIn(coroutineScope)
//        vm.closeEventFlow.onEach { close() }.launchIn(coroutineScope)
//    }
//}
