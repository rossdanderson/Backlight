@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.config

import com.github.rossdanderson.backlight.app.data.Lens
import com.github.rossdanderson.backlight.app.data.Setter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.nio.file.Paths

class ConfigService(
    private val json: Json
) {
    private val modifyMutex = Mutex()

    private val configBroadcastChannel = ConflatedBroadcastChannel<Config>()
    val configFlow: Flow<Config> = configBroadcastChannel.asFlow()

    companion object {
        private val logger = KotlinLogging.logger { }
        private val path = Paths.get("${System.getProperty("user.home")}/.backlight.json").toAbsolutePath()
    }

    suspend fun initialise() {
        val file = path.toFile()
        if (file.exists()) {
            runCatching {
                file.bufferedReader().use {
                    val text = it.readText()
                    val config = json.parse(Config.serializer(), text)
                    logger.info { "Loaded $config" }
                    publish(config)
                }
            }.recover {
                logger.warn(it) { "Unable to load config from $path - using defaults" }
                persistAndPublish(Config())
            }
        } else {
            logger.info { "No config found at $path - using defaults" }
            persistAndPublish(Config())
        }
    }

    suspend fun <T> modify(lens: Lens<Config, T>, mutator: (T) -> T) {
        modify(lens.asSetter(), mutator)
    }

    suspend fun <T> modify(setter: Setter<Config, T>, mutator: (T) -> T) {
        modifyMutex.withLock {
            val oldConfig = configFlow.first()
            val newConfig = setter.modify(oldConfig, mutator)
            if (oldConfig != newConfig) persistAndPublish(newConfig)
        }
    }

    suspend fun <T> set(lens: Lens<Config, T>, value: T) {
        set(lens.asSetter(), value)
    }

    suspend fun <T> set(setter: Setter<Config, T>, value: T) {
        modifyMutex.withLock {
            val oldConfig = configFlow.first()
            val newConfig = setter.set(oldConfig, value)
            if (oldConfig != newConfig) persistAndPublish(newConfig)
        }
    }

    private suspend fun persistAndPublish(config: Config) {
        withContext(NonCancellable) {
            logger.info { "Persisting $config" }
            val serializedConfig = json.stringify(Config.serializer(), config)
            withContext(Dispatchers.IO) {
                path.toFile().bufferedWriter().use { it.write(serializedConfig) }
            }
            publish(config)
        }
    }

    private suspend fun publish(config: Config) {
        withContext(NonCancellable) {
            logger.info { "Publishing $config" }
            configBroadcastChannel.send(config)
        }
    }
}
