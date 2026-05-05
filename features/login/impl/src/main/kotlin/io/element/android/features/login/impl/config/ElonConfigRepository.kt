/*
 * Copyright (c) 2026 Elyon.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 */

package io.element.android.features.login.impl.config

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.network.RetrofitFactory
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import retrofit2.http.GET
import retrofit2.http.Url
import timber.log.Timber

private val cachedConfigKey = stringPreferencesKey("elyon_remote_config_json")

@SingleIn(AppScope::class)
@Inject
class ElonConfigRepository(
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
    private val retrofitFactory: RetrofitFactory,
    private val jsonProvider: JsonProvider,
) {
    private val dataStore = preferenceDataStoreFactory.create("elyon_config_store")
    private val json by lazy { jsonProvider() }

    private val configApi by lazy {
        retrofitFactory.create("https://electonet.xyz").create(ElonConfigApi::class.java)
    }

    private val _config = MutableStateFlow(defaultConfig)
    val config: StateFlow<ElonConfigModel> = _config.asStateFlow()

    suspend fun refreshConfig() {
        val remoteConfig = remoteConfigUrls.firstNotNullOfOrNull { url ->
            runCatching { configApi.getConfig(url) }
                .onFailure { error ->
                    Timber.w(error, "Failed to load Elyon config from %s", url)
                }
                .getOrNull()
        }

        when {
            remoteConfig != null -> {
                storeConfig(remoteConfig)
                _config.value = remoteConfig
            }
            else -> {
                val cachedConfig = readCachedConfig()
                _config.value = cachedConfig ?: defaultConfig
            }
        }
    }

    private suspend fun readCachedConfig(): ElonConfigModel? {
        val cachedJson = dataStore.data.firstOrNull()?.get(cachedConfigKey) ?: return null
        return runCatching { json.decodeFromString<ElonConfigModel>(cachedJson) }
            .onFailure { error ->
                Timber.w(error, "Failed to parse cached Elyon config")
            }
            .getOrNull()
    }

    private suspend fun storeConfig(config: ElonConfigModel) {
        val encoded = json.encodeToString(config)
        dataStore.edit { preferences ->
            preferences[cachedConfigKey] = encoded
        }
    }

    private interface ElonConfigApi {
        @GET
        suspend fun getConfig(@Url url: String): ElonConfigModel
    }

    private companion object {
        val defaultConfig = ElonConfigModel(
            homeservers = listOf(
                ElonHomeserverModel(
                    name = "ElectoNet",
                    url = "https://electonet.xyz",
                    description = null,
                )
            ),
            defaultHomeserver = "https://electonet.xyz",
        )

        val remoteConfigUrls = listOf(
            "https://electonet.xyz/elyon-config.json",
            FALLBACK_CONFIG_URL,
        )

        const val FALLBACK_CONFIG_URL = "https:///elyon-config.json"
    }
}
