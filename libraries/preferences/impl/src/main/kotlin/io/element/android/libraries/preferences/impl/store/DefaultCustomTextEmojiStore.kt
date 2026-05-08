/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 *
 */

package io.element.android.libraries.preferences.impl.store

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.preferences.api.store.CustomTextEmoji
import io.element.android.libraries.preferences.api.store.CustomTextEmojiStore
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultCustomTextEmojiStore @Inject constructor(
    context: Context,
    sessionId: SessionId,
    @SessionCoroutineScope sessionCoroutineScope: CoroutineScope,
) : CustomTextEmojiStore {
    companion object {
        fun storeFile(context: Context, sessionId: SessionId): File {
            val hashedUserId = sessionId.value.hash().take(16)
            return context.preferencesDataStoreFile("custom_emoji_${hashedUserId}_preferences")
        }
        
        private const val SEPARATOR = "||"
    }

    private val customEmojisKey = stringSetPreferencesKey("customEmojis")

    private val dataStoreFile = storeFile(context, sessionId)
    private val store = PreferenceDataStoreFactory.create(
        scope = sessionCoroutineScope,
    ) { dataStoreFile }

    override fun getCustomEmojis(): Flow<ImmutableList<CustomTextEmoji>> {
        return store.data.map { prefs ->
            prefs[customEmojisKey].orEmpty().mapNotNull { entry ->
                val parts = entry.split(SEPARATOR)
                if (parts.size == 2) {
                    CustomTextEmoji(parts[0], parts[1])
                } else {
                    null
                }
            }.sortedBy { it.shortcode }.toImmutableList()
        }
    }

    override suspend fun addCustomEmoji(emoji: CustomTextEmoji) {
        store.edit { prefs ->
            val current = prefs[customEmojisKey].orEmpty().toMutableSet()
            // Remove any existing entry with same shortcode
            current.removeAll { it.startsWith("${emoji.shortcode}$SEPARATOR") }
            current.add("${emoji.shortcode}$SEPARATOR${emoji.displayText}")
            prefs[customEmojisKey] = current
        }
    }

    override suspend fun removeCustomEmoji(shortcode: String) {
        store.edit { prefs ->
            val current = prefs[customEmojisKey].orEmpty().toMutableSet()
            val removed = current.removeAll { it.startsWith("$shortcode$SEPARATOR") }
            if (removed) {
                prefs[customEmojisKey] = current
            }
        }
    }

    override suspend fun updateCustomEmoji(oldShortcode: String, emoji: CustomTextEmoji) {
        store.edit { prefs ->
            val current = prefs[customEmojisKey].orEmpty().toMutableSet()
            current.removeAll { it.startsWith("$oldShortcode$SEPARATOR") }
            current.removeAll { it.startsWith("${emoji.shortcode}$SEPARATOR") }
            current.add("${emoji.shortcode}$SEPARATOR${emoji.displayText}")
            prefs[customEmojisKey] = current
        }
    }

    override suspend fun clear() {
        dataStoreFile.safeDelete()
    }
}
