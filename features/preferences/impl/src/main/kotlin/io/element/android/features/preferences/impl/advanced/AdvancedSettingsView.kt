/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.components.dialogs.ListDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceDropdown
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementPreviewBlack
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewWithLargeHeight
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListSectionHeader
import io.element.android.libraries.designsystem.theme.components.ListSupportingText
import io.element.android.libraries.designsystem.theme.components.ListSupportingTextDefaults
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.utils.snackbar.LocalSnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.analytics.compose.LocalAnalyticsService
import io.element.android.services.analyticsproviders.api.trackers.captureInteraction
import io.element.android.compound.tokens.generated.CompoundIcons

@Composable
fun AdvancedSettingsView(
    state: AdvancedSettingsState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val analyticsService = LocalAnalyticsService.current

    val snackbarDispatcher = LocalSnackbarDispatcher.current
    val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = snackbarMessage)

    var primaryColor by remember(state.connectionLightSettings.primaryColorArgb) { mutableStateOf(Color(state.connectionLightSettings.primaryColorArgb)) }
    var secondaryColor by remember(state.connectionLightSettings.secondaryColorArgb) { mutableStateOf(Color(state.connectionLightSettings.secondaryColorArgb)) }
    var gradientEnabled by remember(state.connectionLightSettings.gradientEnabled) { mutableStateOf(state.connectionLightSettings.gradientEnabled) }

    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = stringResource(id = CommonStrings.common_advanced_settings),
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) {
        PreferenceDropdown(
            title = stringResource(id = CommonStrings.common_appearance),
            selectedOption = state.theme,
            options = state.availableThemeOptions,
            onSelectOption = { themeOption ->
                state.eventSink(AdvancedSettingsEvents.SetTheme(themeOption))
            }
        )
        ListItem(
            headlineContent = {
                Text(text = stringResource(id = CommonStrings.action_view_source))
            },
            supportingContent = {
                Text(text = stringResource(id = R.string.screen_advanced_settings_view_source_description))
            },
            trailingContent = ListItemContent.Switch(
                checked = state.isDeveloperModeEnabled,
            ),
            onClick = { state.eventSink(AdvancedSettingsEvents.SetDeveloperModeEnabled(!state.isDeveloperModeEnabled)) }
        )
        ConnectionLightSettingsSection(
            primaryColor = primaryColor,
            onPrimaryColorChange = { primaryColor = it },
            secondaryColor = secondaryColor,
            onSecondaryColorChange = { secondaryColor = it },
            gradientEnabled = gradientEnabled,
            onGradientEnabledChange = { gradientEnabled = it },
            onApply = {
                state.eventSink(
                    AdvancedSettingsEvents.SetConnectionLightSettings(
                        primaryColorArgb = primaryColor.toArgb(),
                        secondaryColorArgb = secondaryColor.toArgb(),
                        gradientEnabled = gradientEnabled,
                    )
                )
            },
        )
        ListItem(
            headlineContent = {
                Text(text = stringResource(id = R.string.screen_advanced_settings_share_presence))
            },
            supportingContent = {
                Text(text = stringResource(id = R.string.screen_advanced_settings_share_presence_description))
            },
            trailingContent = ListItemContent.Switch(
                checked = state.isSharePresenceEnabled,
            ),
            onClick = { state.eventSink(AdvancedSettingsEvents.SetSharePresenceEnabled(!state.isSharePresenceEnabled)) }
        )
        val compressImages = state.mediaOptimizationState?.shouldCompressImages

        when (state.mediaOptimizationState) {
            null -> Unit
            is MediaOptimizationState.AllMedia -> {
                ListItem(
                    headlineContent = {
                        Text(text = stringResource(id = R.string.screen_advanced_settings_media_compression_title))
                    },
                    supportingContent = {
                        Text(text = stringResource(id = R.string.screen_advanced_settings_media_compression_description))
                    },
                    trailingContent = ListItemContent.Switch(
                        checked = compressImages ?: false,
                    ),
                    onClick = {
                        val newValue = !(compressImages ?: false)
                        analyticsService.captureInteraction(
                            if (newValue) {
                                Interaction.Name.MobileSettingsOptimizeMediaUploadsEnabled
                            } else {
                                Interaction.Name.MobileSettingsOptimizeMediaUploadsDisabled
                            }
                        )
                        state.eventSink(AdvancedSettingsEvents.SetCompressMedia(newValue))
                    }
                )
            }
            is MediaOptimizationState.Split -> {
                ListItem(
                    headlineContent = {
                        Text(text = stringResource(id = R.string.screen_advanced_settings_optimise_image_upload_quality_title))
                    },
                    supportingContent = {
                        Text(text = stringResource(id = R.string.screen_advanced_settings_optimise_image_upload_quality_description))
                    },
                    trailingContent = ListItemContent.Switch(
                        checked = compressImages ?: false,
                    ),
                    onClick = {
                        val newValue = !(compressImages ?: false)
                        analyticsService.captureInteraction(
                            if (newValue) {
                                Interaction.Name.MobileSettingsOptimizeMediaUploadsEnabled
                            } else {
                                Interaction.Name.MobileSettingsOptimizeMediaUploadsDisabled
                            }
                        )
                        state.eventSink(AdvancedSettingsEvents.SetCompressMedia(newValue))
                    }
                )

                var displaySelectorDialog by remember { mutableStateOf(false) }

                ListItem(
                    headlineContent = {
                        Text(text = stringResource(id = R.string.screen_advanced_settings_optimise_video_upload_quality_title))
                    },
                    supportingContent = {
                        val description = stringResource(id = R.string.screen_advanced_settings_optimise_video_upload_quality_description)
                        val quality = when (state.mediaOptimizationState.videoPreset) {
                            VideoCompressionPreset.LOW -> stringResource(id = R.string.screen_advanced_settings_optimise_video_upload_quality_low)
                            VideoCompressionPreset.STANDARD -> stringResource(id = R.string.screen_advanced_settings_optimise_video_upload_quality_standard)
                            VideoCompressionPreset.HIGH -> stringResource(id = R.string.screen_advanced_settings_optimise_video_upload_quality_high)
                        }
                        val descriptionWithValue = remember(quality) {
                            String.format(description, quality)
                        }
                        Text(text = descriptionWithValue)
                    },
                    onClick = { displaySelectorDialog = true },
                )

                if (displaySelectorDialog) {
                    VideoQualitySelectorDialog(
                        selectedPreset = state.mediaOptimizationState.videoPreset,
                        onSubmit = { preset ->
                            state.eventSink(AdvancedSettingsEvents.SetVideoUploadQuality(preset))
                            displaySelectorDialog = false
                        },
                        onDismiss = { displaySelectorDialog = false },
                    )
                }
            }
        }

        ModerationAndSafety(state)
    }
}

