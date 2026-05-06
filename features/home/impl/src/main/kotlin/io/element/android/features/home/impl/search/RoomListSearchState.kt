/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search

import androidx.compose.foundation.text.input.TextFieldState
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.search.MessageSearchResult
import kotlinx.collections.immutable.ImmutableList

data class RoomListSearchState(
    val isSearchActive: Boolean,
    val query: TextFieldState,
    val selectedTab: RoomListSearchTab,
    val roomResults: ImmutableList<RoomListRoomSummary>,
    val messageResults: ImmutableList<MessageSearchResult>,
    val availableRooms: ImmutableList<RoomListRoomSummary>,
    val selectedMessageRoomId: RoomId?,
    val isMessageSearchLoading: Boolean,
    val hasMessageSearchAttempted: Boolean,
    val eventSink: (RoomListSearchEvent) -> Unit,
)

enum class RoomListSearchTab {
    Rooms,
    Messages,
}
