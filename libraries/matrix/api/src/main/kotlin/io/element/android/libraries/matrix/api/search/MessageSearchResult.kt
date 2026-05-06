/*
 * Copyright (c) 2026 Elyon.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 */

package io.element.android.libraries.matrix.api.search

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId

data class MessageSearchResult(
    val eventId: EventId,
    val roomId: RoomId,
    val roomName: String?,
    val senderId: String,
    val senderName: String?,
    val senderAvatar: String?,
    val snippet: String,
    val timestamp: Long,
)
