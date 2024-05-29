package com.homemedics.app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseActivity
import com.homemedics.app.databinding.ActivityEmrBinding
import com.homemedics.app.utils.Enums
import com.homemedics.app.utils.FileUtils
import com.homemedics.app.viewmodel.EMRViewModel


class EMRActivity : BaseActivity() {
    private lateinit var mBinding: ActivityEmrBinding
    private val emrViewModel: EMRViewModel by viewModels()
    val fileUtils = FileUtils()

    override fun getActivityLayout(): Int = R.layout.activity_emr

    override fun getViewBinding() {
        mBinding = binding as ActivityEmrBinding
        ApplicationClass.localeManager.updateLocaleData(this, TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key))

    }

    override fun setClickListeners() {
        init()

    }

    override fun onClick(v: View?) {

    }

    private fun init() {
        fileUtils.init(this)
        initNavigation()
    }

    private fun initNavigation() {
        val myNavHostFragment: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.fEmrNav) as NavHostFragment
        val inflater = myNavHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.customer_emr_navigation)

        myNavHostFragment.navController.graph = graph
    }

    override fun onResume() {
        super.onResume()
        try {
            fetchAndSetLanguage()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
        try {
            fetchAndSetLanguage()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java)
        finishAffinity()
        startActivity(intent)
    }

}