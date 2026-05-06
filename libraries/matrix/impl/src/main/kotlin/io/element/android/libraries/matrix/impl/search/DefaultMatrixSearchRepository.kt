/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search

import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.search.MatrixSearchRepository
import io.element.android.libraries.matrix.api.search.MessageSearchResult
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.IOException

@ContributesBinding(SessionScope::class)
class DefaultMatrixSearchRepository(
    private val matrixClient: MatrixClient,
    private val sessionStore: SessionStore,
    private val okHttpClient: OkHttpClient,
    private val jsonProvider: JsonProvider,
) : MatrixSearchRepository {
    private val json by lazy { jsonProvider() }

    override suspend fun searchMessages(searchTerm: String, roomId: RoomId?): Result<List<MessageSearchResult>> {
        if (searchTerm.isBlank()) return Result.success(emptyList())

        return withContext(matrixClient.sessionCoroutineScope.coroutineContext) {
            runCatchingExceptions {
                val session = sessionStore.getSession(matrixClient.sessionId.value)
                    ?: error("No session data for ${matrixClient.sessionId.value}")

                val payload = buildRequestBody(searchTerm = searchTerm, roomId = roomId)
                val request = Request.Builder()
                    .url("${session.homeserverUrl.trimEnd('/')}/_matrix/client/v3/search")
                    .header("Authorization", "Bearer ${session.accessToken}")
                    .header("Content-Type", CONTENT_TYPE)
                    .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Search request failed with HTTP ${response.code}")
                    }
                    val body = response.body?.string().orEmpty()
                    val root = json.parseToJsonElement(body).jsonObject
                    val parsedResults = mutableListOf<MessageSearchResult>()
                    for (result in root.resultsArray()) {
                        result.jsonObject.toMessageSearchResult()?.let(parsedResults::add)
                    }
                    parsedResults
                }
            }.onFailure {
                Timber.w(it, "Failed to search messages for '%s'", searchTerm)
            }
        }
    }

    private suspend fun JsonObject.toMessageSearchResult(): MessageSearchResult? {
        val event = get("result")?.jsonObject ?: return null
        val eventId = event.string("event_id") ?: return null
        val roomId = event.string("room_id") ?: return null
        val senderId = event.string("sender") ?: return null
        val timestamp = event.long("origin_server_ts") ?: 0L
        val profileInfo = get("context")
            ?.jsonObject
            ?.get("profile_info")
            ?.jsonObject
            ?.get(senderId)
            ?.jsonObject

        val roomName = matrixClient.getRoom(RoomId(roomId))?.info()?.name
        val snippet = event
            .get("content")
            ?.jsonObject
            ?.string("body")
            ?.takeIf { it.isNotBlank() }
            ?: return null

        return MessageSearchResult(
            eventId = EventId(eventId),
            roomId = RoomId(roomId),
            roomName = roomName,
            senderId = senderId,
            senderName = profileInfo?.string("displayname") ?: senderId,
            senderAvatar = profileInfo?.string("avatar_url"),
            snippet = snippet,
            timestamp = timestamp,
        )
    }

    private fun JsonObject.resultsArray(): JsonArray {
        return get("search_categories")
            ?.jsonObject
            ?.get("room_events")
            ?.jsonObject
            ?.get("results")
            ?.jsonArray
            ?: JsonArray(emptyList())
    }

    private fun buildRequestBody(searchTerm: String, roomId: RoomId?): String {
        return buildJsonObject {
            putJsonObject("search_categories") {
                putJsonObject("room_events") {
                    put("search_term", searchTerm)
                    put("order_by", "recent")
                    putJsonObject("event_context") {
                        put("before_limit", 0)
                        put("after_limit", 0)
                    }
                    if (roomId != null) {
                        putJsonObject("filter") {
                            putJsonArray("rooms") {
                                add(roomId.value)
                            }
                        }
                    }
                }
            }
        }.toString()
    }

    private fun JsonObject.string(key: String): String? = get(key)?.primitiveContent()

    private fun JsonObject.long(key: String): Long? = get(key)?.jsonPrimitive?.contentOrNull?.toLongOrNull()

    private fun JsonElement.primitiveContent(): String? = (this as? JsonPrimitive)?.contentOrNull

    private companion object {
        const val CONTENT_TYPE = "application/json"
        val JSON_MEDIA_TYPE = CONTENT_TYPE.toMediaType()
    }
}
