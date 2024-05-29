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
import com.homemedics.app.databinding.ItemFamilyConnectionListBinding
import com.homemedics.app.model.ConnectionType
import com.homemedics.app.utils.*

class FamilyConnectionsAdapter(val type: ConnectionType): BaseRecyclerViewAdapter<FamilyConnectionsAdapter.ViewHolder, FamilyConnection>() {

    val metaData: MetaDataResponse? by lazy {
        TinyDB.instance.getObject(
            Enums.TinyDBKeys.META.key,
            MetaDataResponse::class.java
        ) as MetaDataResponse?
    }

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_family_connection_list }
        set(value) {}

    var onRequestAcceptClick: ((item: FamilyConnection, position: Int)->Unit)? = null
    var onRequestRejectClick: ((item: FamilyConnection, position: Int)->Unit)? = null

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFamilyConnectionListBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemFamilyConnectionListBinding) : BaseViewHolder<FamilyConnection>(binding.root) {
        override fun onBind(item: FamilyConnection, position: Int) {
            binding.apply {
                ivAccept.setOnClickListener {
                    onRequestAcceptClick?.invoke(item, position)
                }
                ivDelete.setOnClickListener {
                    onRequestRejectClick?.invoke(item, position)
                }

                ivAccept.setVisible(type == ConnectionType.Received)

                val relation = item.relation?.replaceFirstChar { it.uppercase() }
                val gender = getLabelFromId(item.genderId.getSafe(), metaData?.genders)
                val age =
                    if(item.age == "0")  ApplicationClass.mGlobalData?.personalprofileBasicScreen?.lessYear.getSafe()
                    else  "${item.age} ${ApplicationClass.mGlobalData?.globalString?.years.getSafe()}"
                tvTitle.text = item.fullName
                tvDesc.text = "$relation | $gender | $age"
                ivIcon.loadImage(item.profilePicture, getGenderIcon(item.genderId))
            }
        }
    }
}