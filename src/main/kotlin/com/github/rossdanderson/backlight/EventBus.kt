package com.github.rossdanderson.backlight

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.asFlow

@FlowPreview
@ExperimentalCoroutinesApi
class EventBus<T : Any> {
    private val broadcastChannel = BroadcastChannel<T>(BUFFERED)

    val receive = broadcastChannel.asFlow()

    fun fire(any: T) {
        broadcastChannel.offer(any)
    }
}