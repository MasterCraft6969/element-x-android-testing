/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.customemoji

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.preferences.api.store.CustomTextEmoji
import io.element.android.libraries.preferences.api.store.CustomTextEmojiStore
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import javax.inject.Inject

class CustomEmojiPresenter @Inject constructor(
    private val customTextEmojiStore: CustomTextEmojiStore,
) : Presenter<CustomEmojiState> {

    @Composable
    override fun present(): CustomEmojiState {
        val coroutineScope = rememberCoroutineScope()
        
        val customEmojis by customTextEmojiStore.getCustomEmojis().collectAsState(initial = persistentListOf())
        var dialogState by remember { mutableStateOf<CustomEmojiState.DialogState>(CustomEmojiState.DialogState.None) }

        fun handleEvent(event: CustomEmojiEvent) {
            when (event) {
                is CustomEmojiEvent.AddEmoji -> coroutineScope.launch {
                    customTextEmojiStore.addCustomEmoji(CustomTextEmoji(event.shortcode, event.displayText))
                    dialogState = CustomEmojiState.DialogState.None
                }
                is CustomEmojiEvent.RemoveEmoji -> coroutineScope.launch {
                    customTextEmojiStore.removeCustomEmoji(event.shortcode)
                }
                is CustomEmojiEvent.UpdateEmoji -> coroutineScope.launch {
                    customTextEmojiStore.updateCustomEmoji(event.oldShortcode, CustomTextEmoji(event.shortcode, event.displayText))
                    dialogState = CustomEmojiState.DialogState.None
                }
                CustomEmojiEvent.DismissDialog -> {
                    dialogState = CustomEmojiState.DialogState.None
                }
                CustomEmojiEvent.ShowAddDialog -> {
                    dialogState = CustomEmojiState.DialogState.Add
                }
                is CustomEmojiEvent.ShowEditDialog -> {
                    dialogState = CustomEmojiState.DialogState.Edit(event.emoji)
                }
            }
        }

        return CustomEmojiState(
            customEmojis = customEmojis,
            dialogState = dialogState,
            eventSink = ::handleEvent,
        )
    }
}
