package com.fatron.network_module.models.response.user

data class UserDeliveryCharges(
    val city_id: Int,
    val country_id: Int,
    val created_at: String,
    val delivery_charges: Int,
    val id: Int,
    val min_amount: Int,
    val status: Int,
    val tenant_id: Int,
    val updated_at: String
)