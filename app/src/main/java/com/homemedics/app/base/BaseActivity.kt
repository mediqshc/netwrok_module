package com.homemedics.app.base

import android.app.Dialog
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.os.Process
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fatron.network_module.models.request.auth.EconOtpRequest
import com.fatron.network_module.models.request.auth.LogoutRequest
import com.fatron.network_module.models.response.EconResponse
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.meta.MetaDataResponse
import com.fatron.network_module.models.response.notification.NotificationCountResponse
import com.fatron.network_module.models.response.user.UserResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.fatron.newtork_module.Constants
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.firebase.FirebaseRemoteConfigsWrapper
import com.homemedics.app.firebase.RemoteConfigKeys
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.activity.AuthActivity
import com.homemedics.app.ui.activity.SplashActivity
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.AuthViewModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity(), View.OnClickListener {

    private val viewModel: AuthViewModel by viewModels()
    private var isLoaderClose: Boolean = false
    protected lateinit var tinydb: TinyDB
    protected lateinit var binding: ViewDataBinding
    private val dialogUtils: DialogUtils by lazy { DialogUtils(this@BaseActivity) }
    private lateinit var loaderDialog: Dialog
    val metaData: MetaDataResponse? by lazy {
        TinyDB.instance.getObject(
            Enums.TinyDBKeys.META.key,
            MetaDataResponse::class.java
        ) as MetaDataResponse?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.getInt("pid", -1) == Process.myPid()) {
                Log.e(BaseActivity::class.java.simpleName, getString(R.string.app_not_killed))
            } else {
                Log.e(BaseActivity::class.java.simpleName, getString(R.string.app_killed))
                restartApp()
            }
        }

        tinydb = TinyDB.instance
        init()

        registerLocalBroadcastReceiver()

    }


    private fun registerLocalBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(
                receiverApiFailureListener,
                IntentFilter(Constants.BROADCAST_ACTION_API_FAILURE)
            );
    }

    private fun unregisterLocalBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverApiFailureListener)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putInt("pid", Process.myPid())
    }

    private fun init() {
        initLoader()

        binding = DataBindingUtil.setContentView(this, getActivityLayout())

        getViewBinding()

        setClickListeners()

        firebaseRemoteConfigsWrapper = FirebaseRemoteConfigsWrapper()

        firebaseRemoteConfigsWrapper.fetchConfigs().observe(this) { it ->
            Log.e(SplashActivity::class.java.simpleName, "firebase remote config data is ")
            if (it.isSuccessful) fetchAndSetLanguage()
            else
                firebaseRemoteConfigsWrapper.initialize().observe(this) {
                    if (it.isSuccessful)
                        fetchAndSetLanguage()
                }
        }
    }

    abstract fun getActivityLayout(): Int

    abstract fun getViewBinding()

    abstract fun setClickListeners()


    override fun onDestroy() {
        unregisterLocalBroadcastReceiver()
        super.onDestroy()

    }

    fun isUserLoggedIn(): Boolean =
        TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.TOKEN_USER.key)
            .isNotEmpty()

    fun logout() {
        if (isOnline(this)) {
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(
                MyFirebaseMessagingService.callNotificationId
            )
            logoutApi()
        } else {
            DialogUtils(this)
                .showSingleButtonAlertDialog(
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                    message = ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }


    fun unSubscribeTelcoPackage() {
        if (isOnline(this)) {
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(
                MyFirebaseMessagingService.callNotificationId
            )
            unSubscribe()
        } else {
            DialogUtils(this)
                .showSingleButtonAlertDialog(
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                    message = ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }


    ///////////////////////////////////////////////////

    private fun initLoader() {
        loaderDialog = Dialog(this@BaseActivity)
        loaderDialog.apply {
            window?.setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        this@BaseActivity,
                        android.R.color.transparent
                    )
                )
            )

            setCancelable(false)
        }

        loaderDialog.setContentView(R.layout.layout_loader)


    }

    fun showLoader() {
        try {
            loaderDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideLoader() {
        try {
            loaderDialog.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun closeKeypad() {
        val view = this.currentFocus
        view?.let { v ->
            val imm =
                this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    fun showKeypad() {
        val view = this.currentFocus
        view?.let { v ->
            val imm =
                this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(v, 0)
        }
    }

    val receiverApiFailureListener: BroadcastReceiver
        get() = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    try {
                        val code = it.getIntExtra(Constants.KEY_API_ERROR_BROADCAST_CODE, 0)
                        val updatedVersionCode =
                            it.getIntExtra(Constants.KEY_API_ERROR_UPDATED_VERSION_CODE, 0)
                        val msg = it.getStringExtra(Constants.KEY_API_ERROR_BROADCAST_MESSAGE)
                        when (code) {
                            Constants.AUTH_CODE -> {
                                clearTinyDb()
                                val intent = Intent(this@BaseActivity, AuthActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            Constants.SOFT_UPDATE -> {
                                checkUpdate(code)
                            }
                            Constants.FORCE_UPDATE -> {
                                checkUpdate(code)
                            }
                            else -> {

                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

    private fun clearTinyDb() {

        TinyDB.instance.remove(com.fatron.network_module.utils.Enums.TinyDBKeys.TOKEN_USER.key)
        TinyDB.instance.remove(com.fatron.network_module.utils.Enums.TinyDBKeys.USER.key)
        TinyDB.instance.remove(com.fatron.network_module.utils.Enums.TinyDBKeys.NAME.key)

    }

    private fun showForceUpdateDialog() {
        val url = ApplicationClass.mGlobalData?.globalString?.appUpdateUrlAndroid.getSafe()
        dialogUtils.showForceUpdateAlertDialog(
            title = ApplicationClass.mGlobalData?.dialogsStrings?.appUpdateTitle.getSafe(),
            message = ApplicationClass.mGlobalData?.dialogsStrings?.appUpdateDesc.getSafe(),
            buttonOneText = ApplicationClass.mGlobalData?.dialogsStrings?.update.getSafe(),
            buttonCallback = {
                navigateToPlayStore(this, url) //packageName
            },
            cancellable = false
        )
    }

    private fun showSoftUpdateDialog() {
        val url = ApplicationClass.mGlobalData?.globalString?.appUpdateUrlAndroid.getSafe()
        com.homemedics.app.utils.Constants.showUpdateDialog = false
        dialogUtils.showDoubleButtonsAlertDialog(
            title = ApplicationClass.mGlobalData?.dialogsStrings?.appUpdateTitle.getSafe(),
            message = ApplicationClass.mGlobalData?.dialogsStrings?.appUpdateDesc.getSafe(),
            positiveButtonStringText = ApplicationClass.mGlobalData?.dialogsStrings?.update.getSafe(),
            negativeButtonStringText = ApplicationClass.mGlobalData?.dialogsStrings?.later.getSafe(),
            buttonCallback = {
                navigateToPlayStore(this, url) //packageName
            },
            negativeButtonCallback = {
            },
            cancellable = false,
        )
    }

    fun checkUpdate(updateAvailable: Int, updatedVersionCode: Int? = null): Boolean {
        if (/*updatedVersionCode <= BuildConfig.VERSION_CODE ||*/ com.homemedics.app.utils.Constants.showUpdateDialog.not())
            return false

        if ((this is BaseActivity)) {
            com.homemedics.app.utils.Constants.showUpdateDialog = false

            return when (updateAvailable) {
                Constants.FORCE_UPDATE -> {
                    if (this !is SplashActivity) {
                        showForceUpdateDialog()
                    }
                    false
                }
                Constants.SOFT_UPDATE -> {
                    if (this !is SplashActivity) {
                        showSoftUpdateDialog()
                    }
                    true
                }
                else -> {
                    true
                }
            }
        } else {
            return true
        }
    }

    // LANGUAGE SETTINGS ///////////////////////////////////////////////////////////////////////////
    private lateinit var firebaseRemoteConfigsWrapper: FirebaseRemoteConfigsWrapper
    fun fetchAndSetLanguage() {

        val langString =
            when {
                TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key) == DefaultLocaleProvider.DEFAULT_LOCALE_UR -> {
                    RemoteConfigKeys.URDU_LANGUAGE
                }
                TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key) == DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_AR -> {
                    RemoteConfigKeys.ARABIC_LANGUAGE
                }
                TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key) == DefaultLocaleProvider.DEFAULT_LOCALE_EN -> {
                    RemoteConfigKeys.ENGLISH_LANGUAGE
                }
                else -> {
                    RemoteConfigKeys.ENGLISH_LANGUAGE
                }
            }

        try {
            val language = getConfigurationData(
                firebaseRemoteConfigsWrapper.getConfigString(
                    langString
                )
            )
            if (language != null)
                ApplicationClass.mGlobalData = language
        } catch (e: Exception) {
            print(e.printStackTrace())
        }

    }

    fun getConfigurationData(json: String): RemoteConfigLanguage? {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter: JsonAdapter<RemoteConfigLanguage> =
            moshi.adapter(RemoteConfigLanguage::class.java)
        Log.e(SplashActivity::class.java.simpleName, "getConfigurationData: $json")
        return adapter.fromJson(json)
    }

    fun setLocale(lang: String) {

        val config = resources.configuration
        val locale = Locale(lang)
        Locale.setDefault(locale)
        config.setLocale(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)

    }

    fun restartApp() {
        val intent = Intent(this, SplashActivity::class.java)
        this.startActivity(intent)
        finishAffinity()
    }

    private fun unSubscribe() {
        val user = TinyDB.instance.getObject(
            Enums.TinyDBKeys.USER.key,
            UserResponse::class.java
        ) as UserResponse

        val request = EconOtpRequest(
            phone = user.phoneNumber.toString(),
            network = user.network.toString()

        )
        viewModel.unSubscribeEconPackage(request).observe(this) {
            when (it) {
                is ResponseResult.Success -> {
                    val data = it.data as ResponseGeneral<*>
                    val response = data.data as EconResponse
                    if (response.status == 0) {
                        logoutApi()
                    } else {
                        //in case status is not zero and unsub is not successfull
                        DialogUtils(this)
                            .showSingleButtonAlertDialog(
                                message = response.result.toString(),
                                buttonCallback = {

                                },
                            )
                    }
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
                    showLoader()
                }
                is ResponseResult.Complete -> {
                    hideLoader()
                }
                else -> {}
            }
        }
    }

    private fun logoutApi() {
        val request = LogoutRequest(
            deviceToken = getAndroidID(this)
        )
        viewModel.logout(request).observe(this) {
            when (it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<*>
                    if (response.status in 200..299) {

                        startActivity(Intent(this, AuthActivity::class.java))
                    }
                    ApplicationClass.twilioChatManager?.apply {
                        conversationClients?.shutdown()
                        conversationClients?.removeAllListeners()
                        conversationClients = null
                        conversationClientCheck = false
                        conversation?.removeAllListeners()
                        conversation = null
                        conversationSid = ""
                        _messages.value = null

                    }

                    TinyDB.instance.remove(Enums.TinyDBKeys.USER.key)
                    TinyDB.instance.remove(Enums.TinyDBKeys.CHATTOKEN.key)
                    TinyDB.instance.remove(Enums.TinyDBKeys.TOKEN_USER.key)
//                    TinyDB.instance.remove(com.homemedics.app.utils.Enums.PlannerMode.PLANNER_MODE.key)
                    TinyDB.instance.remove(Enums.TinyDBKeys.BOOKING_ID.key)
                    TinyDB.instance.remove(Enums.TinyDBKeys.LAB_TEST_BOOKING_ID.key)
                    TinyDB.instance.remove(com.homemedics.app.utils.Enums.FirstTimeUnique.FIRST_TIME_UNIQUE.key)
                    TinyDB.instance.remove(com.homemedics.app.utils.Enums.PlannerMode.PLANNER_MODE.key)
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
                    showLoader()
                }
                is ResponseResult.Complete -> {
                    hideLoader()
                }
                else -> {}
            }
        }
    }


}