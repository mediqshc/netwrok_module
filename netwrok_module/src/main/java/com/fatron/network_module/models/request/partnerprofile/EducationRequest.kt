package com.fatron.network_module.models.request.partnerprofile

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.ngi.netwrok_module.BR
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class EducationRequest :BaseObservable(){
     @Bindable
    @Json(name = "country_id")
    var countryId: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.countryId)
        }
    @Bindable
    @Json(name = "degree")
    var degree: String = ""
        set(value) {
            field = value.trimIndent()
            notifyPropertyChanged(BR.degree)
        }
    @Bindable
    @Json(name = "school")
    var school: String = ""
        set(value) {
            field = value.trimIndent()
            notifyPropertyChanged(BR.school)
        }
    @Bindable
    @Json(name = "country")
    var country: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.country)
        }
    @Bindable
    @Json(name = "year")
    var year: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.year)
        }

}