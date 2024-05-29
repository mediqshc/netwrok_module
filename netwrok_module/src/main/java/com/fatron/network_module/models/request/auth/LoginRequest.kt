package com.fatron.network_module.models.request.auth


import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.fatron.network_module.utils.getAndroidID
import com.ngi.netwrok_module.BR
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.ZoneId
import java.util.*

@JsonClass(generateAdapter = true)
class LoginRequest: BaseObservable(){
    @Bindable
    @Json(name = "phone_number")
    var phoneNumber: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.phoneNumber)
        }

    @Bindable
    @Json(name = "password")
    var password: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.password)
        }

    @Bindable
    @Json(name = "country_code")
    var countryCode: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.countryCode)
        }

    @Json(name = "device_token")
    var deviceToken: String = ""

    @Json(name = "fcm_token")
    var fcmToken: String = ""

    @Json(name = "device_meta")
    var deviceMeta: String = ""

    @Json(name = "type")
    var type: String? = null

    @Json(name = "timezone")
    var timeZone: String = ""
}