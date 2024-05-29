package com.fatron.network_module.models.request.family


import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.ngi.netwrok_module.BR
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class FamilyRequest: BaseObservable() {
    @Bindable
    @Json(name = "full_name")
    var fullName: String? = null
        set(value) {
            field = value?.trimIndent()
            notifyPropertyChanged(BR.fullName)
        }

    @Bindable
    @Json(name = "relation_id")
    var relationId: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.relationId)
        }

    @Bindable
    @Json(name = "gender_id")
    var genderId: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.genderId)
        }

    @Bindable
    @Json(name = "date_of_birth")
    var dateOfBirth: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.dateOfBirth)
        }

    @Bindable
    @Json(name = "country_code")
    var countryCode: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.countryCode)
        }

    @Bindable
    @Json(name = "phone_number")
    var phoneNumber: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.phoneNumber)
        }
}