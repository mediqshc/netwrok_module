package com.fatron.network_module.models.response.partnerprofile


import androidx.databinding.Bindable
import com.fatron.network_module.models.request.partnerprofile.WorkExperience
import com.fatron.network_module.models.response.appointments.MedicalRecords
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.ordersdetails.Review
import com.fatron.network_module.models.response.user.RejectionReason
import com.fatron.network_module.models.response.user.UserResponse
import com.google.gson.annotations.SerializedName
import com.ngi.netwrok_module.BR
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class PartnerProfileResponse(
    @Json(name = "medical_records")
    @SerializedName("medical_records")
    val medicalRecord: List<MedicalRecords>? = null,
    @Json(name = "rejection_reason")
    @SerializedName("rejection_reason")
    val rejectionReason: RejectionReason? = null
) : UserResponse() {
    @Bindable
    @Json(name = "educations")
    var educations:  List<EducationResponse>? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.educations)
        }
    @Bindable
    @Json(name = "license_code")
    var licenseCode: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.licenseCode)
        }
    @Bindable
    @Json(name = "overview")
    var overview: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.overview)
        }
    @Bindable
    @Json(name = "status")
    var status: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.status)
        }

    @Bindable
    @Json(name = "member_type_id")
    var memberTypeId: String? = "null"
        set(value) {
            field = value
            notifyPropertyChanged(BR.memberTypeId)
        }
    @Bindable
    @Json(name = "staff_services")
    var staffServices: List<Services>? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.staffServices)
        }
    @Bindable
    @Json(name = "specialities")
    var specialities: List<GenericItem>? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.specialities)
        }

    @Bindable
    @Json(name = "works")
    var works: List<WorkExperience>? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.works)
        }
    @Bindable
    @Json(name = "doctor_services")
    var doctorServices: List<Services>? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.doctorServices)
        }

    @Bindable
    @Json(name = "years_of_experience")
    var yearsOfExperience: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.yearsOfExperience)
        }

    //for other partners

    @Json(name = "currency_id")
    var currencyId: Int? = null

    @Json(name = "user_id")
    var userId: Int? = null

    @Json(name = "experience")
    var experience: String? = ""

    @Json(name = "fee")
    var fee: String? = ""

    @Json(name = "fees")
    var fees: String? = null

    @Json(name = "currency")
    var currency: String? = ""

    @Json(name = "partner_service_id")
    var partnerServiceId: Int? = null

    @Json(name = "partner_user_id")
    var partnerUserId: Int? = null

    @Json(name = "partner_type_id")
    var partnerTypeId: Int? = null

    @Json(name = "average_reviews_rating")
    var average_reviews_rating: Double? = 0.0

    @Json(name = "total_no_of_rating")
    var totalNoOfRating: Int? = null

    @Json(name = "total_no_of_reviews")
    var totalNoOfReviews: Int? = null

    @Json(name = "booking_id")
    var bookingId: Int? = null

    @Json(name = "patients")
    var patients:  List<PatientResponse>? = null

    @Json(name = "services")
    var services: List<Services>? = null

    @Json(name = "is_online")
    var isOnline: Int? = 0

    @Json(name = "reviews")
    val reviews: Review? = null

    @Json(name = "is_available_in_city")
    var isAvailableInCity: Int? = 0
}