@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.application

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow

class ApplicationState {

    private val minimisedBroadcastChannel = ConflatedBroadcastChannel(false)
    val minimisedFlow = minimisedBroadcastChannel.asFlow()

    suspend fun setMinimised(value: Boolean) {
        minimisedBroadcastChannel.send(value)
    }
}
