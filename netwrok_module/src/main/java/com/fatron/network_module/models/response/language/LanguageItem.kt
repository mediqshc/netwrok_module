package com.fatron.network_module.models.response.language

import com.fatron.network_module.models.generic.MultipleViewItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class LanguageItem(
    @Json(name = "id")
    var id: Int? = null,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "short_name")
    var shortName: String? = null

):MultipleViewItem( itemId = id.toString(), title = name, desc = shortName)
