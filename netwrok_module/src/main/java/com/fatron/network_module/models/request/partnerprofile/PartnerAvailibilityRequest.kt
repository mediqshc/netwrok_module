package com.fatron.network_module.models.request.partnerprofile

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class PartnerAvailabilityRequest (
    @Json(name = "partner_user_id")
    var partnerUserId: Int? = null,

    @Json(name = "available_till")
    var availableTill: String? = null,

    @Json(name = "force_update")
    var forceUpdate: Int? = null,

    @Json(name = "available_now")
    var availableNow: Int? = null,
)