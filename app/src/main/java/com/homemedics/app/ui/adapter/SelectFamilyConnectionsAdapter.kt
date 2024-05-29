package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.meta.MetaDataResponse
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemSelectFamilyConnectionListBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.utils.*

class SelectFamilyConnectionsAdapter(): BaseRecyclerViewAdapter<SelectFamilyConnectionsAdapter.ViewHolder, FamilyConnection>() {

    val metaData: MetaDataResponse? by lazy {
        TinyDB.instance.getObject(
            Enums.TinyDBKeys.META.key,
            MetaDataResponse::class.java
        ) as MetaDataResponse?
    }

    var lang: RemoteConfigLanguage? = null

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_select_family_connection_list }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSelectFamilyConnectionListBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemSelectFamilyConnectionListBinding) : BaseViewHolder<FamilyConnection>(binding.root) {
        override fun onBind(item: FamilyConnection, position: Int) {
            binding.apply {
                val countryCode = item.countryCode
                val phoneNumber = item.phoneNumber
                val mergedNumber = if(phoneNumber.isNullOrEmpty()) "" else "\n${Constants.START}$countryCode$phoneNumber${Constants.END}"

                val relation = item.relation?.replaceFirstChar { it.uppercase() }
                val gender = getLabelFromId(item.genderId.getSafe(), metaData?.genders)
                val age =
                    if(item.age == "0") lang?.personalprofileBasicScreen?.lessYear.getSafe()
                    else "${item.age} ${lang?.globalString?._years}"

                tvTitle.text = item.fullName
                tvDesc.text = "$relation | $gender | $age $mergedNumber"
                ivIcon.loadImage(item.profilePicture, getGenderIcon(item.genderId))
            }
        }
    }
}