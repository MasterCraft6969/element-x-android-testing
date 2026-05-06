/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.home.impl.model.RoomSummaryDisplayType
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.search.MatrixSearchRepository
import io.element.android.libraries.matrix.api.search.MessageSearchResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Inject
class RoomListSearchPresenter(
    private val dataSourceFactory: RoomListSearchDataSource.Factory,
    private val matrixSearchRepository: MatrixSearchRepository,
) : Presenter<RoomListSearchState> {
    @Composable
    override fun present(): RoomListSearchState {
        var isSearchActive by remember {
            mutableStateOf(false)
        }
        val searchQuery = rememberTextFieldState()
        var selectedTab by remember { mutableStateOf(RoomListSearchTab.Rooms) }
        var selectedMessageRoomId by remember { mutableStateOf<RoomId?>(null) }
        var messageResults by remember { mutableStateOf<ImmutableList<MessageSearchResult>>(persistentListOf()) }
        var isMessageSearchLoading by remember { mutableStateOf(false) }

        val coroutineScope = rememberCoroutineScope()
        val dataSource = remember { dataSourceFactory.create(coroutineScope) }

        LaunchedEffect(searchQuery.text) {
            dataSource.setSearchQuery(searchQuery.text.toString())
        }

        LaunchedEffect(searchQuery.text, selectedTab, selectedMessageRoomId) {
            if (selectedTab != RoomListSearchTab.Messages) return@LaunchedEffect

            val query = searchQuery.text.toString().trim()
            if (query.isBlank()) {
                messageResults = persistentListOf()
                isMessageSearchLoading = false
                return@LaunchedEffect
            }

            isMessageSearchLoading = true
            delay(300)
            matrixSearchRepository.searchMessages(query, selectedMessageRoomId)
                .onSuccess { results ->
                    messageResults = results.toImmutableList()
                }
                .onFailure {
                    messageResults = persistentListOf()
                }
            isMessageSearchLoading = false
        }

        fun handleEvent(event: RoomListSearchEvent) {
            when (event) {
                RoomListSearchEvent.ClearQuery -> {
                    searchQuery.clearText()
                }
                RoomListSearchEvent.ToggleSearchVisibility -> {
                    isSearchActive = !isSearchActive
                    searchQuery.clearText()
                    selectedTab = RoomListSearchTab.Rooms
                    selectedMessageRoomId = null
                    messageResults = persistentListOf()
                    isMessageSearchLoading = false
                }
                is RoomListSearchEvent.UpdateVisibleRange -> coroutineScope.launch {
                    dataSource.updateVisibleRange(visibleRange = event.range)
                }
                is RoomListSearchEvent.SelectTab -> {
                    selectedTab = event.tab
                }
                is RoomListSearchEvent.SelectMessageRoomFilter -> {
                    selectedMessageRoomId = event.roomId
                }
            }
        }

        val searchResults by dataSource.roomSummaries.collectAsState(initial = persistentListOf())
        val availableRooms by dataSource.allRoomSummaries.collectAsState(initial = persistentListOf())

        return RoomListSearchState(
            isSearchActive = isSearchActive,
            query = searchQuery,
            selectedTab = selectedTab,
            roomResults = searchResults,
            messageResults = messageResults,
            availableRooms = availableRooms
                .filter { room -> room.displayType == RoomSummaryDisplayType.ROOM && !room.isSpace }
                .toImmutableList(),
            selectedMessageRoomId = selectedMessageRoomId,
            isMessageSearchLoading = isMessageSearchLoading,
            eventSink = ::handleEvent,
        )
    }
}
