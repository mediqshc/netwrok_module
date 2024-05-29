package com.fatron.network_module.models.response.user


import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.fatron.network_module.models.request.user.UserLocation
import com.fatron.network_module.models.request.user.UserPhoneNumber
import com.fatron.network_module.utils.getDateInFormat
import com.ngi.netwrok_module.BR
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class UserResponse : BaseObservable() {


    @Json(name = "redirect_to_login")
    var redirectToLogin: Int? = null


    @Json(name = "is_subscribed")
    var isSubscribed: Int? = null


    @Json(name = "is_corporate_user")
    var isCorporateUser: Int? = null

    @Json(name = "network")
    var network: String? = null


    @Bindable
    @Json(name = "country_code")
    var countryCode: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.countryCode)
        }

    @Bindable
    @Json(name = "country_id")
    var countryId: Int? = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.countryId)
        }

    @Bindable
    @Json(name = "city_id")
    var cityId: Int? = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.cityId)
        }

    @Bindable
    @Json(name = "date_of_birth")
    var dateOfBirth: String? = null
        set(value) {
            if (value != null) {
                val dob = getDateInFormat(value, "yyyy-MM-dd", "dd/MM/yyyy")
                field = if (dob.isEmpty()) value else dob
            } else field = value
            notifyPropertyChanged(BR.dateOfBirth)
        }

    @Bindable
    @Json(name = "full_name")
    var fullName: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.fullName)
        }


    @Bindable
    @Json(name = "gender_id")
    var genderId: Int? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.gender)
        }

    @Bindable
    @Json(name = "gender")
    var gender: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.gender)
        }

    @Bindable
    @Json(name = "phone_number")
    var phoneNumber: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.phoneNumber)
        }

    @Bindable
    @Json(name = "email")
    var email: String? = null
        set(value) {
            field = value ?: ""
            notifyPropertyChanged(BR.email)
        }


    @Bindable
    @Json(name = "cnic")
    var cnic: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.cnic)
        }

    @Bindable
    @Json(name = "cnic_back")
    var cnicBack: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.cnicBack)
        }

    @Bindable
    @Json(name = "is_health_care_professional")
    var isHealthProfessional: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.isHealthProfessional)
        }

    @Bindable
    @Json(name = "application_status")
    var applicationStatus: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.applicationStatus)
        }

    @Bindable
    @Json(name = "application_status_id")
    var applicationStatusId: Int? = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.applicationStatusId)
        }

    @Bindable
    @Json(name = "cnic_front")
    var cnicFront: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.cnicFront)
        }

    @Json(name = "created_at")
    val createdAt: String? = null

    @Json(name = "created_by")
    val createdBy: String? = null

    @Bindable
    @Json(name = "id")
    var id: Int? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.id)
        }

    @Json(name = "ip")
    val ip: String? = null

    @Bindable
    @Json(name = "country")
    var country: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.country)
        }

    @Bindable
    @Json(name = "city")
    var city: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.city)
        }

    @Json(name = "timezone")
    val timezone: String? = null

    @Json(name = "timezone_offset")
    val timezoneOffset: String? = null

    @Bindable
    @Json(name = "type")
    var type: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.type)
        }

    @Json(name = "updated_at")
    val updatedAt: String? = null

    @Json(name = "deleted_at")
    val deletedAt: String? = null

    @Json(name = "email_verified_at")
    val emailVerifiedAt: String? = null

    @Bindable
    @Json(name = "access_token")
    var accessToken: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.accessToken)
        }

    @Bindable
    @Json(name = "device_token")
    var deviceToken: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.deviceToken)
        }

    @Json(name = "avatar")
    val avatar: String? = null

    @Json(name = "is_active")
    val isActive: Int? = null

    @Json(name = "last_login")
    val lastLogin: String? = null

    @Bindable
    @Json(name = "profile_picture_id")
    var profilePictureId: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.profilePictureId)
        }

    @Bindable
    @Json(name = "profile_picture")
    var profilePicture: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.profilePicture)
        }

    @Bindable
    @Json(name = "user_phone_numbers")
    var userPhoneNumbers: List<UserPhoneNumber>? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.userPhoneNumbers)
        }

    @Bindable
    @Json(name = "user_locations")
    var userLocations: List<UserLocation>? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.userLocations)
        }

    @Bindable
    @Json(name = "message_booking_count")
    var messageBookingCount: Int? = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.messageBookingCount)
        }

    @Json(name = "voip_id")
    val voipId: String? = null

    @Json(name = "delivery_charges")
    var deliveryCharges: List<UserDeliveryCharges>? = null
//        set(value) {
//            field = value
//        }

}