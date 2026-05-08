/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.test

import io.element.android.libraries.preferences.api.store.CustomTextEmoji
import io.element.android.libraries.preferences.api.store.CustomTextEmojiStore
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeCustomTextEmojiStore : CustomTextEmojiStore {
    private val emojis = MutableStateFlow<List<CustomTextEmoji>>(emptyList())

    override fun getCustomEmojis(): Flow<ImmutableList<CustomTextEmoji>> {
        val mapped = MutableStateFlow<ImmutableList<CustomTextEmoji>>(kotlinx.collections.immutable.persistentListOf())
        // Since we can't easily map a StateFlow to a StateFlow without a coroutine scope,
        // this is a simplified fake that works by returning a flow that updates when the source updates.
        return kotlinx.coroutines.flow.map { list -> list.toImmutableList() }.apply {
            // Setup flow transformation
            emojis
        }.let {
            kotlinx.coroutines.flow.flow {
                emojis.collect { list ->
                    emit(list.toImmutableList())
                }
            }
        }
    }

    override suspend fun addCustomEmoji(emoji: CustomTextEmoji) {
        emojis.update { list ->
            val filtered = list.filterNot { it.shortcode == emoji.shortcode }
            (filtered + emoji).sortedBy { it.shortcode }
        }
    }

    override suspend fun removeCustomEmoji(shortcode: String) {
        emojis.update { list ->
            list.filterNot { it.shortcode == shortcode }
        }
    }

    override suspend fun updateCustomEmoji(oldShortcode: String, emoji: CustomTextEmoji) {
        emojis.update { list ->
            val filtered = list.filterNot { it.shortcode == oldShortcode || it.shortcode == emoji.shortcode }
            (filtered + emoji).sortedBy { it.shortcode }
        }
    }

    override suspend fun clear() {
        emojis.value = emptyList()
    }
}
