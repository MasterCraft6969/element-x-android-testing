/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

/**
 * Store for custom text emoji shortcuts.
 * Persists per-user custom emoji that expand from shortcodes (e.g. :elyon: → 🌐).
 */
interface CustomTextEmojiStore {
    /**
     * Returns a flow of all custom emojis for the current session.
     */
    fun getCustomEmojis(): Flow<ImmutableList<CustomTextEmoji>>

    /**
     * Adds a new custom emoji.
     */
    suspend fun addCustomEmoji(emoji: CustomTextEmoji)

    /**
     * Removes a custom emoji by its shortcode.
     */
    suspend fun removeCustomEmoji(shortcode: String)

    /**
     * Updates an existing custom emoji, identified by oldShortcode.
     */
    suspend fun updateCustomEmoji(oldShortcode: String, emoji: CustomTextEmoji)

    /**
     * Clears all custom emojis.
     */
    suspend fun clear()
}
