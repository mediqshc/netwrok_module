package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.homemedics.app.R
import com.homemedics.app.databinding.BanneritemfileBinding

class imageViewPagerAdapter(val imagelist: ArrayList<Int>):RecyclerView.Adapter<imageViewPagerAdapter.ViewPagerViewHolder>() {


    inner class ViewPagerViewHolder(val binding:BanneritemfileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(imageUrl: Int) {

            Glide.with(binding.root.context)
                .load(imageUrl)
                .error(R.drawable.dummy_home_banner)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imageView)
        }

    }

    override fun getItemCount(): Int = imagelist.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {

        val binding=BanneritemfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)


        return ViewPagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {

        holder.setData(imagelist[position])
    }

}