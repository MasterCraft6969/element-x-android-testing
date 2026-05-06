/*
 * Copyright (c) 2026 Elyon.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 */

package io.element.android.libraries.matrix.api.search

import io.element.android.libraries.matrix.api.core.RoomId

interface MatrixSearchRepository {
    suspend fun searchMessages(
        searchTerm: String,
        roomId: RoomId? = null,
    ): Result<List<MessageSearchResult>>
}
