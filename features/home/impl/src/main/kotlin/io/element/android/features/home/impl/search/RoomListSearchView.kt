/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.home.impl.R
import io.element.android.features.home.impl.components.RoomSummaryRow
import io.element.android.features.home.impl.contentType
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.features.home.impl.roomlist.RoomListEvent
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.FilledTextField
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.OnVisibleRangeChangeEffect
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.search.MessageSearchResult
import io.element.android.libraries.ui.strings.CommonStrings
import java.text.DateFormat
import java.util.Date

@Composable
internal fun RoomListSearchView(
    state: RoomListSearchState,
    hideInvitesAvatars: Boolean,
    eventSink: (RoomListEvent) -> Unit,
    onRoomClick: (RoomId) -> Unit,
    onMessageClick: (RoomId, EventId) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = state.isSearchActive) {
        state.eventSink(RoomListSearchEvent.ToggleSearchVisibility)
    }

    AnimatedVisibility(
        visible = state.isSearchActive,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(modifier = modifier) {
            RoomListSearchContent(
                state = state,
                hideInvitesAvatars = hideInvitesAvatars,
                onRoomClick = onRoomClick,
                onMessageClick = onMessageClick,
                eventSink = eventSink,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomListSearchContent(
    state: RoomListSearchState,
    hideInvitesAvatars: Boolean,
    eventSink: (RoomListEvent) -> Unit,
    onRoomClick: (RoomId) -> Unit,
    onMessageClick: (RoomId, EventId) -> Unit,
) {
    val borderColor = MaterialTheme.colorScheme.tertiary
    val strokeWidth = 1.dp

    fun onBackButtonClick() {
        state.eventSink(RoomListSearchEvent.ToggleSearchVisibility)
    }

    fun onRoomClick(room: RoomListRoomSummary) {
        onRoomClick(room.roomId)
    }

    fun onMessageClick(result: MessageSearchResult) {
        onMessageClick(result.roomId, result.eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = strokeWidth.value,
                    )
                },
                navigationIcon = { BackButton(onClick = ::onBackButtonClick) },
                title = {
                    val focusRequester = remember { FocusRequester() }
                    FilledTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        state = state.query,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent,
                        ),
                        trailingIcon = if (state.query.text.isNotEmpty()) {
                            @Composable {
                                IconButton(onClick = { state.eventSink(RoomListSearchEvent.ClearQuery) }) {
                                    Icon(
                                        imageVector = CompoundIcons.Close(),
                                        contentDescription = stringResource(CommonStrings.action_cancel),
                                    )
                                }
                            }
                        } else {
                            null
                        },
                    )

                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        if (!focusRequester.restoreFocusedChild()) {
                            focusRequester.requestFocus()
                        }
                        focusRequester.saveFocusedChild()
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            SearchTabs(
                selectedTab = state.selectedTab,
                onSelectTab = { state.eventSink(RoomListSearchEvent.SelectTab(it)) },
            )

            if (state.selectedTab == RoomListSearchTab.Messages) {
                MessageRoomFilter(
                    rooms = state.availableRooms,
                    selectedRoomId = state.selectedMessageRoomId,
                    onSelect = { state.eventSink(RoomListSearchEvent.SelectMessageRoomFilter(it)) },
                )
            }

            when (state.selectedTab) {
                RoomListSearchTab.Rooms -> {
                    val lazyListState = rememberLazyListState()
                    OnVisibleRangeChangeEffect(lazyListState) { visibleRange ->
                        state.eventSink(RoomListSearchEvent.UpdateVisibleRange(visibleRange))
                    }
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.weight(1f),
                    ) {
                        items(
                            items = state.roomResults,
                            contentType = { room -> room.contentType() },
                        ) { room ->
                            RoomSummaryRow(
                                room = room,
                                hideInviteAvatars = hideInvitesAvatars,
                                isInviteSeen = false,
                                onClick = ::onRoomClick,
                                eventSink = eventSink,
                            )
                        }
                    }
                }
                RoomListSearchTab.Messages -> {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(
                            items = state.messageResults,
                            key = { result -> result.eventId.value },
                        ) { result ->
                            MessageSearchResultRow(
                                result = result,
                                timestamp = formatTimestamp(result.timestamp),
                                onClick = { onMessageClick(result) },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(timestamp))
}

@Composable
private fun SearchTabs(
    selectedTab: RoomListSearchTab,
    onSelectTab: (RoomListSearchTab) -> Unit,
) {
    ScrollableTabRow(selectedTabIndex = selectedTab.ordinal) {
        RoomListSearchTab.entries.forEach { tab ->
            val title = when (tab) {
                RoomListSearchTab.Rooms -> stringResource(R.string.screen_search_tab_rooms)
                RoomListSearchTab.Messages -> stringResource(R.string.screen_search_tab_messages)
            }
            Tab(
                selected = tab == selectedTab,
                onClick = { onSelectTab(tab) },
                text = { Text(text = title) },
            )
        }
    }
}

@Composable
private fun MessageRoomFilter(
    rooms: List<RoomListRoomSummary>,
    selectedRoomId: RoomId?,
    onSelect: (RoomId?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = rooms.firstOrNull { it.roomId == selectedRoomId }?.name
        ?: stringResource(R.string.screen_search_all_rooms)

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.screen_search_in_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = selectedLabel,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.screen_search_all_rooms)) },
                onClick = {
                    expanded = false
                    onSelect(null)
                },
            )
            rooms.forEach { room ->
                DropdownMenuItem(
                    text = { Text(text = room.name ?: room.roomId.value, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    onClick = {
                        expanded = false
                        onSelect(room.roomId)
                    },
                )
            }
        }
    }
}

@Composable
private fun MessageSearchResultRow(
    result: MessageSearchResult,
    timestamp: String,
    onClick: () -> Unit,
) {
    val avatarData = AvatarData(
        id = result.senderId,
        name = result.senderName,
        url = result.senderAvatar,
        size = AvatarSize.UserListItem,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Avatar(
            avatarData = avatarData,
            avatarType = AvatarType.User,
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = result.senderName ?: result.senderId,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = result.roomName ?: result.roomId.value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = result.snippet,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun RoomListSearchContentPreview(@PreviewParameter(RoomListSearchStateProvider::class) state: RoomListSearchState) = ElementPreview {
    RoomListSearchContent(
        state = state,
        hideInvitesAvatars = false,
        onRoomClick = {},
        onMessageClick = { _, _ -> },
        eventSink = {},
    )
}
