package com.fatron.network_module.models.response.planschedule


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class deleteSlotRequest(
    @Json(name = "slot_id")
    val slotId: Int ,
    @Json(name = "force_delete")
    val forceDelete: Int=0

)