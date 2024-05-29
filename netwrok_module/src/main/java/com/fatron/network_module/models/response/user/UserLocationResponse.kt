package com.fatron.network_module.models.response.user

import com.fatron.network_module.models.request.user.UserLocation
import com.fatron.network_module.models.request.user.UserPhoneNumber
import com.ngi.netwrok_module.BR
import com.squareup.moshi.Json

class UserLocationResponse {
    @Json(name = "user_locations")
    var userLocation: List<UserLocation>? = null

}