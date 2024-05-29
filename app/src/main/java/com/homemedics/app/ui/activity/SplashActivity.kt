package com.homemedics.app.ui.activity

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.view.View
import androidx.lifecycle.liveData
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.meta.MetaDataResponse
import com.fatron.network_module.models.response.user.UserResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseActivity
import com.homemedics.app.databinding.ActivitySplashBinding
import com.homemedics.app.receiver.NetworkChangeReceiver
import com.homemedics.app.utils.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SplashActivity : BaseActivity() {
    private lateinit var mBinding: ActivitySplashBinding

    override fun getActivityLayout() = R.layout.activity_splash
    private var metaApiDone = false
    private var profileApiDone = false

    override fun getViewBinding() {
        ApplicationClass.localeManager.updateLocaleData(
            this,
            TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
        )
        mBinding = binding as ActivitySplashBinding
//        registerNetworkReceiver()
        getFCMToken()

    }

    override fun onResume() {
        super.onResume()
//        fetchAndSetLanguage()
        if (isOnline(this)) {
            metaApiCall()
        } else {
            DialogUtils(this)
                .showSingleButtonAlertDialog(
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError ?: getString(
                        R.string.error_internet
                    ),
                    message = ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg
                        ?: getString(R.string.internet_error),
                    buttonCallback = {},
                )
        }
        Timber.e("Resume")
        val user = DataCenter.getUser()
        if (user != null)
            getProfile()
        else profileApiDone = true
    }

    private fun metaDataApiCall() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.metaDataCall()
        emit(result)
        emit(ResponseResult.Complete)

    }

    private fun getProfileApiCall() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getProfileCall()
        emit(result)
        emit(ResponseResult.Complete)

    }

    private fun navigateIfPossible() {
        if (metaApiDone && profileApiDone) {
            val intent = Intent(this@SplashActivity, HomeActivity::class.java)
            intent.putExtra("partnerCheck", true)
            startActivity(intent)
            finish()
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.e("Fetching FCM registration token failed: " + task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            TinyDB.instance.putString(Enums.TinyDBKeys.FCM_TOKEN.key, token)

            // Log and toast
            Timber.e("FCM token: $token")
        })
    }


    private fun metaApiCall() {
        metaDataApiCall().observe(this) { it ->
            when (it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<MetaDataResponse>

                    this.statusBarColor(R.color.white, false)
                    val localeItem = response.data?.tenantLanguageItem?.find {
                        it.shortName == TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
                    }
//                    if (localeItem == null) {
//                        TinyDB.instance.putString(
//                            Enums.TinyDBKeys.LOCALE.key,
//                            DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
//                        )
//
//                        ApplicationClass.localeManager.updateLocaleData(
//                            this,
//                            TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
//                        )
//                        recreate()
//                    }else {

                    response.data?.genders = response.data?.genders?.map { gender ->
                        GenericItem(
                            genericItemId = gender.genericItemId,
                            genericItemName = gender.genericItemName?.replaceFirstChar { it.uppercase() })
                    }
                    response.data?.phoneCategories =
                        response.data?.phoneCategories?.map { phone ->
                            GenericItem(
                                genericItemId = phone.genericItemId,
                                genericItemName = phone.genericItemName?.replaceFirstChar { it.uppercase() })
                        }
                    response.data?.locationCategories =
                        response.data?.locationCategories?.map { loc ->
                            GenericItem(
                                genericItemId = loc.genericItemId,
                                genericItemName = loc.genericItemName?.replaceFirstChar { it.uppercase() })
                        }
                    response.data?.familyMemberRelations =
                        response.data?.familyMemberRelations?.map { item ->
                            GenericItem(
                                genericItemId = item.genericItemId,
                                genericItemName = item.genericItemName?.replaceFirstChar { it.uppercase() })
                        }
                    response.data?.let { it1 ->
                        tinydb.putObject(
                            Enums.TinyDBKeys.META.key,
                            it1
                        )
                    }
                    metaApiDone = true
                    when (response.update_available) {
                        1 -> {
                            showSoftUpdateDialog()
                        }
                        2 -> {
                            isStop = true
                            showForceUpdateDialog()
                        }
                        else -> {
                            navigateIfPossible()
                        }
                    }
//                    }
                }
                is ResponseResult.Failure -> {
                    try {
//                        DialogUtils(this)
//                            .showSingleButtonAlertDialog(
//                                message = it.error.message.getSafe(),
//                                buttonCallback = {},
//                            )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                is ResponseResult.ApiError -> {
                    if (it.generalResponse.status == 422 && it.generalResponse.message?.contains(getString(R.string.tenant_error), true).getSafe()) {
                        DialogUtils(this)
                            .showSingleButtonAlertDialog(
                                title = ApplicationClass.mGlobalData?.globalString?.information.getSafe(),
                                message = ApplicationClass.mGlobalData?.dialogsStrings?.tenantDisable.getSafe(),
                                buttonCallback = {},
                            )
                    } else {
                        DialogUtils(this)
                            .showSingleButtonAlertDialog(
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                buttonCallback = {},
                            )
                    }
                }
                is ResponseResult.Pending -> {
                }
                is ResponseResult.Complete -> {
                }
            }
        }
    }

    var isStop = false

    private fun getProfile() {
        if (isOnline(this)) {
            getProfileApiCall().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<UserResponse>
                        response.data?.let { it1 ->
                            tinydb.putObject(
                                Enums.TinyDBKeys.USER.key,
                                it1
                            )
                        }
                        profileApiDone = true
                        if (response.data != null && response.data?.messageBookingCount != 0 && TinyDB.instance.getString(
                                Enums.TinyDBKeys.CHATTOKEN.key
                            )
                                .isNotEmpty() && ApplicationClass.twilioChatManager?.conversationClients == null
                        ) {
                            ApplicationClass.twilioChatManager?.initializeWithAccessToken(
                                applicationContext,
                                TinyDB.instance.getString(
                                    Enums.TinyDBKeys.CHATTOKEN.key
                                ).getSafe(),
                                TinyDB.instance.getString(
                                    Enums.TinyDBKeys.FCM_TOKEN.key
                                )
                            )
                        }
                        if (isStop.not())
                            navigateIfPossible()
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(this)
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(this)
                            .showSingleButtonAlertDialog(
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.Pending -> {
                    }
                    is ResponseResult.Complete -> {
                    }
                    else -> {
                        hideLoader()
                    }
                }
            }
        }
    }

    override fun setClickListeners() {

    }

    override fun onClick(p0: View?) {

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////

    private val networkReceiver = NetworkChangeReceiver()

    private fun registerNetworkReceiver() {
        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onDestroy() {
        super.onDestroy()
//        unregisterReceiver(networkReceiver)
    }

    private fun showSoftUpdateDialog() {
        val url = ApplicationClass.mGlobalData?.globalString?.appUpdateUrlAndroid.getSafe()
        com.homemedics.app.utils.Constants.showUpdateDialog = false
        DialogUtils(this).showDoubleButtonsAlertDialog(
            title = ApplicationClass.mGlobalData?.dialogsStrings?.appUpdateTitle.getSafe(),
            message = ApplicationClass.mGlobalData?.dialogsStrings?.appUpdateDesc.getSafe(),
            positiveButtonStringText = ApplicationClass.mGlobalData?.dialogsStrings?.update.getSafe(),
            negativeButtonStringText = ApplicationClass.mGlobalData?.dialogsStrings?.later.getSafe(),
            buttonCallback =  {
                navigateToPlayStore(this, url) //packageName
            },
            negativeButtonCallback = {
                navigateIfPossible()
            },
            cancellable = false,
        )
    }

    private fun showForceUpdateDialog() {
        val url = ApplicationClass.mGlobalData?.globalString?.appUpdateUrlAndroid.getSafe()
        DialogUtils(this).showSingleButtonAlertDialog(
            title = ApplicationClass.mGlobalData?.dialogsStrings?.appUpdateTitle.getSafe(),
            message = ApplicationClass.mGlobalData?.dialogsStrings?.appUpdateDesc.getSafe(),
            buttonOneText = ApplicationClass.mGlobalData?.dialogsStrings?.update.getSafe(),
            buttonCallback =  {
                navigateToPlayStore(this, url) //packageName
            },
            cancellable = false
        )
    }
}