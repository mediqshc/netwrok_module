package com.fatron.network_module.models.request.auth


import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.ngi.netwrok_module.BR
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ForgetPwdRequest : BaseObservable() {
    @Bindable
    @Json(name = "country_code")
    var countryCode: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.countryCode)
        }

    @Bindable
    @Json(name = "password")
    var password: String ? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.password)

        }

    @Bindable
    @Json(name = "password_confirmation")
    var passwordConfirmation: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.passwordConfirmation)
        }

    @Bindable
    @Json(name = "phone_number")
    var phoneNumber: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.phoneNumber)
        }
    @Bindable
    @Json(name = "otp")
    var otp: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.otp)
        }

}