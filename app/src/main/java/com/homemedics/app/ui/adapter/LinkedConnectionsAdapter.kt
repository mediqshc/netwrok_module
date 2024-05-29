package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import com.fatron.network_module.models.response.claim.ClaimConnection
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemThumbTitleDescRadioBinding
import com.homemedics.app.utils.*
import kotlin.properties.Delegates

class LinkedConnectionsAdapter : BaseRecyclerViewAdapter<LinkedConnectionsAdapter.ViewHolder, ClaimConnection>(),
    Filterable {

    var itemListFiltered: ArrayList<ClaimConnection> = ArrayList()
    lateinit var originalList: ArrayList<ClaimConnection>

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_company }
        set(value) {}
    var selectedPosition by Delegates.observable(-1) { property, oldPos, newPos ->
        if (oldPos != newPos) {
            if (newPos in listItems.indices) {
                listItems[newPos].isSelected = true
                if (oldPos != -1) {
                    listItems[oldPos].isSelected = false
                    notifyItemChanged(oldPos)
                }
                notifyItemChanged(newPos)
            }
        }
    }

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemThumbTitleDescRadioBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(listItems[position], position)
        holder.itemView.setOnClickListener {
            selectedPosition = position
            itemClickListener?.invoke(listItems[position], position)
        }
    }

    var onDataFilter: ((list: ArrayList<ClaimConnection>) -> Unit)? = null

    inner class ViewHolder(var binding: ItemThumbTitleDescRadioBinding) :
        BaseViewHolder<ClaimConnection>(binding.root) {

        override fun onBind(item: ClaimConnection, position: Int) {
            binding.apply {
                tvTitle.text = "${item.genericItemName.getSafe()} - ${item.claimPackage?.name.getSafe()}"
                val currency =
                    DataCenter.getMeta()?.currencies?.find { it.genericItemId == item.claimPackage?.currencyId }?.genericItemName
                tvDesc.maxLines = 20 //no limit

                if (item.companyTypeId == 1) { //company
                    item.imageUrl = item.company?.icon_url
                } else { //insurance
                    tvTitle.text = "${item.insurance?.genericItemName.getSafe()} - ${item.claimPackage?.name.getSafe()}"
                    item.imageUrl = item.insurance?.icon_url
                }

                item.hasLargeIcon = true
                item.desc =
                    if (item.claimPackage?.credit?.amount.toString().isNullOrEmpty()) "$currency 0" else "$currency ${item.claimPackage?.credit?.amount?.round(2)}"
                if (item.onHold.getBoolean()) {
                    val disabled = ContextCompat.getColor(clCompany.context, R.color.primary20)
                    clCompany.isEnabled = false
                    ivIcon.alpha = 0.5F
                    tvTitle.setTextColor(disabled)
                    tvDesc.setTextColor(disabled)
                    tvCreditsOnHold.apply {
                        alpha = 0.5F
                        setVisible(item.onHold.getBoolean())
                        text = ApplicationClass.mGlobalData?.walkInScreens?.creditsOnHold.getSafe()
                    }
                    rbSelect.apply {
                        isEnabled = false
                        isChecked = false
                        isClickable = false
                        isFocusable = false
                        background = null
                        item.isSelected = false
                    }
                }
                item.title = if (item.company != null) item.company?.genericItemName.getSafe() else item.insurance?.genericItemName.getSafe()
                multipleViewItem = item
                executePendingBindings()
            }
        }
    }

    fun getSelectedItem(): ClaimConnection? {
        return listItems.find { it.isSelected.getSafe() }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""

                itemListFiltered = if (charString.isEmpty()) originalList else {
                    val filteredList = ArrayList<ClaimConnection>()

                    val filterList = originalList.filter {
                        (it.claimPackage?.name.getSafe().contains(constraint!!, true))
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
                    results.values as ArrayList<ClaimConnection>

                listItems = itemListFiltered

                onDataFilter?.invoke(listItems)
            }
        }
    }
}