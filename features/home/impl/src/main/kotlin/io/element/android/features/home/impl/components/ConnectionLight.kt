/*
 * Copyright (c) 2026 Elyon.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 */

package io.element.android.features.home.impl.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import io.element.android.libraries.matrix.api.sync.SyncState

@Composable
fun ConnectionLight(
    syncState: SyncState,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFFA8D8FF),
    secondaryColor: Color = Color(0xFFF0F4FF),
    gradientEnabled: Boolean = false,
) {
    val lightMode = when (syncState) {
        SyncState.Idle -> ConnectionLightMode.Connecting
        SyncState.Running -> ConnectionLightMode.Connected
        SyncState.Error -> ConnectionLightMode.Hidden
        SyncState.Terminated -> ConnectionLightMode.Hidden
        SyncState.Offline -> ConnectionLightMode.Hidden
    }

    val targetAlpha = when (lightMode) {
        ConnectionLightMode.Hidden -> 0f
        ConnectionLightMode.Connecting -> 1f
        ConnectionLightMode.Connected -> 1f
    }
    val targetScale = when (lightMode) {
        ConnectionLightMode.Hidden -> 0f
        ConnectionLightMode.Connecting -> 1f
        ConnectionLightMode.Connected -> 1f
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 1200, easing = LinearEasing),
        label = "connection_light_alpha",
    )
    val animatedBaseScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 1200, easing = LinearEasing),
        label = "connection_light_base_scale",
    )

    val pulseTransition = rememberInfiniteTransition(label = "connection_light_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "connection_light_pulse_scale",
    )
    val effectivePulse = when (lightMode) {
        ConnectionLightMode.Hidden -> 1f
        ConnectionLightMode.Connecting -> pulseScale
        ConnectionLightMode.Connected -> 1f + (pulseScale - 1f) * 0.5f
    }

    Canvas(
        modifier = modifier
            .size(40.dp)
            .scale(animatedBaseScale * effectivePulse)
            .alpha(animatedAlpha),
    ) {
        val radius = size.minDimension / 2f
        val glowColor = primaryColor.copy(alpha = primaryColor.alpha.coerceAtMost(0.6f))
        val secondaryGlowColor = (if (gradientEnabled) secondaryColor else primaryColor)
            .copy(alpha = 0.24f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor,
                    secondaryGlowColor,
                    primaryColor.copy(alpha = 0f),
                ),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = radius,
            ),
            radius = radius,
            center = Offset(size.width / 2f, size.height / 2f),
        )
    }
}

private enum class ConnectionLightMode {
    Hidden,
    Connecting,
    Connected,
}
