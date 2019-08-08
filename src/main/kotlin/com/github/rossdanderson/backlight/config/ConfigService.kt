@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.config

import arrow.optics.Optional
import arrow.optics.Setter
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.nio.file.Paths

@FlowPreview
@ExperimentalCoroutinesApi
class ConfigService(
    private val json: Json
) {
    private lateinit var config: Config
    private val modifyMutex = Mutex()

    private val configBroadcastChannel = ConflatedBroadcastChannel<Config>()
    val configFlow: Flow<Config> = configBroadcastChannel.asFlow()

    companion object {
        private val logger = KotlinLogging.logger {  }
        private val path = Paths.get("${System.getProperty("user.home")}/.backlight.json").toAbsolutePath()
    }

    fun initialise() {
        runBlocking {
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
                    logger.warn { "Unable to load config from $path - using defaults" }
                    persistAndPublish(Config())
                }
            } else {
                logger.info { "No config found at $path - using defaults" }
                persistAndPublish(Config())
            }
        }
    }

    suspend fun <T> set(setter: Setter<Config, T>, mutator: (T) -> T) {
        modifyMutex.withLock {
            persistAndPublish(setter.modify(config, mutator))
        }
    }

    suspend fun <T> set(setter: Setter<Config, T>, value: T) {
        modifyMutex.withLock {
            persistAndPublish(setter.set(config, value))
        }
    }

    suspend fun <T> set(setter: Optional<Config, T>, value: T) {
        modifyMutex.withLock {
            persistAndPublish(setter.set(config, value))
        }
    }

    suspend fun <T> set(setter: Optional<Config, T>, mutator: (T) -> T) {
        modifyMutex.withLock {
            persistAndPublish(setter.modify(config, mutator))
        }
    }

    private suspend fun persistAndPublish(config: Config) {
        logger.info { "Persisting $config" }
        withContext(Dispatchers.IO) {
            val stringify = json.stringify(Config.serializer(), config)
            path.toFile().bufferedWriter().use {
                it.write(stringify)
            }
        }
        publish(config)
    }

    private suspend fun publish(config: Config) {
        logger.info { "Publishing $config" }
        this.config = config
        configBroadcastChannel.send(config)
    }
}