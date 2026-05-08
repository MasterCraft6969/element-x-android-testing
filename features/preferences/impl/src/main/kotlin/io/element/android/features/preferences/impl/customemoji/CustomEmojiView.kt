/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.customemoji

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.preferences.api.store.CustomTextEmoji

@Composable
fun CustomEmojiView(
    state: CustomEmojiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = stringResource(id = R.string.screen_custom_emoji_title)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.customEmojis.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.screen_custom_emoji_empty),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.customEmojis) { emoji ->
                        CustomEmojiItem(
                            emoji = emoji,
                            onDelete = { state.eventSink(CustomEmojiEvent.RemoveEmoji(it.shortcode)) },
                            onClick = { state.eventSink(CustomEmojiEvent.ShowEditDialog(it)) },
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { state.eventSink(CustomEmojiEvent.ShowAddDialog) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.screen_custom_emoji_add_title))
            }
        }
    }

    if (state.dialogState is CustomEmojiState.DialogState.Add) {
        CustomEmojiDialog(
            isEdit = false,
            initialShortcode = "",
            initialDisplayText = "",
            onConfirm = { shortcode, displayText ->
                state.eventSink(CustomEmojiEvent.AddEmoji(shortcode, displayText))
            },
            onDismiss = { state.eventSink(CustomEmojiEvent.DismissDialog) }
        )
    } else if (state.dialogState is CustomEmojiState.DialogState.Edit) {
        CustomEmojiDialog(
            isEdit = true,
            initialShortcode = state.dialogState.emoji.shortcode,
            initialDisplayText = state.dialogState.emoji.displayText,
            onConfirm = { shortcode, displayText ->
                state.eventSink(CustomEmojiEvent.UpdateEmoji(state.dialogState.emoji.shortcode, shortcode, displayText))
            },
            onDismiss = { state.eventSink(CustomEmojiEvent.DismissDialog) }
        )
    }
}

@Composable
private fun CustomEmojiItem(
    emoji: CustomTextEmoji,
    onDelete: (CustomTextEmoji) -> Unit,
    onClick: (CustomTextEmoji) -> Unit,
) {
    ListItem(
        headlineContent = { Text(":${emoji.shortcode}:") },
        supportingContent = { Text(emoji.displayText) },
        trailingContent = ListItemContent.Custom {
            IconButton(onClick = { onDelete(emoji) }) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.screen_custom_emoji_delete_confirmation))
            }
        },
        onClick = { onClick(emoji) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomEmojiDialog(
    isEdit: Boolean,
    initialShortcode: String,
    initialDisplayText: String,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var shortcode by remember { mutableStateOf(initialShortcode) }
    var displayText by remember { mutableStateOf(initialDisplayText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isEdit) stringResource(id = R.string.screen_custom_emoji_edit_title)
                else stringResource(id = R.string.screen_custom_emoji_add_title)
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = shortcode,
                    onValueChange = { shortcode = it },
                    label = { Text(stringResource(id = R.string.screen_custom_emoji_shortcode_label)) },
                    placeholder = { Text(stringResource(id = R.string.screen_custom_emoji_shortcode_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = displayText,
                    onValueChange = { displayText = it },
                    label = { Text(stringResource(id = R.string.screen_custom_emoji_display_text_label)) },
                    placeholder = { Text(stringResource(id = R.string.screen_custom_emoji_display_text_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(shortcode.trim(), displayText.trim()) },
                enabled = shortcode.isNotBlank() && displayText.isNotBlank()
            ) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = android.R.string.cancel))
            }
        }
    )
}

@PreviewsDayNight
@Composable
internal fun CustomEmojiViewPreview(@PreviewParameter(CustomEmojiStateProvider::class) state: CustomEmojiState) = ElementPreview {
    CustomEmojiView(
        state = state,
        onBackClick = {},
    )
}