@Composable
private fun ConnectionLightSettingsSection(
    primaryColor: Color,
    onPrimaryColorChange: (Color) -> Unit,
    secondaryColor: Color,
    onSecondaryColorChange: (Color) -> Unit,
    gradientEnabled: Boolean,
    onGradientEnabledChange: (Boolean) -> Unit,
    onApply: () -> Unit,
) {
    PreferenceCategory(
        title = stringResource(id = R.string.screen_advanced_settings_connection_light_title),
        showTopDivider = true,
    ) {
        LightPreview(primaryColor = primaryColor, secondaryColor = secondaryColor, gradientEnabled = gradientEnabled)
        PresetSwatches(
            onPick = onPrimaryColorChange,
            selected = primaryColor,
            title = stringResource(id = R.string.screen_advanced_settings_connection_light_primary_color),
        )
        RgbSliders(color = primaryColor, onColorChange = onPrimaryColorChange)
        PreferenceSwitch(
            title = stringResource(id = R.string.screen_advanced_settings_connection_light_enable_gradient),
            isChecked = gradientEnabled,
            onCheckedChange = onGradientEnabledChange,
        )
        if (gradientEnabled) {
            PresetSwatches(
                onPick = onSecondaryColorChange,
                selected = secondaryColor,
                title = stringResource(id = R.string.screen_advanced_settings_connection_light_secondary_color),
            )
            RgbSliders(color = secondaryColor, onColorChange = onSecondaryColorChange)
        }
        Button(
            text = stringResource(id = CommonStrings.action_save),
            onClick = onApply,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun PresetSwatches(
    title: String,
    selected: Color,
    onPick: (Color) -> Unit,
) {
    ListSectionHeader(title = title, hasDivider = false)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        connectionLightPresets.forEach { preset ->
            val isSelected = preset.color.value == selected.value
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(28.dp)
                        .clickable { onPick(preset.color) }
                        .background(preset.color)
                        .padding(4.dp)
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = CompoundIcons.Check(),
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Text(
                    text = stringResource(id = preset.labelRes),
                    style = ElementTheme.typography.fontBodyXsRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
        }
    }
}

private data class ConnectionLightPreset(
    val labelRes: Int,
    val color: Color,
)

private val connectionLightPresets = listOf(
    ConnectionLightPreset(R.string.screen_advanced_settings_connection_light_color_soft_white, Color(0xFFF0F4FF)),
    ConnectionLightPreset(R.string.screen_advanced_settings_connection_light_color_ice_blue, Color(0xFFA8D8FF)),
    ConnectionLightPreset(R.string.screen_advanced_settings_connection_light_color_mint, Color(0xFFA8FFD8)),
    ConnectionLightPreset(R.string.screen_advanced_settings_connection_light_color_amber, Color(0xFFFFD8A8)),
    ConnectionLightPreset(R.string.screen_advanced_settings_connection_light_color_lavender, Color(0xFFD8A8FF)),
    ConnectionLightPreset(R.string.screen_advanced_settings_connection_light_color_rose, Color(0xFFFFA8C8)),
)

@Composable
private fun RgbSliders(
    color: Color,
    onColorChange: (Color) -> Unit,
) {
    val red = (color.red * 255f).toInt()
    val green = (color.green * 255f).toInt()
    val blue = (color.blue * 255f).toInt()
    ColorSlider(label = "R", value = red) { onColorChange(Color(it / 255f, color.green, color.blue, 1f)) }
    ColorSlider(label = "G", value = green) { onColorChange(Color(color.red, it / 255f, color.blue, 1f)) }
    ColorSlider(label = "B", value = blue) { onColorChange(Color(color.red, color.green, it / 255f, 1f)) }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Int,
    onValueChanged: (Int) -> Unit,
) {
    ListItem(
        headlineContent = { Text(text = "$label: $value") },
    )
    Slider(
        value = value.toFloat(),
        onValueChange = { onValueChanged(it.toInt()) },
        valueRange = 0f..255f,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun LightPreview(
    primaryColor: Color,
    secondaryColor: Color,
    gradientEnabled: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = if (gradientEnabled) {
                            listOf(primaryColor, secondaryColor)
                        } else {
                            listOf(primaryColor, primaryColor)
                        }
                    )
                )
                .align(Alignment.Center),
        )
    }
}

@Composable
private fun VideoQualitySelectorDialog(
    selectedPreset: VideoCompressionPreset,
    onSubmit: (VideoCompressionPreset) -> Unit,
    onDismiss: () -> Unit
) {
    val videoPresets = VideoCompressionPreset.entries
    var localSelectedPreset by remember { mutableStateOf(selectedPreset) }
    ListDialog(
        title = stringResource(CommonStrings.dialog_video_quality_selector_title),
        subtitle = stringResource(CommonStrings.dialog_default_video_quality_selector_subtitle),
        onSubmit = { onSubmit(localSelectedPreset) },
        onDismissRequest = onDismiss,
        applyPaddingToContents = false,
    ) {
        for (preset in videoPresets) {
            val isSelected = preset == localSelectedPreset
            item(
                key = preset,
                contentType = preset,
            ) {
                val title = when (preset) {
                    VideoCompressionPreset.LOW -> stringResource(R.string.screen_advanced_settings_optimise_video_upload_quality_low)
                    VideoCompressionPreset.STANDARD -> stringResource(R.string.screen_advanced_settings_optimise_video_upload_quality_standard)
                    VideoCompressionPreset.HIGH -> stringResource(R.string.screen_advanced_settings_optimise_video_upload_quality_high)
                }
                val subtitle = when (preset) {
                    VideoCompressionPreset.LOW -> stringResource(CommonStrings.common_video_quality_low_description)
                    VideoCompressionPreset.STANDARD -> stringResource(CommonStrings.common_video_quality_standard_description)
                    VideoCompressionPreset.HIGH -> stringResource(CommonStrings.common_video_quality_high_description)
                }
                ListItem(
                    headlineContent = {
                        Text(
                            text = title,
                            style = ElementTheme.typography.fontBodyLgMedium,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = subtitle,
                            style = ElementTheme.typography.fontBodyMdRegular,
                            color = ElementTheme.colors.textSecondary,
                        )
                    },
                    leadingContent = ListItemContent.RadioButton(
                        selected = isSelected,
                    ),
                    onClick = {
                        localSelectedPreset = preset
                    },
                )
            }
        }
    }
}

@Composable
private fun ModerationAndSafety(
    state: AdvancedSettingsState,
    modifier: Modifier = Modifier,
) {
    PreferenceCategory(
        modifier = modifier,
        title = stringResource(R.string.screen_advanced_settings_moderation_and_safety_section_title),
        showTopDivider = true
    ) {
        PreferenceSwitch(
            title = stringResource(R.string.screen_advanced_settings_hide_invite_avatars_toggle_title),
            isChecked = state.mediaPreviewConfigState.hideInviteAvatars,
            onCheckedChange = {
                state.eventSink(AdvancedSettingsEvents.SetHideInviteAvatars(it))
            },
            enabled = !state.mediaPreviewConfigState.setHideInviteAvatarsAction.isLoading()
        )
        ListSectionHeader(
            title = stringResource(R.string.screen_advanced_settings_show_media_timeline_title),
            hasDivider = false,
            description = {
                ListSupportingText(
                    text = stringResource(R.string.screen_advanced_settings_show_media_timeline_subtitle),
                    contentPadding = ListSupportingTextDefaults.Padding.None,
                )
            }
        )
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.screen_advanced_settings_show_media_timeline_always_hide)) },
            leadingContent = ListItemContent.RadioButton(
                selected = state.mediaPreviewConfigState.timelineMediaPreviewValue == MediaPreviewValue.Off,
                compact = true
            ),
            onClick = {
                state.eventSink(AdvancedSettingsEvents.SetTimelineMediaPreviewValue(MediaPreviewValue.Off))
            },
            enabled = !state.mediaPreviewConfigState.setTimelineMediaPreviewAction.isLoading()
        )
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.screen_advanced_settings_show_media_timeline_private_rooms)) },
            leadingContent = ListItemContent.RadioButton(
                selected = state.mediaPreviewConfigState.timelineMediaPreviewValue == MediaPreviewValue.Private,
                compact = true
            ),
            onClick = {
                state.eventSink(AdvancedSettingsEvents.SetTimelineMediaPreviewValue(MediaPreviewValue.Private))
            },
            enabled = !state.mediaPreviewConfigState.setTimelineMediaPreviewAction.isLoading()
        )
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.screen_advanced_settings_show_media_timeline_always_show)) },
            leadingContent = ListItemContent.RadioButton(
                selected = state.mediaPreviewConfigState.timelineMediaPreviewValue == MediaPreviewValue.On,
                compact = true
            ),
            onClick = {
                state.eventSink(AdvancedSettingsEvents.SetTimelineMediaPreviewValue(MediaPreviewValue.On))
            },
            enabled = !state.mediaPreviewConfigState.setTimelineMediaPreviewAction.isLoading()
        )
    }
}

