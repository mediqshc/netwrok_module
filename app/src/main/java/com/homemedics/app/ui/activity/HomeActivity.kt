package com.homemedics.app.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.liveData
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.fatron.network_module.models.request.notification.BookingDetailsNotification
import com.fatron.network_module.models.request.notification.PropertiesObj
import com.fatron.network_module.models.request.orders.MyOrdersRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.user.UserResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.BuildConfig.VERSION_NAME
import com.homemedics.app.R
import com.homemedics.app.base.BaseActivity
import com.homemedics.app.databinding.ActivityHomeBinding
import com.homemedics.app.databinding.DialogAddReviewBinding
import com.homemedics.app.interfaces.ContactUsInterface
import com.homemedics.app.locale.DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
import com.homemedics.app.model.PNModels
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.ui.fragment.home.HomeFragmentDirections
import com.homemedics.app.utils.*
import com.homemedics.app.utils.Constants.BOOKING_ID
import com.homemedics.app.viewmodel.EMRViewModel
import com.homemedics.app.viewmodel.HomeViewModel
import com.homemedics.app.viewmodel.ProfileViewModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class HomeActivity : BaseActivity() {
    val fileUtils = FileUtils()

    companion object {
        lateinit var navigationView: NavigationView
    }

    private var isPartnerMode = false
    private lateinit var mBinding: ActivityHomeBinding
    private val profileViewModel: ProfileViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var sbPartnerMode: SwitchCompat
    private val emrViewModel: EMRViewModel by viewModels()
    private var newIntent: Intent? =
        null //on logout without app close, intent was not working as per requirement so adding this

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        //do nothing for now
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S || "S" == Build.VERSION.CODENAME) {
            // Android 12 or Android 12 Beta
            if (NotificationManagerCompat.from(this).areNotificationsEnabled().not()) {
                displayNeverAskAgainDialog(getString(R.string.m_notification_permissions))
            }
        } else {
            when {
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
                else -> {
                    if (NotificationManagerCompat.from(this).areNotificationsEnabled().not()) {
//                    displayNeverAskAgainDialog(getString(R.string.m_notification_permissions))
                        requestPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                }
            }
        }
    }

    private fun displayNeverAskAgainDialog(message: String) {
        DialogUtils(this).showDoubleButtonsAlertDialog(
            message = message,
            positiveButtonStringText = ApplicationClass.mGlobalData?.dialogsStrings?.permitManual.getSafe(),
            negativeButtonStringText = ApplicationClass.mGlobalData?.globalString?.close.getSafe(),
            buttonCallback = {
                val settingsIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(
                        Settings.EXTRA_APP_PACKAGE,
                        applicationContext.packageName
                    )
                startActivity(settingsIntent)
            },
            cancellable = false
        )
    }

    override fun getActivityLayout(): Int = R.layout.activity_home

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        homeViewModel._homeActivityIntent.value = intent

        newIntent = intent

        handlePNRedirections()
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

    override fun onResume() {
        super.onResume()

//        requestNotificationPermission()
        try {
            fetchAndSetLanguage()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        isPartnerMode =
            tinydb.getBoolean(com.homemedics.app.utils.Enums.PlannerMode.PLANNER_MODE.key)

        //isPartnerMode = DataCenter.getUser().isDoctor() || DataCenter.getUser()
        //   .isMedicalStaff() //to keep default state as active if doctor logs in

        sbPartnerMode.isChecked = isPartnerMode

        if (isUserLoggedIn() && DataCenter.getUser().isCustomer()) {
            isPartnerMode = false
            tinydb.putBoolean(
                com.homemedics.app.utils.Enums.PlannerMode.PLANNER_MODE.key,
                isPartnerMode
            )
        }

        initDrawer()

//        setDrawerItems()
        // call end review api
        if (intent?.hasExtra(Enums.BundleKeys.bookingId.key).getSafe()) {
            val customerId = intent?.getIntExtra(Enums.BundleKeys.id.key, 0)
            bookingId =
                intent.getStringExtra(com.homemedics.app.utils.Enums.BundleKeys.bookingId.key)

            if (customerId == DataCenter.getUser()?.id)
                showReviewDialog()
            intent.removeExtra(com.homemedics.app.utils.Enums.BundleKeys.bookingId.key)
            intent.removeExtra(com.homemedics.app.utils.Enums.BundleKeys.id.key)
        }


        navigateAccordingToPartnerApplicationStatusIfPossible()
    }

    override fun getViewBinding() {
        mBinding = binding as ActivityHomeBinding

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isUserLoggedIn())
            homeViewModel.updateFCMToken()
        ApplicationClass.localeManager.updateLocaleData(
            this,
            TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        )

        init()
    }

    override fun setClickListeners() {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                closeDrawer()
            }
            R.id.ivLogIn -> {
                if (isUserLoggedIn()) {
                    DialogUtils(this)
                        .showDoubleButtonsAlertDialog(
                            message = ApplicationClass.mGlobalData?.dialogsStrings?.logoutMsg.getSafe(),
                            buttonCallback = {
                                profileViewModel.flushDataOnLogout()
                                homeViewModel.linkedAccounts.clear()
                                logout()
                            },
                            negativeButtonCallback = {},
                            cancellable = false
                        )
                } else {
                    closeDrawer()
                    startActivity(Intent(this, AuthActivity::class.java))
                }
            }
        }
    }

    var bookingId: String? = null

    private fun init() {
        requestNotificationPermission()
        fileUtils.init(this)
        isPartnerMode =
            tinydb.getBoolean(com.homemedics.app.utils.Enums.PlannerMode.PLANNER_MODE.key)
        initNavigation()

        navigationView = findViewById(R.id.navigationView)
        sbPartnerMode = navigationView.getHeaderView(0).findViewById(R.id.sbPartnerMode)

        navHeaderListeners()
        sbPartnerMode.isChecked = isPartnerMode
        initDrawer()
    }

    private fun navHeaderListeners() {
        navigationView.getHeaderView(0).apply {
            findViewById<AppCompatImageView>(R.id.ivLogIn).setOnClickListener(this@HomeActivity)
            findViewById<AppCompatImageView>(R.id.ivBack).setOnClickListener(this@HomeActivity)
            sbPartnerMode.setOnCheckedChangeListener { _, isChecked ->
                tinydb.putBoolean(
                    Enums.PlannerMode.PLANNER_MODE.key,
                    isChecked
                )
                setDrawerItems()
                homeViewModel.isDoctorViewEnabled(isChecked)
            }
        }

    }

    fun gotoLogin() {
        closeDrawer()
        startActivity(Intent(this, AuthActivity::class.java))
    }

    @SuppressLint("SuspiciousIndentation")
    private fun initDrawer() {
        setDrawerItems()
        var lastTimeClicked = 0L
        mBinding.apply {
            (drawerLayout.findViewById(R.id.navigationView) as NavigationView).setNavigationItemSelectedListener {
                if (SystemClock.elapsedRealtime() - lastTimeClicked < 1000) {
                    return@setNavigationItemSelectedListener false
                }
                lastTimeClicked = SystemClock.elapsedRealtime()
                when (it.itemId) {
                    R.id.mBecomePartner -> {
                        closeDrawer()
                        if (isUserLoggedIn()) {
                            findNavController(R.id.fHomeNav).navigate(HomeFragmentDirections.actionHomeFragmentToBecomePartnerNavigation())
                        } else
                            startActivity(Intent(this@HomeActivity, AuthActivity::class.java))
                    }
                    R.id.mPersonalProfile -> {
                        closeDrawer()
                        profileViewModel.addresses.clear()
                        profileViewModel.contacts.clear()
                        profileViewModel._userProfile.value =
                            tinydb.getObject(
                                com.fatron.network_module.utils.Enums.TinyDBKeys.USER.key,
                                UserResponse::class.java
                            ) as UserResponse
                        findNavController(R.id.fHomeNav).navigate(HomeFragmentDirections.actionHomeFragmentToPersonalProfileFragment())
                    }
                    R.id.mMyPartnerProfile -> {
                        closeDrawer()
                        findNavController(R.id.fHomeNav).navigate(HomeFragmentDirections.actionHomeFragmentToPartnerProfileNavigation())
                    }
                    R.id.mMyConnections, R.id.mAssociation -> {
                        closeDrawer()
                        val bundle = Bundle().apply {
                            putBoolean(
                                com.homemedics.app.utils.Enums.PlannerMode.PLANNER_MODE.key,
                                sbPartnerMode.isChecked
                            )
                        }
                        findNavController(R.id.fHomeNav).navigate(
                            R.id.action_homeFragment_to_linked_account_navigation,
                            bundle
                        )
                    }
                    R.id.mMyMedicalRecords -> {
                        closeDrawer()
                        val emrIntent = Intent(this@HomeActivity, EMRActivity::class.java)
                        startActivity(emrIntent)
                    }
                    R.id.mMyAppointments -> {
                        closeDrawer()
                        findNavController(R.id.fHomeNav).navigate(HomeFragmentDirections.actionHomeFragmentToTaskNavigation())
                    }
                    R.id.mMyPlanner -> {
                        closeDrawer()
                        findNavController(R.id.fHomeNav).navigate(HomeFragmentDirections.actionHomeFragmentToPlanningScheduleNavigation())
                    }
                    R.id.mMyOrders -> {
                        closeDrawer()
                        findNavController(R.id.fHomeNav).navigate(HomeFragmentDirections.actionHomeFragmentToOrdersNavigation())
                    }
                    R.id.mLanguage -> {
                        try {
                            closeDrawer()
                            findNavController(R.id.fHomeNav).navigate(HomeFragmentDirections.actionHomeFragmentToSelectLanguageFragment())
                        } catch (e: Exception) {
                            print(e.printStackTrace())
                        }
                    }
                    R.id.mDoctorMessages, R.id.mPatientMessages -> {
                        val chatIntent = Intent(this@HomeActivity, ChatActivity::class.java)
                        startActivity(chatIntent)
                        finish()
                    }
                    R.id.mFaq -> {
                        closeDrawer()
                        findNavController(R.id.fHomeNav).navigate(HomeFragmentDirections.actionHomeFragmentToFaqFragment())
                    }
                    R.id.Contactus -> {
                        closeDrawer()
                        DialogUtils(this@HomeActivity).showImagePickerDialog(this@HomeActivity,
                            object : ContactUsInterface {
                                override fun onNumberpress() {
                                    super.onNumberpress()
                                    val intent = Intent(Intent.ACTION_DIAL)
                                    intent.data = Uri.parse("tel:051-111-3773-77")
                                    startActivity(intent)
                                }

                                override fun onEmailPress() {
                                    super.onEmailPress()
                                    val intent = Intent(Intent.ACTION_SENDTO)

                                    intent.data = Uri.parse("mailto:info@mediq.com.pk")
                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Subject")
                                    intent.putExtra(Intent.EXTRA_TEXT, "Body of the email")
                                    intent.setPackage("com.google.android.gm")

                                    if (intent.resolveActivity(packageManager) != null) {
                                        startActivity(intent)
                                    } else {
                                        // If Gmail is not available, open a chooser with other email apps
                                        val chooser =
                                            Intent.createChooser(intent, "Send email with")
                                        if (chooser.resolveActivity(packageManager) != null) {
                                            startActivity(chooser)
                                        } else {
                                            // Handle the case where no email app is available
                                        }
                                    }
                                }
                            })

                    }
                    R.id.mlogout -> {
                        closeDrawer()
                        DialogUtils(this@HomeActivity)
                            .showDoubleButtonsAlertDialog(
                                message = ApplicationClass.mGlobalData?.dialogsStrings?.logoutMsg.getSafe(),
                                buttonCallback = {
                                    profileViewModel.flushDataOnLogout()
                                    homeViewModel.linkedAccounts.clear()
                                    logout()
                                },
                                negativeButtonCallback = {},
                                cancellable = false
                            )


                    }

                    R.id.UnSubscribe -> {
                        closeDrawer()
                        DialogUtils(this@HomeActivity)
                            .showDoubleButtonsAlertDialog(
                                message = "By unsubscribing, you will no longer have access to our exclusive discounts and services. if you wish to proceed, please click the unsubscribe button below",
                                buttonCallback = {
                                    profileViewModel.flushDataOnLogout()
                                    homeViewModel.linkedAccounts.clear()
                                    unSubscribeTelcoPackage()
                                },
                                negativeButtonCallback = {},
                                cancellable = false
                            )


                    }


                }


                return@setNavigationItemSelectedListener false
            }
        }
    }

    fun gotoMyAppointments() {
        closeDrawer()
        findNavController(R.id.fHomeNav).navigate(HomeFragmentDirections.actionHomeFragmentToTaskNavigation())
    }

    fun gotoMyPlanner() {
        closeDrawer()
        findNavController(R.id.fHomeNav).navigate(HomeFragmentDirections.actionHomeFragmentToPlanningScheduleNavigation())
    }

    fun gotoPatientMessages() {
        closeDrawer()
        val chatIntent = Intent(this@HomeActivity, ChatActivity::class.java)
        startActivity(chatIntent)
        finish()
    }

    fun gotoMyAssociations() {
        closeDrawer()
        val bundle = Bundle().apply {
            putBoolean(
                com.homemedics.app.utils.Enums.PlannerMode.PLANNER_MODE.key,
                sbPartnerMode.isChecked
            )
        }
        findNavController(R.id.fHomeNav).navigate(
            R.id.action_homeFragment_to_linked_account_navigation,
            bundle
        )
    }

    fun enableDrawer(state: Boolean) {
        mBinding.drawerLayout.setDrawerLockMode(
            if (state)
                DrawerLayout.LOCK_MODE_UNLOCKED
            else
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        )
    }

    fun openDrawer() {
        mBinding.drawerLayout.openDrawer(GravityCompat.START)
    }

    fun closeDrawer() {
        mBinding.drawerLayout.closeDrawers()
    }

    private fun initNavigation() {
        val myNavHostFragment: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.fHomeNav) as NavHostFragment
        val inflater = myNavHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.home_navigation)

        myNavHostFragment.navController.graph = graph


        handlePNRedirections()
    }

    private fun navigateAccordingToPartnerApplicationStatusIfPossible() {
        //navigate according to partner application status
        if ((intent?.hasExtra("partnerCheck").getSafe() || newIntent?.hasExtra("partnerCheck")
                .getSafe()) && isUserLoggedIn()
        ) {

            intent?.removeExtra("partnerCheck")
            newIntent = null
            lifecycleScope.launch {
                delay(300)

                when (DataCenter.getUser()?.applicationStatusId) {
                    Enums.ApplicationStatus.UNDER_REVIEW.key,
                    Enums.ApplicationStatus.REJECTED.key -> {
                        findNavController(R.id.fHomeNav).navigate(HomeFragmentDirections.actionHomeFragmentToBecomePartnerNavigation())
                    }

                }
            }
        }
    }

    private fun handlePNRedirections() {
        try {
            fetchAndSetLanguage()
        } catch (e: Exception) {
            print(e.printStackTrace())
        }
        if (intent?.hasExtra(Enums.BundleKeys.pnNavType.key).getSafe()) {
            when (intent?.getIntExtra(Enums.BundleKeys.pnNavType.key, 0)
                .toString()) {
                Enums.NotificationChannelIds.TYPE_PARTNER_REQUEST_APPROVED.key -> {  //10
                    getProfile(
                        intent?.getIntExtra(Enums.BundleKeys.pnNavType.key, 0)
                            .toString()
                    )
//                    findNavController(R.id.fHomeNav).safeNavigate(R.id.action_homeFragment_to_partner_profile_navigation)
                }
                Enums.NotificationChannelIds.TYPE_BOOKING_CREATED.key, Enums.NotificationChannelIds.TYPE_BOOKING_COMPLETED.key,
                Enums.NotificationChannelIds.TYPE_HOME_VISIT_UPDATE.key, Enums.NotificationChannelIds.TYPE_PAYMENT_RECEIVED.key,
                Enums.NotificationChannelIds.TYPE_BOOKING_REMINDER.key, Enums.NotificationChannelIds.TYPE_BOOKING_CANCELLED.key,
                Enums.NotificationChannelIds.TYPE_BOOKING_RESCHEDULED_REQUEST.key,
                Enums.NotificationChannelIds.TYPE_HOME_HEALTHCARE_PAYMENT_CONFIRMATION.key,
                Enums.NotificationChannelIds.TYPE_HOME_VISIT_BOOKING_CONFIRMATION.key,
                Enums.NotificationChannelIds.TYPE_BOOKING_RESCHEDULED_ACCEPTED_BY_CUSTOMER.key,
                Enums.NotificationChannelIds.TYPE_DELIVERY_COMPLETED.key
                -> {
                    val intent = Gson().fromJson(
                        intent?.getStringExtra(Enums.BundleKeys.pnData.key),
                        PNModels::class.java
                    )
                    findNavController(R.id.fHomeNav).safeNavigate(
                        R.id.action_homeFragment_to_orders_navigation,
                        bundleOf(BOOKING_ID to intent.booking_id)
                    )
                }
                Enums.NotificationChannelIds.TYPE_MESSAGE_SESSION_EXPIRED.key, Enums.NotificationChannelIds.TYPE_MESSAGE_SESSION_START.key, Enums.NotificationChannelIds.TYPE_CHAT_MESSAGE.key -> {
                    redirectToChat()
                }
                Enums.NotificationChannelIds.TYPE_PARTNER_REQUEST_APPROVED.key, Enums.NotificationChannelIds.TYPE_PARTNER_REQUEST_PROCESSED.key, Enums.NotificationChannelIds.TYPE_PARTNER_REQUEST_REJECTED.key -> {
                    getProfile(
                        intent?.getIntExtra(Enums.BundleKeys.pnNavType.key, 0)
                            .toString()
                    )
                }
                Enums.NotificationChannelIds.TYPE_CUSTOMER_REGISTRATION_COMPLETE.key -> {
                    findNavController(R.id.fHomeNav).safeNavigate(
                        R.id.action_homeFragment_self
                    )
                }
                Enums.NotificationChannelIds.TYPE_BOOKING_RESCHEDULED_PROCESSED.key,
                Enums.NotificationChannelIds.TYPE_PARTNER_BOOKING_REMINDER.key, Enums.NotificationChannelIds.TYPE_BOOKING_RESCHEDULED_ACCEPTED.key -> {
                    val intent = Gson().fromJson(
                        intent?.getStringExtra(Enums.BundleKeys.pnData.key),
                        PNModels::class.java
                    )
                    findNavController(R.id.fHomeNav).safeNavigate(
                        R.id.action_homeFragment_to_task_navigation,
                        bundleOf(
                            BOOKING_ID to intent.booking_id,
                            "partnerServiceId" to intent?.partner_service_id,
                            "dutyId" to intent?.duty_id
                        )
                    )
                }
                Enums.NotificationChannelIds.TYPE_FAMILY_MEMBER_INVITED.key,
                Enums.NotificationChannelIds.TYPE_FAMILY_MEMBER_ADDED.key,
                Enums.NotificationChannelIds.TYPE_FAMILY_MEMBER_LINKED.key -> {
                    findNavController(R.id.fHomeNav).safeNavigate(
                        R.id.action_homeFragment_to_personalprofile_navigation,
                        bundleOf(
                            "fromNoti" to true,
                            "NotiType" to intent?.getIntExtra(Enums.BundleKeys.pnNavType.key, 0)
                        )
                    )
                }
                Enums.NotificationChannelIds.TYPE_MEDICAL_RECORD_SHARED.key, Enums.NotificationChannelIds.TYPE_EMR_REPORT_UPLOAD.key, Enums.NotificationChannelIds.TYPE_EMR_SHARED_WITH_DOCTOR.key -> {

                    val intent = Gson().fromJson(
                        intent?.getStringExtra(Enums.BundleKeys.pnData.key),
                        PNModels::class.java
                    )
                    emrViewModel.apply {
                        emrID = intent.booking_id.toInt().getSafe()
                        isPatient = true
                    }
                    findNavController(R.id.fHomeNav).safeNavigate(
                        R.id.action_home_to_customer_emr_consultation_navigation

                    )
                }
                Enums.NotificationChannelIds.TYPE_EMPLOYEE_LINKED.key,
                Enums.NotificationChannelIds.TYPE_CUSTOMER_LINKED.key -> {
                    findNavController(R.id.fHomeNav).safeNavigate(R.id.action_homeFragment_to_linked_account_navigation)
                }

                Enums.NotificationChannelIds.TYPE_WALK_IN_TRANSACTION.key,
                Enums.NotificationChannelIds.TYPE_WALK_IN_APPROVED.key,
                Enums.NotificationChannelIds.TYPE_WALK_IN_REJECTED.key,
                Enums.NotificationChannelIds.TYPE_WALK_IN_DOCUMENT.key,
                Enums.NotificationChannelIds.TYPE_WALK_IN_REQUEST.key, //walkin hospital store
                Enums.NotificationChannelIds.WALK_IN_REQUEST_CONFIRMED.key,
                Enums.NotificationChannelIds.WALK_IN_CANCELLED_CUSTOMER.key,
                Enums.NotificationChannelIds.WALK_IN_CANCELLED_ADMIN.key -> {
                    val intent = Gson().fromJson(
                        intent?.getStringExtra(Enums.BundleKeys.pnData.key),
                        PNModels::class.java
                    )

                    val intentProperties = Gson().fromJson(
                        intent.properties,
                        BookingDetailsNotification::class.java
                    )
                    val services =
                        metaData?.partnerServiceType?.find { it.id == intentProperties?.bookingDetail?.partnerServiceId }
                    when (services?.id) {
                        CustomServiceTypeView.ServiceType.WalkInPharmacy.id ->//Walk-In Pharmacy

                            findNavController(R.id.fHomeNav).safeNavigate(
                                R.id.action_homeFragment_to_walkin_order_details_navigation,
                                bundleOf(
                                    "fromNoti" to true,
                                    "bookingId" to intentProperties?.bookingDetail?.walkInPharmacyId
                                )
                            )
                        CustomServiceTypeView.ServiceType.WalkInHospital.id ->//Walk-In Hospital

                            findNavController(R.id.fHomeNav).safeNavigate(
                                R.id.action_homeFragment_to_walkin_hosp_order_details_navigation,
                                bundleOf(
                                    "fromNoti" to true,
                                    "bookingId" to intentProperties?.bookingDetail?.walkInHospitalId
                                )
                            )
                        CustomServiceTypeView.ServiceType.WalkInLaboratory.id ->//Walk-In Laboratory

                            findNavController(R.id.fHomeNav).safeNavigate(
                                R.id.action_homeFragment_to_walkin_lab_order_details_navigation,
                                bundleOf(
                                    "fromNoti" to true,
                                    "bookingId" to intentProperties?.bookingDetail?.walkInLaboratoryId
                                )
                            )
                    }
                }

                Enums.NotificationChannelIds.TYPE_CLAIM_TRANSACTION.key,
                Enums.NotificationChannelIds.TYPE_CLAIM_REJECTED.key,
                Enums.NotificationChannelIds.TYPE_CLAIM_DOCUMENT.key, Enums.NotificationChannelIds.TYPE_CLAIM_APPROVED.key,
                Enums.NotificationChannelIds.TYPE_CLAIM_SETTLEMENT_ON_HOLD.key, Enums.NotificationChannelIds.TYPE_CLAIM_SETTLED.key,
                Enums.NotificationChannelIds.CLAIM_CANCELLED_USER.key
                -> {
                    val intent = Gson().fromJson(
                        intent?.getStringExtra(Enums.BundleKeys.pnData.key),
                        PNModels::class.java
                    )

                    val intentProperties = Gson().fromJson(
                        intent.properties,
                        PropertiesObj::class.java
                    )
                    findNavController(R.id.fHomeNav).safeNavigate(
                        R.id.action_homeFragment_to_claim_details_navigation,
                        bundleOf(
                            "fromNoti" to true,
                            "bookingId" to intentProperties?.bookingDetail?.claimId
                        )
                    )
                }
            }
            intent.removeExtra(Enums.BundleKeys.pnNavType.key)
            intent.removeExtra(Enums.BundleKeys.pnData.key)
        } else if (intent?.hasExtra(Constants.SID).getSafe()) {
            redirectToChat()
        }


    }

    private fun redirectToChat() {
        val sid = intent.getStringExtra(Constants.SID)
        intent.removeExtra(Constants.SID)
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(Constants.SID, sid)
        startActivity(intent)
    }

    fun setUserDetails() {
        val profilePicture =
            navigationView.getHeaderView(0).findViewById<CircleImageView>(R.id.ivUserImage)
        val tvAppVersion = navigationView.findViewById<TextView>(R.id.tvAppVersion)
        tvAppVersion.text =
            "${Constants.START}${ApplicationClass.mGlobalData?.globalString?.appVersion.getSafe()} $VERSION_NAME${Constants.END}"
        val name = navigationView.getHeaderView(0).findViewById<TextView>(R.id.tvUserName)
        val user = DataCenter.getUser()

        if (isUserLoggedIn()) {
            name.text = user?.fullName
            navigationView.menu.apply {
                if (isPartnerMode) {
                    findItem(R.id.mMyPartnerProfile).isVisible =
                        user?.applicationStatusId == 2 || user?.applicationStatusId == 4
                }
                if (DataCenter.getUser().isCustomer()) {
                    findItem(R.id.mBecomePartner).isVisible =
                        user?.applicationStatusId == 0 || user?.applicationStatusId == 1 || user?.applicationStatusId == 3
                }

                //show unsub button to only sub users
                findItem(R.id.UnSubscribe).isVisible = DataCenter.getUser()?.isSubscribed == 1

            }
            if (user?.profilePicture != null) {
                profilePicture.loadImage(
                    user.profilePicture,
                    getGenderIcon(user.genderId.toString())
                )
            }
        } else {
            name.text = ApplicationClass.mGlobalData?.tabString?.loginToContinue
            profilePicture.loadImage(
                R.drawable.ic_default
            )
        }
    }

    private fun setDrawerItems() {
        // header
        val userIsLoggedIn = isUserLoggedIn()
        val userIsPartner =
            (DataCenter.getUser().isDoctor() || DataCenter.getUser().isMedicalStaff())
                    && DataCenter.getUser()?.applicationStatusId == com.homemedics.app.utils.Enums.ApplicationStatus.APPROVED.key
        val langData = ApplicationClass.mGlobalData?.tabString
        navigationView.getHeaderView(0).apply {
            findViewById<AppCompatImageView>(R.id.ivLogIn).setImageResource(if (userIsLoggedIn) R.drawable.ic_login_state else R.drawable.ic_login)
            findViewById<TextView>(R.id.tvPartnerMode).apply {
                text = langData?.partnerMode
                setVisible(userIsPartner)
            }
            sbPartnerMode.setVisible(userIsPartner)
            findViewById<View>(R.id.view1).setVisible(userIsPartner)
        }
        val partnerModeOn = (sbPartnerMode.isChecked && sbPartnerMode.isVisible)
        navigationView.menu.apply {
            findItem(R.id.mLanguage).apply {
                title = langData?.language
                //not working
                val enLangItem =
                    metaData?.tenantLanguageItem?.find { it.shortName == DEFAULT_LOCALE_LANGUAGE_EN }
                isVisible = enLangItem != null && metaData?.tenantLanguageItem?.size.getSafe() > 1

            }
            findItem(R.id.mBecomePartner).apply {
                title = langData?.becomePartner
                isVisible = userIsPartner.not()
            }

            findItem(R.id.mMyConnections).apply {
                title = langData?.myConnections
                isVisible = userIsLoggedIn && partnerModeOn.not()
            }
            findItem(R.id.mPersonalProfile).apply {
                title = langData?.myProfile
                isVisible = userIsLoggedIn && partnerModeOn.not()
            }
            findItem(R.id.mAssociation).apply {
                title = langData?.myAssociations
                isVisible = userIsLoggedIn && userIsPartner && partnerModeOn
            }
            findItem(R.id.mMyMedicalRecords).apply {
                isVisible = userIsLoggedIn && partnerModeOn.not()
                title = langData?.myMedicalRecords
            }
            findItem(R.id.mDoctorMessages).apply {
                isVisible = userIsLoggedIn && partnerModeOn.not()
                title = langData?.doctorMessages
            }
            findItem(R.id.mMyOrders).apply {
                title = langData?.myOrdersRequests
                isVisible = userIsLoggedIn && partnerModeOn.not()
            }
            findItem(R.id.mFaq).apply {
                isVisible = true
                title = langData?.faq
            }
            findItem(R.id.mMyPartnerProfile).apply {
                title = langData?.partnerProfile
                isVisible =
                    userIsLoggedIn && userIsPartner && partnerModeOn
            }
            findItem(R.id.mMyAppointments).apply {
                isVisible =
                    userIsLoggedIn && userIsPartner && partnerModeOn
                title = langData?.myAppointment
            }
            findItem(R.id.mMyPlanner).apply {
                title = langData?.myPlanner
                isVisible = userIsLoggedIn && userIsPartner && partnerModeOn
            }
            findItem(R.id.mPatientMessages).apply {
                isVisible =
                    userIsLoggedIn && userIsPartner && partnerModeOn
                title = langData?.patientMessages
            }

            findItem(R.id.mlogout).apply {
                isVisible = userIsLoggedIn
                title = langData?.logout

            }

            findItem(R.id.UnSubscribe).apply {
                isVisible = userIsLoggedIn
            }

            findItem(R.id.Contactus).apply {
                isVisible = true
                title = langData?.contact_us
            }
        }
    }

    private var rating: String? = null

    private var review: String? = null

    private fun showReviewDialog() {
        val dialog: AlertDialog
        val langData = ApplicationClass.mGlobalData
        val reviewDialogBuilder =
            AlertDialog.Builder(this@HomeActivity, R.style.AlertDialogTheme).apply {
                val addReviewBinding = DialogAddReviewBinding.inflate(layoutInflater)
                addReviewBinding.etInstructions.hint =
                    langData?.globalString?.feedbackHere.getSafe()
                addReviewBinding.tvDesc.visible()
                addReviewBinding.tvDesc.text =
                    langData?.dialogsStrings?.callDialogReviewDesc?.replace(
                        "[0]",
                        intent?.getStringExtra("partnerUserName").getSafe()
                    )


                addReviewBinding.etInstructions.addTextChangedListener {
                    val length = it?.length.getSafe()
                    addReviewBinding.tvLength.text =
                        langData?.dialogsStrings?.reviewLength?.replace("[0]", length.toString())
                }

                setView(addReviewBinding.root)
                setCancelable(false)
                setTitle(langData?.dialogsStrings?.writeReview)
                setPositiveButton(langData?.globalString?.save) { _, _ ->
                    val review = addReviewBinding.etInstructions.text.toString()
                    val rating = addReviewBinding.ratingBar.rating.toDouble()
                    if (isValidRating(rating).not()) {
//                        showToast(langData?.fieldValidationStrings?.ratingValidation.getSafe())
                        return@setPositiveButton
                    }
                    this@HomeActivity.review = review
                    this@HomeActivity.rating = rating.toString()
                    val request = MyOrdersRequest(
                        bookingId = bookingId?.toInt(),
                        rating = rating,
                        review
                    )
                    addReview(request)
                    this.create().dismiss()
                }
                setNegativeButton(langData?.globalString?.cancel, null)
            }

        dialog = reviewDialogBuilder.create()

        dialog.show()
    }

    private fun addReview(request: MyOrdersRequest) {
        homeViewModel.addReview(request).observe(this) {
            when (it) {
                is ResponseResult.Success -> {}
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
                else -> {
                    hideLoader()
                }
            }
        }
    }

    private fun getProfileApiCall() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getProfileCall()
        emit(result)
        emit(ResponseResult.Complete)

    }

    private fun getProfile(type: String) {
        if (isOnline(this)) {
            getProfileApiCall().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<UserResponse>
                        response.data?.let { it1 ->
                            tinydb.putObject(
                                com.fatron.network_module.utils.Enums.TinyDBKeys.USER.key,
                                it1
                            )
                        }

                        if (response.data != null && response.data?.messageBookingCount != 0 && TinyDB.instance.getString(
                                com.fatron.network_module.utils.Enums.TinyDBKeys.CHATTOKEN.key
                            )
                                .isNotEmpty() && ApplicationClass.twilioChatManager?.conversationClients == null
                        ) {
                            ApplicationClass.twilioChatManager?.initializeWithAccessToken(
                                applicationContext,
                                TinyDB.instance.getString(
                                    com.fatron.network_module.utils.Enums.TinyDBKeys.CHATTOKEN.key
                                ).getSafe(),
                                TinyDB.instance.getString(
                                    com.fatron.network_module.utils.Enums.TinyDBKeys.FCM_TOKEN.key
                                )
                            )
                        }
                        onResume()
                        if (DataCenter.getUser().isCustomer())
                            findNavController(R.id.fHomeNav).safeNavigate(R.id.action_homeFragment_to_become_partner_navigation)
                        else {
                            findNavController(R.id.fHomeNav).safeNavigate(R.id.action_homeFragment_to_partner_profile_navigation)
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

}