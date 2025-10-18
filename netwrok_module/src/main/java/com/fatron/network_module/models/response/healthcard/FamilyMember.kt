package com.fatron.network_module.models.response.healthcard

data class FamilyMember(
    val age: Int?,
    val country_code: String?,
    val family_member_id: Int,
    val family_member_relation_id: Int,
    val full_name: String?,
    val gender_id: Int?,
    val id: Int,
    val phone_number: String?,
    val profile_picture: Any?,
    val relation: String?,
    val user_id: Int?
)