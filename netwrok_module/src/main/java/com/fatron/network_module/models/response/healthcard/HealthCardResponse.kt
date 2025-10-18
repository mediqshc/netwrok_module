package com.fatron.network_module.models.response.healthcard

data class HealthCardResponse(
    val age: Int,
    val c_section: Int,
    val card_no: String,
    val cnic: String,
    val dob: String,
    val employee_id: String,
    val family_members: List<FamilyMember>?,
    val full_name: String,
    val grade: String,
    val hospitalization: Int,
    val id: Int,
    val maternity: Int,
    val member_no: String,
    val normal_delivery: Int,
    val policy_id: String,
    val room_limit: Int,
    val user_id: Int,
    val valid_from: String,
    val valid_till: String
)