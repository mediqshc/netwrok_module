package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.homemedics.app.databinding.ItemTopRatedDoctorBinding

class TopRatedDoctorListAdapter :
    PagingDataAdapter<PartnerProfileResponse, RecyclerView.ViewHolder>(REPO_COMPARATOR) {
    var itemClickListener: ((item: PartnerProfileResponse, position: Int) -> Unit)? = null

    companion object {
        private val REPO_COMPARATOR = object : DiffUtil.ItemCallback<PartnerProfileResponse>() {
            override fun areItemsTheSame(
                oldItem: PartnerProfileResponse,
                newItem: PartnerProfileResponse
            ) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: PartnerProfileResponse,
                newItem: PartnerProfileResponse
            ) =
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
    class DoctorListViewHolder(val binding: ItemTopRatedDoctorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            //get instance of the DoggoImageViewHolder
            fun getInstance(parent: ViewGroup): DoctorListViewHolder {
                return DoctorListViewHolder(
                    ItemTopRatedDoctorBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent as ViewGroup,
                        false
                    )
                )
            }
        }


        fun bind(item: PartnerProfileResponse?) {
            binding.apply {

                partner = item
                fromList = true
            }
        }

        private fun String.stringToUnicode(): String {
            val unicodeString = StringBuilder()
            for (char in this) {
                unicodeString.append("\\u").append(char.toInt().toString(16).padStart(4, '0'))
            }
            return unicodeString.toString()
        }
    }
}