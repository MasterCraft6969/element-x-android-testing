/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.customemoji

import io.element.android.libraries.preferences.api.store.CustomTextEmoji
import kotlinx.collections.immutable.ImmutableList

data class CustomEmojiState(
    val customEmojis: ImmutableList<CustomTextEmoji>,
    val dialogState: DialogState,
    val eventSink: (CustomEmojiEvent) -> Unit,
) {
    sealed interface DialogState {
        data object None : DialogState
        data object Add : DialogState
        data class Edit(val emoji: CustomTextEmoji) : DialogState
    }
}
