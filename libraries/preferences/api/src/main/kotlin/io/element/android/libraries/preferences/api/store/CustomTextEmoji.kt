/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

/**
 * Represents a custom text emoji shortcut.
 *
 * @property shortcode The shortcode without colons, e.g. "elyon"
 * @property displayText The emoji or text to display when the shortcode is used, e.g. "🌐"
 */
data class CustomTextEmoji(
    val shortcode: String,
    val displayText: String,
)
