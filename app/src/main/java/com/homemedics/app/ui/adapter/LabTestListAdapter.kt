package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.fatron.network_module.models.response.labtest.LabTestResponse
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemPharmacyMedicineBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.utils.DataCenter
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.gone

class LabTestListAdapter : BaseRecyclerViewAdapter<LabTestListAdapter.ViewHolder, LabTestResponse>(), Filterable {

    var itemListFiltered: ArrayList<LabTestResponse> = ArrayList()
    lateinit var originalList: ArrayList<LabTestResponse>

    override var layout: (viewType: Int) -> Int
    get() = { R.layout.item_company }
    set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPharmacyMedicineBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    var onDataFilter: ((list: ArrayList<LabTestResponse>) -> Unit)? = null

    inner class ViewHolder(var binding: ItemPharmacyMedicineBinding) : BaseViewHolder<LabTestResponse>(binding.root) {
        override fun onBind(item: LabTestResponse, position: Int) {
            val locale = TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
            binding.apply {
                ivPharma.gone()

                tvDescription.maxLines = 2

                tvName.text = item.name
                tvDescription.text = item.description

                var currency = ""
                if(item.labTestLaboratory.isNullOrEmpty().not())
                    currency = DataCenter.getMeta()?.currencies?.find { it.genericItemId == item.labTestLaboratory?.get(0)?.currencyId }?.genericItemName.getSafe()

                if(item.labTestLaboratory?.size.getSafe() > 1) {
                    if (item.labTestLaboratory?.first()?.rate != item.labTestLaboratory?.last()?.rate)
                        tvAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${item.labTestLaboratory?.first()?.rate} - ${item.labTestLaboratory?.last()?.rate} $currency" else "$currency ${item.labTestLaboratory?.first()?.rate} - ${item.labTestLaboratory?.last()?.rate}"
                    else
                        tvAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${item.labTestLaboratory?.first()?.rate} $currency" else "$currency ${item.labTestLaboratory?.first()?.rate}"
                }
                else {
                    if(item.labTestLaboratory.isNullOrEmpty().not()) {
                        tvAmount.text =
                            if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${item.labTestLaboratory?.first()?.rate} $currency" else "$currency ${item.labTestLaboratory?.first()?.rate}"
                    } else {
                        tvAmount.text = ""
//                        tvAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "0 $currency"  else "$currency 0"
//                        tvAmount.invisible()
                    }
                }
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""

                itemListFiltered = if (charString.isEmpty()) originalList else {
                    val filteredList = ArrayList<LabTestResponse>()

                    val filterList = originalList.filter {
                        (it.name.getSafe().contains(constraint!!,true))
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
                    results.values as ArrayList<LabTestResponse>

                listItems = itemListFiltered

                onDataFilter?.invoke(listItems)
            }
        }
    }
}