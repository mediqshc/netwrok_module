package com.homemedics.app.ui.adapter

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.homemedics.app.model.ConnectionType
import com.homemedics.app.ui.fragment.profile.FamilyConnectionsFragment

class FamilyProfileTabsPagerAdapter(fm: FragmentManager, lifeCycle: Lifecycle): FragmentStateAdapter(fm, lifeCycle) {
    private val connectionTypeList = arrayListOf(ConnectionType.Connected, ConnectionType.Received, ConnectionType.Sent)

    private val fragments = ArrayList<Fragment>().apply {
        add(FamilyConnectionsFragment())
        add(FamilyConnectionsFragment())
        add(FamilyConnectionsFragment())
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position].apply {
            arguments = Bundle().apply {
                putSerializable("type", connectionTypeList[position])
            }
        }
    }
}