package com.homemedics.app.model

import com.homemedics.app.BR
import androidx.databinding.Bindable
import com.fatron.network_module.models.generic.MultipleViewItem

class ContactItem: MultipleViewItem(){
    @Bindable
    var countryCode: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.countryCode)
        }

    @Bindable
    var other: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.other)
        }

    @Bindable
    var mobileNumber: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.mobileNumber)
        }

    @Bindable
    var category: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.category)
        }
    @Bindable
    var categoryId: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.category)
        }
}