package com.fatron.network_module.models.response.claim

import com.squareup.moshi.Json

data class PriorAuthInit(
    @Json(name = "id")
    var id: Int? = null,
    @Json(name = "u_id")
    var uId: String? = null,

)