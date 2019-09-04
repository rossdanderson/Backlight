package com.github.rossdanderson.backlight.app.ui

import com.github.rossdanderson.backlight.app.ui.base.BaseFragment
import javafx.geometry.Pos.CENTER
import javafx.scene.control.Alert
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tornadofx.*

@FlowPreview
@ExperimentalCoroutinesApi
class PortSelectFragment : BaseFragment("Port Select") {

    private val vm by inject<PortSelectViewModel>()

    override val root =
        stackpane {
            vbox {
                enableWhen(vm.connectCommand.running.not())

                label("Select a port to use:")

                listview<String> {
                    launch { vm.ports.collect { items = it.observable() } }
                    onUserSelect {
                        launch { vm.connectCommand(it) }
                    }
                }
            }

            vbox(alignment = CENTER) {
                visibleWhen(vm.connectCommand.running)

                progressindicator()
                label("Connecting...")
            }
        }

    override fun onDock() {
        launch {
            vm.connectionFailedAlertEventFlow
                .collect { alert(Alert.AlertType.WARNING, "Unable to connect to ${it.portDescriptor}", it.reason) }
        }
        launch { vm.closeEventFlow.collect { close() } }
    }
}
