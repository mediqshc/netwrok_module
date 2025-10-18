package com.fatron.network_module.models.response.Surgery

import com.squareup.moshi.Json


data class SurgeonResponse(
    @Json(name = "surgeons") val surgeons: List<Surgeon>
)

data class Surgeon(
    @Json(name = "id") val id: Int,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "name") val name: String,
    @Json(name = "country_code") val countryCode: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "years_of_experience") val yearsOfExperience: String,
    @Json(name = "partner_surgery_services") val partnerSurgeryServices: List<PartnerSurgeryService>,
    @Json(name = "consultation_details") val consultationDetails: ConsultationDetails,
    @Json(name = "hospital_details") val hospitalDetails: List<HospitalDetail>,

)

data class PartnerSurgeryService(
    @Json(name = "id") val id: Int,
    @Json(name = "doctor_id") val doctorId: Int,
    @Json(name = "surgery_catalog_id") val surgeryCatalogId: Int,
    @Json(name = "healthcare_id") val healthcareId: Int,
    @Json(name = "selected_days") val selectedDays: Map<String, SelectedDay>,
    @Json(name = "surgery_cost") val surgeryCost: String,
    @Json(name = "discount") val discount: String,
    @Json(name = "share") val share: String,
    @Json(name = "status") val status: Boolean,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "room_categories") val roomCategories: List<RoomCategory>
)

data class SelectedDay(
    @Json(name = "start") val start: String?,
    @Json(name = "end") val end: String?
)

data class RoomCategory(
    @Json(name = "id") val id: Int,
    @Json(name = "healthcare_id") val healthcareId: Int,
    @Json(name = "room_type") val roomType: String,
    @Json(name = "room_price") val roomPrice: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "laravel_through_key") val laravelThroughKey: Int
)

data class ConsultationDetails(
    @Json(name = "is_available_in_city") val isAvailableInCity: Int,
    @Json(name = "customer_user_id") val customerUserId: Int,
    @Json(name = "partner_id") val partnerId: Int,
    @Json(name = "partner_user_id") val partnerUserId: Int,
    @Json(name = "partner_type_id") val partnerTypeId: Int,
    @Json(name = "partner_service_id") val partnerServiceId: Int,
    @Json(name = "is_online") val isOnline: Int,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "gender_id") val genderId: Int,
    @Json(name = "profile_picture") val profilePicture: String,
    @Json(name = "experience") val experience: String,
    @Json(name = "overview") val overview: String,
    @Json(name = "fee") val fee: String,
    @Json(name = "currency_id") val currencyId: Int,
    @Json(name = "average_reviews_rating") val averageReviewsRating: Int,
    @Json(name = "total_no_of_reviews") val totalNoOfReviews: Int,
    @Json(name = "specialities") val specialities: List<Speciality>,
    @Json(name = "educations") val educations: List<Education>,
    @Json(name = "reviews") val reviews: List<Review>
)

data class Speciality(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "icon_url") val iconUrl: String
)

data class Education(
    @Json(name = "id") val id: Int,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "school") val school: String,
    @Json(name = "degree") val degree: String,
    @Json(name = "year") val year: String,
    @Json(name = "country_id") val countryId: Int
)

data class Review(
    @Json(name = "id") val id: Int,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "booking_id") val bookingId: Int,
    @Json(name = "partner_id") val partnerId: Int,
    @Json(name = "service_id") val serviceId: Int,
    @Json(name = "rating") val rating: Double,
    @Json(name = "review") val review: String?
)

data class HospitalDetail(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "hospital_address") val address: String? = null,
    @Json(name = "logo") val hospitalLogo: String? = null,
    @Json(name = "surgery_cost") val surgeryCost: String? = null,// placeholder (your JSON has empty array for now)
    @Json(name = "surgeries") val surgeries: List<DoctorSurgery>
)

data class DoctorSurgery(
    @Json(name = "id") val id: Int,
    @Json(name = "parent_id") val parentId: Int,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String?,
    @Json(name = "inclusions") val inclusions: String?,
    @Json(name = "exclusions") val exclusions: String?,
    @Json(name = "status") val status: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "icon_url") val iconUrl: String?,
    @Json(name = "surgery_cost") val surgeryCost: String
)


