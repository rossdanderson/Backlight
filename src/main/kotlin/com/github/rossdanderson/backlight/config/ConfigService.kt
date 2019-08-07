package com.github.rossdanderson.backlight.config

import arrow.optics.Lens
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@FlowPreview
@ExperimentalCoroutinesApi
class ConfigService(
    initialConfig: Config
) {
    private var config = initialConfig
    private val modifyMutex = Mutex()

    private val configBroadcastChannel = ConflatedBroadcastChannel(config)
    val configFlow: Flow<Config> = configBroadcastChannel.asFlow()

    /**
     * Applies a single change to the config
     */
    suspend fun <T> set(setter: Lens<Config, T>, mutator: (T) -> T) {
        modifyMutex.withLock {
            config = setter.modify(config, mutator)
            configBroadcastChannel.send(config)
        }
    }

    suspend fun <T> set(setter: Lens<Config, T>, value: T) {
        modifyMutex.withLock {
            config = setter.set(config, value)
            configBroadcastChannel.send(config)
        }
    }
}