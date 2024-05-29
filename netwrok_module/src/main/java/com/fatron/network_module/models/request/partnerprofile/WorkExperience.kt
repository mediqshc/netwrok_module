package com.fatron.network_module.models.request.partnerprofile


import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.fatron.network_module.models.generic.MultipleViewItem
import com.ngi.netwrok_module.BR
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class WorkExperience  : MultipleViewItem() {

    @Bindable
    @Json(name = "id")
    var id: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.id)
        }
    @Bindable
    @Json(name = "company")
    var company: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.company)
        }

    @Bindable
    @Json(name = "years_of_experience")
    var yearsOfExperience: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.yearsOfExperience)
        }

    @Bindable
    @Json(name = "designation")
    var designation: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.designation)
        }
    @Bindable
    @Json(name = "end_date")
    var endDate: String? =  null
        set(value) {
            field = value
            notifyPropertyChanged(BR.endDate)
        }
    @Bindable
    @Json(name = "start_date")
    var startDate: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.startDate)
        }


}