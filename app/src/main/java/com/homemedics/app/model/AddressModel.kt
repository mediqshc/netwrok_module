package com.homemedics.app.model

import com.fatron.network_module.models.generic.MultipleViewItem

class AddressModel(
    var address: String = "",
    var subLocality: String = "",
    var region: String = "",
    var streetAddress: String = "",
    var floor : String= "",
    var category: String = "",
    var categoryId: Int? = 0,
    var other: String? = null,
    var latitude:Double?=null,
    var longitude:Double?=null
) : MultipleViewItem(){
    var id: Int? = null
}