package com.fatron.network_module.models.response.Surgery

import com.squareup.moshi.Json

data class SurgeryResponse(
    @Json(name = "surgeries")
    val surgeries: List<Surgery>
)

data class Surgery(
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: String?,
    @Json(name = "inclusions")
    val inclusions: String?,
    @Json(name = "exclusions")
    val exclusions: String?,
    @Json(name = "icon_url")
    val iconUrl: String?,
    @Json(name = "children")
    val children: List<SurgeryChild>
)

class SurgeryTypes (
    @Json(name = "id")
    val id: Int?=null ,
    @Json(name = "name")
    val name:String?=null

)

data class SurgeryChild(
    val id: Int,
    val parent_id: Int,
    val name: String,
    val description: String?,
    val inclusions: String?,
    val exclusions: String?,
    val status: String,
    val created_at: String,
    val updated_at: String,
    val icon_url: String?
)

data class SurgeryBooking(

    @Json(name = "company_id") var companyId: Int,
    @Json(name = "hospital_id") var hospitalId: Int,
    @Json(name = "surgeon_id") var surgeonId: Int,
    @Json(name = "surgery_id") var surgeryId: Int,
    @Json(name = "admission_date") var admissionDate: String,
    @Json(name = "room_type") var roomType: String,
    @Json(name = "room_price") var roomPrice: String,
    @Json(name = "stay_length") var stayLength: Int

)


data class BookingInfo(
    val booking_id: Int,
    val case_number: String,
    val inclusions: String?,
    val exclusions: String?
)