@PreviewWithLargeHeight
@Composable
internal fun AdvancedSettingsViewLightPreview(@PreviewParameter(AdvancedSettingsStateProvider::class) state: AdvancedSettingsState) =
    ElementPreviewLight { ContentToPreview(state) }

@PreviewWithLargeHeight
@Composable
internal fun AdvancedSettingsViewDarkPreview(@PreviewParameter(AdvancedSettingsStateProvider::class) state: AdvancedSettingsState) =
    ElementPreviewDark { ContentToPreview(state) }

@PreviewWithLargeHeight
@Composable
internal fun AdvancedSettingsViewBlackPreview(@PreviewParameter(AdvancedSettingsStateProvider::class) state: AdvancedSettingsState) =
    ElementPreviewBlack { ContentToPreview(state) }

@ExcludeFromCoverage
@Composable
private fun ContentToPreview(state: AdvancedSettingsState) {
    AdvancedSettingsView(
        state = state,
        onBackClick = { }
    )
}

@Composable
@PreviewsDayNight
internal fun VideoQualitySelectorDialogPreview() {
    ElementPreview {
        VideoQualitySelectorDialog(
            selectedPreset = VideoCompressionPreset.STANDARD,
            onSubmit = { /* no-op */ },
            onDismiss = { /* no-op */ }
        )
    }
}
