package com.fatron.network_module.models.generic

import androidx.databinding.BaseObservable
import java.io.Serializable

open class MultipleViewItem(
    var itemId: String? = "",
    var title: String? = "",
    var desc: String? = "",
    var imageUrl: String? = "",
    var drawable: Int? = 0,

    var pakgeId: Int? = null,
    var charges: Int? = null
) : BaseObservable(), Serializable {
    var nameTitle: String? = null
    var type: String? = ""
    var isSelected: Boolean? = false
    var hasRoundLargeIcon: Boolean? = false
    var hasLargeIcon: Boolean? = false
    var itemEndIcon: Int? = null
    var itemCenterIcon:Int?=null
    var extraInt: Int? = null //if item have more than 1 id to be used //using in location
    var hasSecondAction: Boolean? = false // edit button visibility in emr
    var subDesc: String? = ""
    var descMaxLines: Int? = 2
    var subDescMaxLines: Int? = 1
    var isEnabled: Boolean? = true
    var isRTL: Boolean? = false
    var isRed: Boolean? = false
    var redText: String? = ""
}