package com.fatron.network_module.models.response.emr.customer.medicine


import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.partnerprofile.Media
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MedicinesResponse(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "description")
    var description: String? = null,
    @Json(name = "dosage_quantity")
    val dosageQuantity: String? = null,
    @Json(name = "label")
    val label: String? = null,
    @Json(name = "emr_id")
    val emrId: Int? = null,
    @Json(name = "product_id")
    val productId: Int? = null,
    @Json(name = "dosage")
    val dosage: Dosage? = null,
    @Json(name = "no_of_days")
    val noOfDays: String? = null,
    @Json(name = "special_instruction")
    val specialInstruction: String? = null,
    @Json(name = "product")
    val product: Product? = null,
): MultipleViewItem(itemId = id.toString(), title = name, desc = description, imageUrl = product?.media?.file)

data class Product(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "product_image_id")
    val productImageId: Int? = null,
    @Json(name = "type_id")
    val typeId: Int? = null,
    @Json(name = "media")
    val media: Media? = null
)

data class Dosage(
    @Json(name = "morning")
    val morning: String? = null,
    @Json(name = "afternoon")
    val afternoon: String? = null,
    @Json(name = "evening")
    val evening: String? = null,
    @Json(name = "hourly")
    val hourly: String? = null
)