/*
 * Copyright (c) 2026 Elyon.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 */

package io.element.android.features.login.impl.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElonConfigModel(
    val homeservers: List<ElonHomeserverModel>,
    @SerialName("default")
    val defaultHomeserver: String,
)

@Serializable
data class ElonHomeserverModel(
    val name: String,
    val url: String,
    val description: String? = null,
)
