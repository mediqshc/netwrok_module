package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.meta.MetaDataResponse
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemShareRecordWithBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.utils.*

class ShareRecordWithAdapter(): BaseRecyclerViewAdapter<ShareRecordWithAdapter.ViewHolder, FamilyConnection>(),
    Filterable {
    var itemListFiltered: ArrayList<FamilyConnection> = ArrayList()
    lateinit var originalList: ArrayList<FamilyConnection>

    val metaData: MetaDataResponse? by lazy {
        DataCenter.getMeta()
    }

    var lang: RemoteConfigLanguage? = null

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_share_record_with }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemShareRecordWithBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    var onCheckChange: (()->Unit)? = null
    var onDataFilter: ((list: ArrayList<FamilyConnection>) -> Unit)? = null

    inner class ViewHolder(var binding: ItemShareRecordWithBinding) : BaseViewHolder<FamilyConnection>(binding.root) {
        override fun onBind(item: FamilyConnection, position: Int) {
            binding.apply {
                val relation = item.relation?.replaceFirstChar { it.uppercase() }
                val gender = getLabelFromId(item.genderId.getSafe(), metaData?.genders)
                val age =
                    if(item.age == "0") lang?.personalprofileBasicScreen?.lessYear.getSafe()
                    else "${item.age} ${lang?.globalString?._years}"
                tvTitle.text = item.fullName
                tvDesc.text = "$relation | $gender | $age"
                ivIcon.loadImage(item.profilePicture, getGenderIcon(item.genderId))

                checkbox.isChecked = item.isSelected

                itemClickListener = { item, pos ->
                    listItems[pos].isSelected = item.isSelected.not()
                    onCheckChange?.invoke()
                    notifyDataSetChanged()
                }
            }
        }
    }

    fun getSelectedItems(): ArrayList<FamilyConnection>{
        return listItems.filter { it.isSelected } as ArrayList
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""

                itemListFiltered = if (charString.isEmpty()) originalList else {
                    val filteredList = ArrayList<FamilyConnection>()

                    val filterList = originalList.filter {
                        (it.fullName.getSafe().contains(constraint!!,true))
                    }.toList()


                    filterList.forEach {
                        filteredList.add(it)
                    }
                    filteredList
                }
                return FilterResults().apply { values = itemListFiltered }
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                itemListFiltered = if (results?.values == null)
                    ArrayList()
                else
                    results.values as ArrayList<FamilyConnection>

                listItems = itemListFiltered

                onDataFilter?.invoke(listItems)
            }
        }
    }
}