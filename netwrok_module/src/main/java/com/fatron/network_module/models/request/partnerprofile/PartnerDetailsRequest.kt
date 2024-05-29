package com.fatron.network_module.models.request.partnerprofile

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.ngi.netwrok_module.BR
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class PartnerDetailsRequest (

    @Json(name = "cnic")
    var cnic: String? = "",

    @Json(name = "license_code")
    var licenseCode: String? = "",


    @Json(name = "overview")
    var overview: String? = "",

    @Json(name = "type")
    var type:Int? = null,

    @Json(name = "years_of_experience")
    var yearsOfExperience: String? = null

)