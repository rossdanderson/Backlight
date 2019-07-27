package com.github.rossdanderson.backlight.config

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ExperimentalCoroutinesApi
class ConfigService {
    val configFlow: Flow<Config> = flowOf(Config())
}