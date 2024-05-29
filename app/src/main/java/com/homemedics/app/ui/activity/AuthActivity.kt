package com.homemedics.app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseActivity
import com.homemedics.app.databinding.ActivityAuthBinding
import com.homemedics.app.utils.Enums
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : BaseActivity() {
    private lateinit var mBinding: ActivityAuthBinding

    override fun getActivityLayout(): Int = R.layout.activity_auth

    override fun getViewBinding() {
        ApplicationClass.localeManager.updateLocaleData(this, TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key))
        mBinding = binding as ActivityAuthBinding

    }

    override fun setClickListeners() {

    }

    override fun onClick(p0: View?) {

    }

    fun navigateToHome(){
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("partnerCheck", true)
        startActivity(intent)

        finish()
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
}