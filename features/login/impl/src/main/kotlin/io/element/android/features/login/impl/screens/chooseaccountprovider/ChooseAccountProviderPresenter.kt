/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.chooseaccountprovider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.config.ElonConfigRepository
import io.element.android.features.login.impl.login.LoginHelper
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Inject
class ChooseAccountProviderPresenter(
    private val elonConfigRepository: ElonConfigRepository,
    private val loginHelper: LoginHelper,
) : Presenter<ChooseAccountProviderState> {
    @Composable
    override fun present(): ChooseAccountProviderState {
        val localCoroutineScope = rememberCoroutineScope()
        val loginMode by loginHelper.collectLoginMode()
        val remoteConfig by elonConfigRepository.config.collectAsState()

        var selectedAccountProvider: AccountProvider? by remember { mutableStateOf(null) }

        fun handleEvent(event: ChooseAccountProviderEvents) {
            when (event) {
                ChooseAccountProviderEvents.Continue -> localCoroutineScope.launch {
                    selectedAccountProvider?.let {
                        loginHelper.submit(
                            isAccountCreation = false,
                            homeserverUrl = it.url,
                            resolvedHomeserverUrl = null,
                            loginHint = null,
                        )
                    }
                }
                is ChooseAccountProviderEvents.SelectAccountProvider -> {
                    // Ensure that the user do not change the server during processing
                    if (loginMode is AsyncData.Uninitialized) {
                        selectedAccountProvider = event.accountProvider
                    }
                }
                ChooseAccountProviderEvents.ClearError -> loginHelper.clearError()
            }
        }

        val dynamicAccountProviderList = remember(remoteConfig) {
            remoteConfig.homeservers
                .sortedByDescending { it.url == remoteConfig.defaultHomeserver }
                .map { homeserver ->
                    AccountProvider(
                        url = homeserver.url,
                        title = if (homeserver.url == "https://electonet.xyz") "ElectoNet" else homeserver.name,
                        subtitle = homeserver.description,
                        isPublic = homeserver.url == "https://electonet.xyz",
                        isMatrixOrg = homeserver.url == "https://electonet.xyz",
                    )
                }
                .toImmutableList()
        }

        return ChooseAccountProviderState(
            accountProviders = dynamicAccountProviderList,
            selectedAccountProvider = selectedAccountProvider,
            loginMode = loginMode,
            eventSink = ::handleEvent,
        )
    }
}
