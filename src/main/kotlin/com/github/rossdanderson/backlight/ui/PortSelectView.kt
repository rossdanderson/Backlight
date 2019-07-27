package com.github.rossdanderson.backlight.ui

import com.github.rossdanderson.backlight.ofType
import com.github.rossdanderson.backlight.ui.PortSelectViewModel.PortSelectEvent.CloseEvent
import com.github.rossdanderson.backlight.ui.PortSelectViewModel.PortSelectEvent.ConnectionFailedAlertEvent
import com.github.rossdanderson.backlight.ui.base.BaseView
import javafx.geometry.Pos.CENTER
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tornadofx.*

@FlowPreview
@ExperimentalCoroutinesApi
class PortSelectView : BaseView("Port Select") {

    private val portDetailsViewModel by inject<PortSelectViewModel>()

    init {
        launch {
            portDetailsViewModel.receive.ofType<ConnectionFailedAlertEvent>()
                .collect { close() }
        }
        launch {
            portDetailsViewModel.receive.ofType<CloseEvent>().collect { close() }
        }
    }

    override val root =
        stackpane {
            vbox {
                enableWhen(portDetailsViewModel.connectCommand.running.not())

                label("Select a port to use:")
                listview(portDetailsViewModel.ports) {
                    onUserSelect { launch { portDetailsViewModel.connectCommand.execute(it) } }
                }
            }

            vbox(alignment = CENTER) {
                visibleWhen(portDetailsViewModel.connectCommand.running)

                progressindicator()
                label("Connecting...")
            }
        }

    override suspend fun onAttach() {
        portDetailsViewModel.startSubscriptions.execute()
    }

    override suspend fun onDetach() {
        portDetailsViewModel.stopSubscriptions.execute()
    }
}