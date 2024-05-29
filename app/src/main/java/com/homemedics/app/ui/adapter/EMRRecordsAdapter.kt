package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.fatron.network_module.models.response.emr.customer.consultation.ConsultationRecordsResponse
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemRecordsListBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.getBoolean
import com.homemedics.app.utils.getDateInFormat
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.setVisible
import com.homemedics.app.viewmodel.EMRViewModel

class EMRRecordsAdapter(val emrViewModel: EMRViewModel) : BaseRecyclerViewAdapter<EMRRecordsAdapter.ViewHolder, ConsultationRecordsResponse>(), Filterable {

    var itemListFiltered: ArrayList<ConsultationRecordsResponse> = ArrayList()
    lateinit var originalList: ArrayList<ConsultationRecordsResponse>
    var lang: RemoteConfigLanguage? = null

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

    var onShareClick: ((item: ConsultationRecordsResponse, position: Int)->Unit)? = null
    var onDataFilter: ((list: ArrayList<ConsultationRecordsResponse>) -> Unit)? = null

    inner class ViewHolder(var binding: ItemRecordsListBinding) : BaseViewHolder<ConsultationRecordsResponse>(binding.root) {
        override fun onBind(item: ConsultationRecordsResponse, position: Int) {
            val hash = "\u0023"
            val pipe = "\u007C"
            val start = "\u2066"
            val end = "\u2069"
            binding.apply {
                tvTitle.text = item.partnerName

                val dateStamp: String = getDateInFormat(
                    item.date.getSafe(), "yyyy-MM-dd", "dd MMMM yyyy"
                )


                if(item.speciality.isNullOrEmpty()){
                    tvDesc.text = "$end$dateStamp$start\n" +
                            "${lang?.emrScreens?.record.getSafe()} $hash $start${item.emrNumber}$end"
                }
                else {
                    tvDesc.text = "${item.speciality?.get(0)?.genericItemName} $pipe $end$dateStamp$start\n" +
                            "${lang?.emrScreens?.record.getSafe()} $hash $start${item.emrNumber}$end"
                }

                val icon = CustomServiceTypeView.ServiceType.getServiceById(item
                    .partnerServiceId.getSafe())?.icon.getSafe()
                ivIcon.setImageResource(icon)

                rbSelect.setVisible(emrViewModel.fromBDC)
                rbSelect.isChecked = item.isSelected

                ivShare.setVisible(item.isShared.getBoolean() && emrViewModel.fromBDC.not())
                ivShare.setOnClickListener {
//                    onShareClick?.invoke(item, position)
                }
            }
        }
    }

    fun getSelectedItem(): ConsultationRecordsResponse? {
        return listItems.find { it.isSelected.getSafe() }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""

                itemListFiltered = if (charString.isEmpty()) originalList else {
                    val filteredList = ArrayList<ConsultationRecordsResponse>()

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
                    results.values as ArrayList<ConsultationRecordsResponse>

                listItems = itemListFiltered

                onDataFilter?.invoke(listItems)
            }
        }
    }
}