package com.homemedics.app.model

import com.fatron.network_module.models.request.notification.PropertiesObj

data class PNModels  (
    var booking_id:String,
    var type_id:String?=null,
    var partner_service_id:String?=null,
    var duty_id:String?=null,
    val properties: String? = null,

    )