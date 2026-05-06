/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.features.home.impl.roomlist.aRoomListRoomSummaryList
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.search.MessageSearchResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class RoomListSearchStateProvider : PreviewParameterProvider<RoomListSearchState> {
    override val values: Sequence<RoomListSearchState>
        get() = sequenceOf(
            aRoomListSearchState(),
            aRoomListSearchState(
                isSearchActive = true,
                query = "Test",
                roomResults = aRoomListRoomSummaryList(),
            ),
            aRoomListSearchState(
                isSearchActive = true,
                query = "hello",
                selectedTab = RoomListSearchTab.Messages,
                messageResults = listOf(
                    MessageSearchResult(
                        eventId = EventId("\$event"),
                        roomId = RoomId("!room:example.org"),
                        roomName = "General",
                        senderId = "@alice:example.org",
                        senderName = "Alice",
                        senderAvatar = null,
                        snippet = "Hello from message search",
                        timestamp = 1_735_987_200_000,
                    )
                ).toImmutableList(),
                hasMessageSearchAttempted = true,
            ),
        )
}

fun aRoomListSearchState(
    isSearchActive: Boolean = false,
    query: String = "",
    selectedTab: RoomListSearchTab = RoomListSearchTab.Rooms,
    roomResults: ImmutableList<RoomListRoomSummary> = persistentListOf(),
    messageResults: ImmutableList<MessageSearchResult> = persistentListOf(),
    availableRooms: ImmutableList<RoomListRoomSummary> = persistentListOf(),
    hasMessageSearchAttempted: Boolean = false,
    eventSink: (RoomListSearchEvent) -> Unit = { },
) = RoomListSearchState(
    isSearchActive = isSearchActive,
    query = TextFieldState(initialText = query),
    selectedTab = selectedTab,
    roomResults = roomResults,
    messageResults = messageResults,
    availableRooms = availableRooms,
    selectedMessageRoomId = null,
    isMessageSearchLoading = false,
    hasMessageSearchAttempted = hasMessageSearchAttempted,
    eventSink = eventSink,
)
