package com.homemedics.app.utils

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.fatron.network_module.models.response.bdc.PartnerSlotsResponse
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.locale.DefaultLocaleProvider


object Bindings {
    @JvmStatic
    @BindingAdapter("android:imagePath")
    fun setImage(view: ImageView, path: String?){
        if(path.isNullOrEmpty().not()){
            Glide.with(ApplicationClass.getContext())
                .load(path)
                .placeholder(R.drawable.ic_placeholder)
                .into(view)
        }
        else view.setImageResource(R.drawable.ic_placeholder)
    }
    @JvmStatic
    @BindingAdapter("setImage","defaultImage")
    fun setImageExtFunc(view: ImageView, path: String?,defaultImage: Int=0){
        view.loadImage(path, if (defaultImage != 0)defaultImage else R.drawable.ic_placeholder)
    }

    @JvmStatic
    @BindingAdapter("android:imageRes")
    fun setImageRes(view: ImageView, res: Int?){
        res?.let {
            view.setImageResource(
                if(res == 0) 0
                else res
            )
        }?.run {
            if(res == 0) 0
        }
    }

    @JvmStatic
    @BindingAdapter("setPartnerBio")
    fun setPartnerBio(view: TextView, partner: PartnerProfileResponse?){
        partner?.let {
            val sb = StringBuilder()
            partner.specialities?.forEachIndexed { index, item ->
                sb.append(item.genericItemName)
                if(index != partner.specialities?.size?.minus(1))
                    sb.append(", ")
            }

            sb.append(", ")
            partner.educations?.forEachIndexed { index, edu ->
                sb.append(edu.degree)
                if(index != partner.educations?.size?.minus(1))
                    sb.append(", ")
            }

            view.text = sb.toString()
        }
    }

    @JvmStatic
    @BindingAdapter("setPartnerEduction")
    fun setPartnerEduction(view: TextView, partner: PartnerProfileResponse?){
        partner?.let {
            val sb = StringBuilder()
            partner.educations?.forEachIndexed { index, edu ->
                sb.append(edu.degree)
                if(index != partner.educations?.size?.minus(1))
                    sb.append(", ")
            }
            view.text = sb.toString()
        }
    }

    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter("setPartnerFee")
    fun setPartnerFee(view: TextView, partner: PartnerProfileResponse?) {
        val currency = DataCenter.getMeta()?.currencies?.find { it.itemId == partner?.currencyId.toString() }
        partner?.let {
            if (currency?.genericItemName != null && partner.fee != null) {
                val locale=  TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
                 if(locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN)
                    view.text = "${partner.fee} ${currency.genericItemName} "
                else view.text = "${currency.genericItemName} ${partner.fee} "
            } else {
                view.text = if(partner.fee?.contains("Free", true).getSafe()) {
                    ApplicationClass.mGlobalData?.globalString?.free.getSafe()
                } else {
                    "${partner.fee}"
                }
            }
        }
    }

    @JvmStatic
    @BindingAdapter("setRating")
    fun setRating(view: RatingBar, partner: PartnerProfileResponse?){
        partner?.let {
            view.rating = partner.average_reviews_rating?.toFloat().getSafe()
        }
    }

    @JvmStatic
    @BindingAdapter("setSlotsFee")
    fun setSlotsFee(view: TextView, partner: PartnerSlotsResponse?) {
        val locale = TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
        val currency = DataCenter.getMeta()?.currencies?.find { it.itemId == partner?.currencyId.toString() }
        partner?.let {
            if (currency?.genericItemName != null && partner.fee != null) {
                view.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${partner.fee} ${currency.genericItemName}" else "${currency.genericItemName} ${partner?.fee}"
            } else {
                view.text = if (partner.fee?.contains("Free", true).getSafe()) {
                    ApplicationClass.mGlobalData?.globalString?.free.getSafe()
                } else {
                    "${partner.fee}"
                }
            }
        }
    }
}