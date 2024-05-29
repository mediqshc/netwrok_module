package com.fatron.network_module.models.request.emr.medicine


import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.ngi.netwrok_module.BR
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class StoreMedicineRequest: BaseObservable() {
    @Json(name = "modify")
    var modify: Int? = null

    @Json(name = "emr_item_id")
    var emrItemId: Int? = null //using to modify data according to this id

    @Json(name = "emr_id")
    var emrId: Int? = null

    @Json(name = "emr_type_id")
    var emrTypeId: Int? = null

    @Json(name = "emr_product_id")
    var emrProductId: Int? = null

    @Json(name = "product_id")
    var productId: String? = null

    @Json(name = "date")
    var date: String? = null

//    @Json(name = "dosage_quantity")
//    var dosageQuantity: Int? = null

    @Bindable
    @Json(name = "dosage_quantity")
    var dosageQuantity: Float? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.dosageQuantity)
        }

    @Bindable
    @Json(name = "name")
    var name: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
        }

    @Json(name = "type")
    var type: Int? = null

    @Bindable
    @Json(name = "description")
    var description: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.description)
        }

    @Json(name = "dosage_type")
    var dosageType: String? = null

    @Bindable
    @Json(name = "special_instruction")
    var specialInstruction: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.specialInstruction)
        }

    @Bindable
    @Json(name = "no_of_days")
    var noOfDays: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.noOfDays)
        }

    @Bindable
    @Json(name = "hourly")
    var hourly: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.hourly)
        }

    @Bindable
    @Json(name = "morning")
    var morning: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.morning)
        }

    @Bindable
    @Json(name = "afternoon")
    var afternoon: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.afternoon)
        }

    @Bindable
    @Json(name = "evening")
    var evening: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.evening)
        }
}