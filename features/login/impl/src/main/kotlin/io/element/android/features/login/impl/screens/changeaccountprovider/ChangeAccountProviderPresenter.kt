/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.changeaccountprovider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.api.canConnectToAnyHomeserver
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.changeserver.ChangeServerState
import io.element.android.features.login.impl.config.ElonConfigRepository
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.toImmutableList

@Inject
class ChangeAccountProviderPresenter(
    private val changeServerPresenter: Presenter<ChangeServerState>,
    private val enterpriseService: EnterpriseService,
    private val elonConfigRepository: ElonConfigRepository,
) : Presenter<ChangeAccountProviderState> {
    @Composable
    override fun present(): ChangeAccountProviderState {
        val remoteConfig by elonConfigRepository.config.collectAsState()
        val dynamicAccountProviderList = remember(remoteConfig) {
            remoteConfig.homeservers
                .map { homeserver ->
                    AccountProvider(
                        url = homeserver.url,
                        title = homeserver.name,
                        subtitle = homeserver.description,
                        isPublic = false,
                        isMatrixOrg = false,
                    )
                }
                .toImmutableList()
        }

        val canSearchForAccountProviders = remember {
            enterpriseService.canConnectToAnyHomeserver()
        }

        val changeServerState = changeServerPresenter.present()
        return ChangeAccountProviderState(
            accountProviders = dynamicAccountProviderList,
            canSearchForAccountProviders = canSearchForAccountProviders,
            changeServerState = changeServerState,
        )
    }
}
