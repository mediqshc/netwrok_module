package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.LinearLayoutManager
import com.fatron.network_module.models.response.emr.customer.records.CustomerRecordResponse
import com.fatron.network_module.models.response.emr.type.EmrVital
import com.fatron.network_module.utils.getDateInFormat
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemVitalRecordsListBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.getBoolean
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.setVisible
import com.homemedics.app.viewmodel.EMRViewModel

class EMRVitalsAdapter(val emrViewModel: EMRViewModel) : BaseRecyclerViewAdapter<EMRVitalsAdapter.ViewHolder, CustomerRecordResponse>(),
    Filterable {
    var itemListFiltered: ArrayList<CustomerRecordResponse> = ArrayList()
    lateinit var originalList: ArrayList<CustomerRecordResponse>
    var lang: RemoteConfigLanguage? = null

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_vital_records_list }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemVitalRecordsListBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    var onShareClick: ((item: CustomerRecordResponse, position: Int)->Unit)? = null
    var onDataFilter: ((list: ArrayList<CustomerRecordResponse>) -> Unit)? = null
    var onCheckChange: (()->Unit)? = null

    inner class ViewHolder(var binding: ItemVitalRecordsListBinding) : BaseViewHolder<CustomerRecordResponse>(binding.root) {
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
//                itemClickListener = {_,pos->
//                    listItems.map { it.isSelected = false }
//                    listItems[pos].isSelected = true
//                    notifyDataSetChanged()
//
//                    onCheckChange?.invoke()
//                }
                //setting vitals

                item.vitals?.let {
                    val vitalItemsAdapter = EMRVitalItemsAdapter()
                    val layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
                    rvVitals.layoutManager = layoutManager
                    rvVitals.adapter = vitalItemsAdapter
                    vitalItemsAdapter.listItems = it.sortedBy { it.sortBy }.toMutableList() as ArrayList<EmrVital>
                }
            }
        }
    }

    private fun getDescription(item: CustomerRecordResponse): String {
        val start = "\u2066"
        val end = "\u2069"
        val hash = "\u0023"
        val pipe = "\u007C"
        var desc = ""
        desc = if(item.speciality.isNullOrEmpty()){
            "${getDateInFormat(item.date.getSafe(), "yyyy-MM-dd", "dd MMMM yyyy")} $pipe ${lang?.emrScreens?.record.getSafe()} $hash ${item.emrNumber}"
        } else {
            "${item.speciality?.get(0)?.genericItemName} $pipe ${getDateInFormat(item.date.getSafe(), "yyyy-MM-dd", "dd MMMM yyyy")} $pipe ${lang?.emrScreens?.record.getSafe()} $hash ${item.emrNumber}"
        }

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