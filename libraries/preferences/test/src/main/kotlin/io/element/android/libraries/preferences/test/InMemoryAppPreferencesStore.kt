/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.test

import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.matrix.api.tracing.LogLevel
import io.element.android.libraries.matrix.api.tracing.TraceLogPack
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryAppPreferencesStore(
    isDeveloperModeEnabled: Boolean = false,
    customElementCallBaseUrl: String? = null,
    hideInviteAvatars: Boolean? = null,
    timelineMediaPreviewValue: MediaPreviewValue? = null,
    theme: String? = null,
    connectionLightPrimaryColor: Int = 0xFFA8D8FF.toInt(),
    connectionLightSecondaryColor: Int = 0xFFF0F4FF.toInt(),
    connectionLightGradientEnabled: Boolean = false,
    logLevel: LogLevel = LogLevel.INFO,
    traceLockPacks: Set<TraceLogPack> = emptySet(),
) : AppPreferencesStore {
    private val isDeveloperModeEnabled = MutableStateFlow(isDeveloperModeEnabled)
    private val customElementCallBaseUrl = MutableStateFlow(customElementCallBaseUrl)
    private val theme = MutableStateFlow(theme)
    private val logLevel = MutableStateFlow(logLevel)
    private val tracingLogPacks = MutableStateFlow(traceLockPacks)
    private val connectionLightPrimaryColor = MutableStateFlow(connectionLightPrimaryColor)
    private val connectionLightSecondaryColor = MutableStateFlow(connectionLightSecondaryColor)
    private val connectionLightGradientEnabled = MutableStateFlow(connectionLightGradientEnabled)
    private val hideInviteAvatars = MutableStateFlow(hideInviteAvatars)
    private val timelineMediaPreviewValue = MutableStateFlow(timelineMediaPreviewValue)

    override suspend fun setDeveloperModeEnabled(enabled: Boolean) {
        isDeveloperModeEnabled.value = enabled
    }

    override fun isDeveloperModeEnabledFlow(): Flow<Boolean> {
        return isDeveloperModeEnabled
    }

    override suspend fun setCustomElementCallBaseUrl(string: String?) {
        customElementCallBaseUrl.tryEmit(string)
    }

    override fun getCustomElementCallBaseUrlFlow(): Flow<String?> {
        return customElementCallBaseUrl
    }

    override suspend fun setTheme(theme: String) {
        this.theme.value = theme
    }

    override fun getThemeFlow(): Flow<String?> {
        return theme
    }

    override suspend fun setConnectionLightSettings(primaryColorArgb: Int, secondaryColorArgb: Int, gradientEnabled: Boolean) {
        connectionLightPrimaryColor.value = primaryColorArgb
        connectionLightSecondaryColor.value = secondaryColorArgb
        connectionLightGradientEnabled.value = gradientEnabled
    }

    override fun getConnectionLightPrimaryColorFlow(): Flow<Int> = connectionLightPrimaryColor

    override fun getConnectionLightSecondaryColorFlow(): Flow<Int> = connectionLightSecondaryColor

    override fun isConnectionLightGradientEnabledFlow(): Flow<Boolean> = connectionLightGradientEnabled

    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    override fun getHideInviteAvatarsFlow(): Flow<Boolean?> {
        return hideInviteAvatars
    }

    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    override fun getTimelineMediaPreviewValueFlow(): Flow<MediaPreviewValue?> {
        return timelineMediaPreviewValue
    }

    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    override suspend fun setHideInviteAvatars(hide: Boolean?) {
        hideInviteAvatars.value = hide
    }

    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    override suspend fun setTimelineMediaPreviewValue(mediaPreviewValue: MediaPreviewValue?) {
        timelineMediaPreviewValue.value = mediaPreviewValue
    }

    override suspend fun setTracingLogLevel(logLevel: LogLevel) {
        this.logLevel.value = logLevel
    }

    override fun getTracingLogLevelFlow(): Flow<LogLevel> {
        return logLevel
    }

    override suspend fun setTracingLogPacks(targets: Set<TraceLogPack>) {
        tracingLogPacks.value = targets
    }

    override fun getTracingLogPacksFlow(): Flow<Set<TraceLogPack>> {
        return tracingLogPacks
    }

    override suspend fun reset() {
        // No op
    }
}
