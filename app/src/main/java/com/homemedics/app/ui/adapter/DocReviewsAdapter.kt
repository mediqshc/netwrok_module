package com.homemedics.app.ui.adapter

import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.devs.readmoreoption.ReadMoreOption
import com.fatron.network_module.models.response.partnerprofile.PartnerReviews
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemDoctorReviewBinding
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.getString


class DocReviewsAdapter : BaseRecyclerViewAdapter<DocReviewsAdapter.ViewHolder, PartnerReviews>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_doctor_review }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDoctorReviewBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemDoctorReviewBinding) : BaseViewHolder<PartnerReviews>(binding.root) {
        override fun onBind(item: PartnerReviews, position: Int) {
            binding.apply {
                reviewResponse = item
                addReadMore(item.review.getSafe(),tvDesc)
//                val readMoreOption = ReadMoreOption.Builder(root.context) .textLength(2, ReadMoreOption.TYPE_LINE) // OR
//                    //.textLength(300, ReadMoreOption.TYPE_CHARACTER)
//                    .moreLabel("read more")
//                    .lessLabel("read less")
//                    .moreLabelColor( root.context.getColor(R.color.grey))
//                    .lessLabelColor( root.context.getColor(R.color.grey))
//                    .labelUnderLine(true)
//                    .expandAnimation(true)
//                    .build();
//
//                readMoreOption.addReadMoreTo(
//                    tvDesc,
//                    Html.fromHtml(item.review+ ' ', Html.FROM_HTML_MODE_LEGACY)
//                )
            }
        }

        private fun addReadMore(text: String, textView: TextView) {


            var ss:SpannableString=SpannableString(text.substring(0, text.length))
            if(text.length>70){
             ss   = SpannableString(text.substring(0, 70) + "...  ${ApplicationClass.mGlobalData?.globalString?.readMore}")
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    addReadLess(text, textView)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ds.color = binding.root.context.resources.getColor(R.color.grey,null)
                    } else {
                        ds.color =  binding.root.context.resources.getColor(R.color.grey,null)
                    }
                }
            }

            ss.setSpan(clickableSpan, ss.length - 10, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            textView.text = ss
            textView.movementMethod = LinkMovementMethod.getInstance()
        }

        private fun addReadLess(text: String, textView: TextView) {
            val ss = SpannableString("$text   ${ApplicationClass.mGlobalData?.globalString?.readLess}")
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    addReadMore(text, textView)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ds.color = binding.root.context.resources.getColor(R.color.grey,null)
                    } else {
                        ds.color =  binding.root.context.resources.getColor(R.color.grey,null)
                    }
                }
            }
            ss.setSpan(clickableSpan, ss.length - 10, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            textView.text = ss
            textView.movementMethod = LinkMovementMethod.getInstance()
        }
    }


}