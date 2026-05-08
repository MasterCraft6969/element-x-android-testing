/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.customemoji

import io.element.android.libraries.preferences.api.store.CustomTextEmoji

sealed interface CustomEmojiEvent {
    data class AddEmoji(val shortcode: String, val displayText: String) : CustomEmojiEvent
    data class RemoveEmoji(val shortcode: String) : CustomEmojiEvent
    data class UpdateEmoji(val oldShortcode: String, val shortcode: String, val displayText: String) : CustomEmojiEvent
    data object DismissDialog : CustomEmojiEvent
    data object ShowAddDialog : CustomEmojiEvent
    data class ShowEditDialog(val emoji: CustomTextEmoji) : CustomEmojiEvent
}
