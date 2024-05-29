package com.fatron.network_module.models.response.partnerprofile


import com.fatron.network_module.models.response.user.UserResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PatientResponse(
    @Json(name = "user_id")
    val userId: Int? = null,
    @Json(name = "relation")
    val relation: String? = null,
    @Json(name = "family_member_id")
    var familyMemberId: Int? = null
): UserResponse()