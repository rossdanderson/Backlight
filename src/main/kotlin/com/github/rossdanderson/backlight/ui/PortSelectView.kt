package com.github.rossdanderson.backlight.ui

import com.github.rossdanderson.backlight.ui.PortSelectViewModel.PortSelectEvent.CloseEvent
import com.github.rossdanderson.backlight.ui.PortSelectViewModel.PortSelectEvent.ConnectionFailedAlertEvent
import com.github.rossdanderson.backlight.ui.base.BaseView
import javafx.geometry.Pos.CENTER
import javafx.scene.control.Alert
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import tornadofx.*

@FlowPreview
@ExperimentalCoroutinesApi
class PortSelectView : BaseView("Port Select") {

    private val portDetailsViewModel by inject<PortSelectViewModel>()

    init {
        launch {
            portDetailsViewModel.eventBus.receive.filterIsInstance<ConnectionFailedAlertEvent>()
                .collect { alert(Alert.AlertType.WARNING, "Unable to connect to ${it.portDescriptor}") }
        }
        launch {
            portDetailsViewModel.eventBus.receive.filterIsInstance<CloseEvent>().collect { close() }
        }
    }

    override val root =
        stackpane {
            vbox {
                enableWhen(portDetailsViewModel.connectCommand.running.not())

                label("Select a port to use:")
                listview(portDetailsViewModel.ports) {
                    onUserSelect { portDetailsViewModel.connectCommand.execute(it) }
                }
            }

            vbox(alignment = CENTER) {
                visibleWhen(portDetailsViewModel.connectCommand.running)

                progressindicator()
                label("Connecting...")
            }
        }

    override fun onDock() {
        portDetailsViewModel.startSubscriptions.execute()
    }

    override fun onUndock() {
        portDetailsViewModel.stopSubscriptions.execute()
    }
}