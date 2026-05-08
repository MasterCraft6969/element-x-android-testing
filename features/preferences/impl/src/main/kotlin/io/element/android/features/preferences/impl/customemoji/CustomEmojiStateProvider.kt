/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.customemoji

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.preferences.api.store.CustomTextEmoji
import kotlinx.collections.immutable.persistentListOf

open class CustomEmojiStateProvider : PreviewParameterProvider<CustomEmojiState> {
    override val values: Sequence<CustomEmojiState>
        get() = sequenceOf(
            aCustomEmojiState(),
            aCustomEmojiState(
                customEmojis = persistentListOf(
                    CustomTextEmoji("elyon", "🌐"),
                    CustomTextEmoji("smile", "😊"),
                    CustomTextEmoji("party", "🎉"),
                )
            ),
            aCustomEmojiState(dialogState = CustomEmojiState.DialogState.Add),
            aCustomEmojiState(dialogState = CustomEmojiState.DialogState.Edit(CustomTextEmoji("elyon", "🌐"))),
        )
}

fun aCustomEmojiState(
    customEmojis: kotlinx.collections.immutable.ImmutableList<CustomTextEmoji> = persistentListOf(),
    dialogState: CustomEmojiState.DialogState = CustomEmojiState.DialogState.None,
    eventSink: (CustomEmojiEvent) -> Unit = {},
) = CustomEmojiState(
    customEmojis = customEmojis,
    dialogState = dialogState,
    eventSink = eventSink,
)
