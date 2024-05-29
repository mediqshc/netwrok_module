package com.homemedics.app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.fatron.network_module.models.request.bdc.BookConsultationRequest
import com.fatron.network_module.models.response.bdc.PartnerSlotsResponse
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.utils.TinyDB
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseActivity
import com.homemedics.app.databinding.ActivityCheckoutBinding
import com.homemedics.app.utils.Enums
import com.homemedics.app.viewmodel.CheckoutViewModel


class CheckoutActivity : BaseActivity() {
    private lateinit var mBinding: ActivityCheckoutBinding
    private val checkoutViewModel: CheckoutViewModel by viewModels()

    override fun onBackPressed() {
        val currentDest = findNavController(R.id.fCheckoutNav).currentDestination
        if(currentDest?.id == R.id.checkoutConfirmationFragment
            || currentDest?.id == R.id.myOrderFragment //when coming from view details order listing after checkout
        )
        {
            val intent = Intent(this, HomeActivity::class.java)
             finishAffinity()
             startActivity(intent)
        }
        else {
            super.onBackPressed()
        }
    }

    override fun getActivityLayout(): Int = R.layout.activity_checkout

    override fun getViewBinding() {
        mBinding = binding as ActivityCheckoutBinding
        init()
        ApplicationClass.localeManager.updateLocaleData(this, TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key))

    }

    override fun setClickListeners() {

    }

    override fun onClick(v: View?) {

    }

    private fun init() {
        handleIntent()
        initNavigation()

    }

    private fun initNavigation() {
        val myNavHostFragment: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.fCheckoutNav) as NavHostFragment
        val inflater = myNavHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.checkout_navigation)

        myNavHostFragment.navController.graph = graph
    }

    private fun handleIntent(){
        try {
            intent?.let {
                checkoutViewModel.partnerProfileResponse =
                    Gson().fromJson(intent.getStringExtra(Enums.BundleKeys.partnerProfileResponse.key), PartnerProfileResponse::class.java)
                checkoutViewModel.partnerSlotsResponse =
                    Gson().fromJson(intent.getStringExtra(Enums.BundleKeys.partnerSlotsResponse.key), PartnerSlotsResponse::class.java)
                 checkoutViewModel.bookConsultationRequest =
                    Gson().fromJson(intent.getStringExtra(Enums.BundleKeys.bookConsultationRequest.key), BookConsultationRequest::class.java)
            }
        }
        catch (e:Exception){e.printStackTrace()}
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