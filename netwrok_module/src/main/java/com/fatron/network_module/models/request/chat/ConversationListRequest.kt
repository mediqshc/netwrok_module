package com.fatron.network_module.models.request.chat


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConversationListRequest(
    @Json(name = "type")
    var type: String? = null,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "page")
    var page: Int? = 1,

)