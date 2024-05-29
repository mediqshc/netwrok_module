package com.fatron.network_module.models.request.labtest


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class LabTestFilterRequest {
    @Json(name = "page")
    var page: Int? = 0

    @Json(name = "lab_test_category_id")
    var categoryId: Int? = null

    @Json(name = "country_id")
    var countryId: Int? = null

    @Json(name = "city_id")
    var cityId: Int? = null

    @Json(name = "name")
    var name: String? = null

    @Json(name = "lab_test_id")
    var labTestId: Int? = null

    var countryName: String? = null
    var cityName: String? = null
    var categoryName: String? = null
}