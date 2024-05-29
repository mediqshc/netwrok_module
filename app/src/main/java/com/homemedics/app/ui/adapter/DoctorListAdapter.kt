package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.homemedics.app.ApplicationClass
import com.homemedics.app.databinding.ItemDoctorBinding
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.gone
import com.homemedics.app.utils.visible
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DoctorListAdapter :  PagingDataAdapter<PartnerProfileResponse, RecyclerView.ViewHolder>(REPO_COMPARATOR) {
    var itemClickListener: ((item:PartnerProfileResponse,  position: Int) -> Unit)? = null

    companion object {
        private val REPO_COMPARATOR = object : DiffUtil.ItemCallback<PartnerProfileResponse>() {
            override fun areItemsTheSame(oldItem: PartnerProfileResponse, newItem: PartnerProfileResponse) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PartnerProfileResponse, newItem: PartnerProfileResponse) =
                oldItem.id == newItem.id
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? DoctorListViewHolder)?.bind(item = getItem(position))
        holder.itemView.setOnClickListener {
            itemClickListener.let {
                getItem(position)?.let { it1 ->
                    if (it != null) {
                        it(it1, position)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DoctorListViewHolder.getInstance(parent)
    }

    /**
     * view holder class for doggo item
     */
    class DoctorListViewHolder(val binding: ItemDoctorBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            //get instance of the DoggoImageViewHolder
            fun getInstance(parent: ViewGroup): DoctorListViewHolder {
                return DoctorListViewHolder(ItemDoctorBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent as ViewGroup,
                    false
                ) )
            }
        }


        fun bind(item: PartnerProfileResponse?) {
            binding.apply {

                partner = item
                fromList = true
                var yearExp=ApplicationClass.mGlobalData?.globalString?.yearExp?.replace("[0]",partner?.experience.getSafe())
                if (partner?.experience?.isNotEmpty().getSafe()) {
                    try {
                        if(partner?.experience?.toFloat().getSafe()<1F)
                            yearExp=ApplicationClass.mGlobalData?.globalString?.lessThanOneYearExp?.replace("[0]",partner?.experience.getSafe())
                        else if(partner?.experience?.toFloat().getSafe()>1F)
                            yearExp=ApplicationClass.mGlobalData?.globalString?.yearsExp?.replace("[0]",partner?.experience.getSafe())
                    }
                    catch (e:Exception){
                        e.printStackTrace()
                        yearExp = ""
                    }

                } else {
                    yearExp = ""
                }
                tvYearsExp.text=yearExp

                if (item?.average_reviews_rating.getSafe() > 0.0) {
                    tvNoRating.gone()
                    ratingBar.apply {
                        visible()
                        rating = item?.average_reviews_rating?.toFloat().getSafe()
                    }
                } else {
                    ratingBar.gone()
                    tvNoRating.text=ApplicationClass.mGlobalData?.globalString?.noRating
                    tvNoRating.visible()
                }

            }
        }
        private fun String.stringToUnicode( ): String {
            val unicodeString = StringBuilder()
            for (char in this) {
                unicodeString.append("\\u").append(char.toInt().toString(16).padStart(4, '0'))
            }
            return unicodeString.toString()
        }
//        fun String.toUnicode():String{
//        var code=""
//            this.forEach { c ->
//                print("${String.format("\\%04x", c.code)}")
//                code+=c.code
//            }
//            return code
//        }
    }


}