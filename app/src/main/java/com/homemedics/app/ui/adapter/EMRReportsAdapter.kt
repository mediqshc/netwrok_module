package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.fatron.network_module.models.response.emr.customer.records.CustomerRecordResponse
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemRecordsListBinding
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel

class EMRReportsAdapter(val emrViewModel: EMRViewModel) : BaseRecyclerViewAdapter<EMRReportsAdapter.ViewHolder, CustomerRecordResponse>(),
    Filterable {
    var itemListFiltered: ArrayList<CustomerRecordResponse> = ArrayList()
    lateinit var originalList: ArrayList<CustomerRecordResponse>

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_records_list }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRecordsListBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    var onShareClick: ((item: CustomerRecordResponse, position: Int)->Unit)? = null
    var onDataFilter: ((list: ArrayList<CustomerRecordResponse>) -> Unit)? = null

    inner class ViewHolder(var binding: ItemRecordsListBinding) : BaseViewHolder<CustomerRecordResponse>(binding.root) {
        override fun onBind(item: CustomerRecordResponse, position: Int) {
            binding.apply {
                tvTitle.text = item.partnerName
                val icon = if(item.serviceTypeId.isNullOrEmpty() || item.serviceTypeId == "0"){
                    R.drawable.ic_face
                } else{
                    CustomServiceTypeView.ServiceType.getServiceById(item
                        .serviceTypeId.getSafe().toInt())?.icon.getSafe()
                }
                ivIcon.setImageResource(icon)
                tvDesc.text = getDescription(item)

                rbSelect.setVisible(emrViewModel.fromBDC)
                rbSelect.isChecked = item.isSelected

                ivShare.setVisible(item.isShared.getBoolean() && emrViewModel.fromBDC.not())
                ivShare.setOnClickListener {
//                    onShareClick?.invoke(item, position)
                }
            }
        }
    }

    private fun getDescription(item: CustomerRecordResponse): String{
        val lang = ApplicationClass.mGlobalData
        var desc = ""
        var med = ""
        desc = "${getDateInFormat(item.date.getSafe(), "yyyy-MM-dd", "dd MMMM yyyy")} ${Constants.PIPE} ${lang?.globalString?.record} ${Constants.HASH} ${item.emrNumber}"

        item.labTests?.forEach {
            med += "\n▪ ${it.name}"
        }
        desc = "$desc $med"

        return desc
    }

    fun getSelectedItem(): CustomerRecordResponse? {
        return listItems.find { it.isSelected.getSafe() }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""

                itemListFiltered = if (charString.isEmpty()) originalList else {
                    val filteredList = ArrayList<CustomerRecordResponse>()

                    val filterList = originalList.filter {
                        (it.emrNumber.getSafe().contains(constraint!!,true))
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
                    results.values as ArrayList<CustomerRecordResponse>

                listItems = itemListFiltered

                onDataFilter?.invoke(listItems)
            }
        }
    }
}