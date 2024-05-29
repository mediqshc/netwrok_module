package com.homemedics.app.ui.activity

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.NotificationManager
import android.app.PictureInPictureParams
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Rect
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.util.Rational
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.fatron.network_module.models.request.video.ParticipantsRequest
import com.fatron.network_module.models.request.video.TokenRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.AppointmentResponse
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.ordersdetails.OrderResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseActivity
import com.homemedics.app.databinding.ActivityCallBinding
import com.homemedics.app.databinding.LayoutCallParticipantBinding
import com.homemedics.app.twilio.call.MyService
import com.homemedics.app.twilio.call.ProximitySensorUtils
import com.homemedics.app.twilio.call.RemoteViewAndParticipant
import com.homemedics.app.twilio.call.TwilioVideoUtils
import com.homemedics.app.ui.bottomsheets.*
import com.homemedics.app.utils.*
import com.homemedics.app.utils.FileUtils
import com.homemedics.app.viewmodel.CallViewModel
import com.homemedics.app.viewmodel.EMRViewModel
import com.twilio.video.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


class CallActivity : BaseActivity() {
    companion object {
        const val actionCallEnded = "com.homemedics.app.call_ended"
        const val actionCallRejected = "com.homemedics.app.call_rejected"

        val callPermissionArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
            )
        }
    }

    private var ringingTimer: CountDownTimer = object : CountDownTimer(
        DataCenter.getMeta()?.callRingingTime.getSafe().toLong() * 1000, 1000
    ) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            ringingTimeRanOut = true
            endCall()
        }
    }

    private var reconnectTimer: CountDownTimer = object : CountDownTimer(
        DataCenter.getMeta()?.callRingingTime.getSafe().toLong() * 1000, 1000
    ) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            endCall()
        }
    }

    private fun startMultiCallRingingTimer(onFinish: () -> Unit) {
        lifecycleScope.launch {
            delay(DataCenter.getMeta()?.callRingingTime.getSafe().toLong() * 1000)
            onFinish.invoke()
        }
    }

    private fun startMultiCallP2RingingTimer(onFinish: () -> Unit) {
        val timer = object : CountDownTimer(
            DataCenter.getMeta()?.callRingingTime.getSafe().toLong() * 1000, 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                onFinish.invoke()
            }
        }

        timer.start()
    }

    private lateinit var mBinding: ActivityCallBinding
    private val callViewModel: CallViewModel by viewModels()
    private val emrViewModel: EMRViewModel by viewModels()
    private val langData = ApplicationClass.mGlobalData

    private var roomName: String = ""
    private var accessToken: String = ""
    private var taskDetails: AppointmentResponse? = null

    private var isVideoEnabled = false
    private var isAudioEnabled = true
    private var isAudioCall = true

    private var isMultiParticipantCall = true

    private var callAccepted = false

    private var fromPush = false
    private var disconnectedManually = false
    private var appointmentNo = ""
    private var partnerUserName = ""
    private var partnerProfilePic = ""
    val fileUtils = FileUtils()
    private val callEndedSet: HashSet<Int> = HashSet()
    private var singleUserCallRejectedToastShow = true
    private var ringingTimeRanOut = false

    private var isBluetoothConnected = false
    private var isSpeakerOn = true
    private var defaultAudioMode = -1

    private val audioManager: AudioManager by lazy {
        getSystemService(AUDIO_SERVICE) as AudioManager
    }

    private val powerManager: PowerManager by lazy {
        getSystemService(POWER_SERVICE) as PowerManager
    }

    private val wakeLock: PowerManager.WakeLock by lazy {
        powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "medIQ:proximity_lock"
        )
    }

    private val twilioVideoUtils by lazy {
        TwilioVideoUtils(this)
    }

    private val proximitySensorUtils by lazy {
        ProximitySensorUtils(this)
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?, persistentState: PersistableBundle?
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
        try {
            fetchAndSetLanguage()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setProximitySensor(true)

        val filter = IntentFilter()
        filter.apply {
            addAction(actionCallEnded)
            addAction(actionCallRejected)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(callEndedReceiver, filter)
        if (isMultiParticipantCall) {
            mBinding.iUserLarge.tvCallStatus.text = ""
        }
        if (isVideoEnabled && callAccepted) {
            try {
                twilioVideoUtils.toggleLocalVideo(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onPause() {
        setProximitySensor(false)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(callEndedReceiver)

        if (isVideoEnabled && callAccepted) {
            try {
                twilioVideoUtils.toggleLocalVideo(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        super.onPause()
    }

    override fun onBackPressed() {
        supportFragmentManager.apply {
            if (backStackEntryCount > 1 /* to always keep callMainFragment */) {
                popBackStack()
            } else {
                //don't go back
            }
//            if(mBinding.flEMRNav.isVisible){
//                showCancelChangesDialog()
//            }

            val navController = findNavController(R.id.fCallEMRNav)

            if (navController.currentDestination?.id == R.id.customerConsultationRecordDetailsFragment) {
                navController.popBackStack(R.id.customerConsultationRecordDetailsFragment, true)
                mBinding.flEMRNav.gone()
            } else if (navController.currentDestination?.id == R.id.medicalRecordsFragment) {
                showCancelChangesDialog()
            }
        }
    }

    fun goBackDuringCall() {
        if (mBinding.flEMRNav.isVisible) {
            removeEMRNavigation()
        }
    }

    private fun showCancelChangesDialog() {
        DialogUtils(this).showDoubleButtonsAlertDialog(
            title = langData?.dialogsStrings?.cancelChanges.getSafe(),
            message = langData?.dialogsStrings?.dataLoseMsg.getSafe(),
            positiveButtonStringText = langData?.globalString?.yes.getSafe(),
            negativeButtonStringText = langData?.globalString?.no.getSafe(),
            buttonCallback = {
                if (mBinding.flEMRNav.isVisible) {
                    removeEMRNavigation()
                }

                emrViewModel.emrID = 0
                emrViewModel.bookingId = 0
                emrViewModel.isDraft = false
            },
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            unregisterReceiver(headsetStateReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        twilioVideoUtils.removeLocalVideo()
        endCall()

        stopReconnectTimer()

        if (fromPush.not()) stopRingingTimer()

        audioManager.mode = defaultAudioMode
    }

    override fun getActivityLayout(): Int = com.homemedics.app.R.layout.activity_call

    override fun getViewBinding() {
        mBinding = binding as ActivityCallBinding
        init()
        ApplicationClass.localeManager.updateLocaleData(
            this,
            TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        )

    }

    override fun setClickListeners() {
        mBinding.apply {
            iActionbar.apply {
                ivSwitchCamera.setOnClickListener(this@CallActivity)
                ivVolume.setOnClickListener(this@CallActivity)
            }
            iCallActionButtons.apply {
                ivOptions.setOnClickListener(this@CallActivity)
                ivEMR.setOnClickListener(this@CallActivity)
                ivToggleVideo.setOnClickListener(this@CallActivity)
                ivToggleAudio.setOnClickListener(this@CallActivity)
                ivDisconnect.setOnClickListener(this@CallActivity)
            }
            iIncomingCallActionButtons.apply {
                ivAccept.setOnClickListener(this@CallActivity)
                ivReject.setOnClickListener(this@CallActivity)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivOptions -> {
                showDetailsBottomSheet()
                // to updates medical record on 3 dots menu
                val request = TokenRequest(bookingId = callViewModel.orderId)
                getOrderDetails(request)
            }
            R.id.ivSwitchCamera -> {
                twilioVideoUtils.switchCamera()
            }
            R.id.ivVolume -> {
                toggleSpeaker(isSpeakerOn.not())
            }
            R.id.ivToggleVideo -> {
                toggleLocalVideo()
            }
            R.id.ivToggleAudio -> {
                toggleLocalMic()
            }
            R.id.ivDisconnect -> {
                disconnectedManually = true
                endCall()
                supportFragmentManager.apply {
                    if (backStackEntryCount > 1 /* to always keep callMainFragment */) {
                        popBackStack()
                    } else {
                        //don't go back
                    }
                }
            }
            R.id.ivAccept -> {
                acceptCall()
            }
            R.id.ivReject -> {
                endCall(isRejected = true)
            }
            R.id.ivEMR -> {
                emrViewModel.bookingId = taskDetails?.bookingId?.toInt().getSafe()
                emrViewModel.customerId = taskDetails?.bookedForUser?.id.getSafe().toString()
                emrViewModel.isDraft = true
                emrViewModel.partnerServiceId =
                    callViewModel.orderDetailsResponse2?.bookingDetails?.partnerServiceId.toString()
                setEMRNavigation()
            }
        }
    }

    private fun init() {
        mBinding.apply {
            iUserLarge.tvCallStatus.text = langData?.globalString?.callStatus.getSafe()
            iIncomingCallActionButtons.tvAccept.text = langData?.globalString?.accept.getSafe()
            iIncomingCallActionButtons.tvDecline.text = langData?.globalString?.decline.getSafe()
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        statusBarColor(R.color.black90, true)
        turnScreenOnAndKeyguardOff()

        defaultAudioMode = audioManager.mode

        fileUtils.init(this)

        intent?.let {
            fromPush = intent.hasExtra(Enums.BundleKeys.fromPush.key)
            callViewModel.fromPush = fromPush

            if (fromPush) {
                callViewModel.orderId = it.getIntExtra(Enums.BundleKeys.id.key, 0)

                val orderJson = it.getStringExtra(Enums.BundleKeys.order.key)
                val order = Gson().fromJson(orderJson, OrderResponse::class.java)

                isMultiParticipantCall = order.bookedForUser?.id != order.customerUser?.id

                callViewModel.orderDetailsResponse = order
            } else {
                startRingingTimer()
                taskDetails = it.getSerializableExtra("task_details") as AppointmentResponse?
                callViewModel.bookingId = it.getStringExtra("booking_id").getSafe()
                callViewModel.appointmentResponse = taskDetails
                callViewModel.appointmentAttachments =
                    it.getSerializableExtra("attachments") as ArrayList<Attachment>?

                callViewModel.orderId = if (callViewModel.bookingId.isNotEmpty()) callViewModel.bookingId.toInt() else 0

                isMultiParticipantCall =
                    taskDetails?.bookedForUser?.id != taskDetails?.customerUser?.id
            }

            setDataInViews()
        }

        treatViewsCorners()

        requestPermissions()

        val request = TokenRequest(bookingId = callViewModel.orderId)
        getOrderDetails(request)


        try {
            val filter = IntentFilter()
            filter.apply {
                addAction(Intent.ACTION_HEADSET_PLUG)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
            registerReceiver(headsetStateReceiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startRingingTimer() {
        try {
            ringingTimer.start()
            startRingingTone()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRingingTimer() {
        try {
            ringingTimer.cancel()
            stopRingingTone()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private lateinit var mediaPlayer: MediaPlayer
    private fun startRingingTone() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.call_ringtone)
            mediaPlayer.isLooping = true
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRingingTone() {
        try {
            mediaPlayer.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startReconnectTimer() {
        try {
            reconnectTimer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopReconnectTimer() {
        try {
            reconnectTimer.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playStopHeadUpNoti(isPlay: Boolean) {
        if (isPlay) {
            startService(Intent(this@CallActivity, MyService::class.java))
        } else {
            stopService(Intent(this@CallActivity, MyService::class.java))
        }
    }

    private fun showDetailsBottomSheet() {
        val bottomSheet = CallDetailsBottomSheet()
        bottomSheet.show(supportFragmentManager, bottomSheet.TAG)
    }

    fun showDoctorMedicalRecords() {
        val doctorRecords = DoctorRecordsBottomSheet()
        doctorRecords.show(supportFragmentManager, doctorRecords.TAG)
    }

    fun showPatientRecords() {
        val patientRecords = PatientMedicalRecordsBottomSheet()
        patientRecords.show(supportFragmentManager, patientRecords.TAG)
    }

    fun showDoctorAttachment() {
        val doctorAttachmentBottomSheet = DoctorAttachmentBottomSheet()
        doctorAttachmentBottomSheet.show(supportFragmentManager, doctorAttachmentBottomSheet.TAG)
    }

    fun showPatientAttachment() {
        val patientAttachmentBottomSheet = PatientAttachmentBottomSheet()
        patientAttachmentBottomSheet.show(supportFragmentManager, patientAttachmentBottomSheet.TAG)
    }

    fun showDoctorDetails() {
        val doctorDetails = DoctorDetailsBottomSheet()
        doctorDetails.show(supportFragmentManager, doctorDetails.TAG)
    }

    fun showAppointmentDetail() {
        val appointmentBottomSheet = AppointmentDetailsBottomSheet()
        appointmentBottomSheet.show(supportFragmentManager, appointmentBottomSheet.TAG)
    }

    private fun treatViewsCorners() {
        val roundedRadius = resources.getDimensionPixelSize(R.dimen.dp10)
        mBinding.apply {
            iSelfView.root.background = MaterialShapeDrawable(
                ShapeAppearanceModel.Builder().setAllCornerSizes(roundedRadius.toFloat())
                    .setAllCorners(RoundedCornerTreatment()).build()
            ).apply {
                fillColor =
                    ColorStateList.valueOf(resources.getColor(R.color.call_light_grey, theme))
            }

            val roundedRadiusLg = resources.getDimensionPixelSize(R.dimen.dp20)
            iCallSingle.root.background = MaterialShapeDrawable(
                ShapeAppearanceModel.Builder().setAllCornerSizes(roundedRadiusLg.toFloat())
                    .setAllCorners(RoundedCornerTreatment()).build()
            ).apply {
                fillColor = ColorStateList.valueOf(resources.getColor(R.color.call_grey, theme))
            }

            iCallMulti.iMultiRemoteView1.root.background = MaterialShapeDrawable(
                ShapeAppearanceModel.Builder().setAllCornerSizes(roundedRadiusLg.toFloat())
                    .setAllCorners(RoundedCornerTreatment()).build()
            ).apply {
                fillColor = ColorStateList.valueOf(resources.getColor(R.color.call_grey, theme))
            }

            iCallMulti.iMultiRemoteView2.root.background = MaterialShapeDrawable(
                ShapeAppearanceModel.Builder().setAllCornerSizes(roundedRadiusLg.toFloat())
                    .setAllCorners(RoundedCornerTreatment()).build()
            ).apply {
                fillColor = ColorStateList.valueOf(resources.getColor(R.color.call_grey, theme))
            }
        }
    }

    private fun handleIntent() {
        setInitialAudioSource()

        if (fileUtils.hasPermissions(baseContext, callPermissionArray)) {
            twilioVideoUtils.initStream(isAudioEnabled, isVideoEnabled)
        }

        intent?.let {
            accessToken = it.getStringExtra(Enums.BundleKeys.token.key).toString()
            roomName = it.getStringExtra(Enums.BundleKeys.roomName.key).toString()
        }

        if (fromPush.not()) {
            twilioVideoUtils.connectToRoom(
                roomName, accessToken, roomListener
            )
        }

        val action = intent?.getIntExtra(Enums.BundleKeys.action.key, 0)
        when (action) {
            Enums.CallPNType.ACCEPT.key -> {
                acceptCall()
            }
            Enums.CallPNType.REJECT.key -> {
                endCall(isRejected = true)
            }
        }
    }

    private fun isHeadsetOn(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return audioManager.isWiredHeadsetOn || audioManager.isBluetoothScoOn || audioManager.isBluetoothA2dpOn
        } else {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                if (device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {

                    isBluetoothConnected = device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO

                    return true
                }
            }
        }
        return false
    }

    private fun setInitialAudioSource() {
        Timber.e("handsfree: ${isHeadsetOn()}")
        Timber.e("bluetooth: $isBluetoothConnected")

        if (isHeadsetOn().not()) {
//            audioManager.mode = AudioManager.MODE_IN_CALL
//            audioManager.isSpeakerphoneOn = isSpeakerOn
            toggleSpeaker(true)
        } else {
            toggleSpeaker(false)
        }
    }

    private fun toggleSpeaker(onLoudSpeaker: Boolean) {
        if (onLoudSpeaker) {
            audioManager.mode = if (isMultiParticipantCall) AudioManager.MODE_IN_COMMUNICATION
            else AudioManager.MODE_IN_CALL
//                audioManager.mode =  AudioManager.MODE_IN_CALL
            audioManager.isSpeakerphoneOn = true
            mBinding.iActionbar.ivVolume.setImageResource(R.drawable.ic_volume_on_white)
            isSpeakerOn = true
        } else {
            audioManager.mode = if (isMultiParticipantCall) AudioManager.MODE_IN_COMMUNICATION
            else AudioManager.MODE_NORMAL
//                audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
            if (isBluetoothConnected) {
                audioManager.mode = AudioManager.STREAM_VOICE_CALL
                audioManager.isBluetoothScoOn = true
            }

            mBinding.iActionbar.ivVolume.setImageResource(R.drawable.ic_call_white)
            isSpeakerOn = false
        }
    }

    private fun toggleLocalMic() {
        isAudioEnabled = isAudioEnabled.not()

        mBinding.apply {
            twilioVideoUtils.toggleMic()
            if (isAudioEnabled) {
                iCallActionButtons.ivToggleAudio.setImageResource(R.drawable.ic_mic_call)
                iSelfView.ivAudioStatus.setVisible(false)
            } else {
                iCallActionButtons.ivToggleAudio.setImageResource(R.drawable.ic_mic_off_call)
                iSelfView.ivAudioStatus.setVisible(true)
            }
        }
    }

    private fun toggleLocalVideo() {
        isVideoEnabled = isVideoEnabled.not()
        twilioVideoUtils.toggleLocalVideo(isVideoEnabled)

        if (isAudioCall) {
            twilioVideoUtils.publishLocalVideoTrack()
        }

        setLocalVideoViews(isVideoEnabled)
    }

    private fun setLocalVideoViews(videoEnabled: Boolean) {
        mBinding.apply {
            iSelfView.ivThumbnail.setVisible(videoEnabled.not())
            iActionbar.ivSwitchCamera.setVisible(videoEnabled)

            iCallActionButtons.ivToggleVideo.setImageResource(
                if (isVideoEnabled) R.drawable.ic_camera_call
                else R.drawable.ic_video_off_call
            )

            if (videoEnabled) //to handle grey bg behind local video view
                iSelfView.localVideoView.visible()
            else iSelfView.localVideoView.invisible()

            if (isAudioCall) {
                isAudioCall = false
                iSelfView.root.setVisible(true)
            }
        }
    }

    private fun setRemoteVideoViews(videoEnabled: Boolean, participant: RemoteParticipant) {
        mBinding.apply {
            if (isMultiParticipantCall) {
                twilioVideoUtils.remoteVideoViewList.find { it.identity == participant.identity }
                    ?.apply {
                        videoView.setVisible(videoEnabled)
                        parentViewBinding.ivThumbnail.setVisible(videoEnabled.not())
                    }
            } else {
                iCallSingle.remoteVideoView.setVisible(videoEnabled, true)
                iCallSingle.ivThumbnail.setVisible(videoEnabled.not())
            }
        }
    }

    private fun setRemoteAudioViews(audioEnabled: Boolean, participant: RemoteParticipant) {
        mBinding.apply {
            if (isMultiParticipantCall) {
                twilioVideoUtils.remoteVideoViewList.find { it.identity == participant.identity }?.parentViewBinding?.ivAudioStatus?.setVisible(
                    audioEnabled.not()
                )
            } else {
                iCallSingle.ivAudioStatus.setVisible(audioEnabled.not())
            }
        }
    }

    fun setFragment(fragment: Fragment, args: Bundle? = null) {
        val fm = supportFragmentManager

        if (args != null) fragment.arguments = args

        mBinding.flMainContainer.visible()

        fm.beginTransaction().addToBackStack(fragment::class.simpleName)
            .replace(R.id.flMainContainer, fragment, fragment::class.simpleName).commit()
    }

    private fun setEMRNavigation() {
        val myNavHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.fCallEMRNav) as NavHostFragment
        val inflater = myNavHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.medical_records_navigation)

        myNavHostFragment.navController.graph = graph
        mBinding.flEMRNav.visible()
    }

    fun removeEMRNavigation() {
        mBinding.flEMRNav.gone()

        // to updates medical record on 3 dots menu
        val request = TokenRequest(bookingId = callViewModel.orderId)
        getOrderDetails(request)
    }

    fun removeFragment() {
        mBinding.flMainContainer.gone()

        supportFragmentManager.fragments.forEach { _ ->
            supportFragmentManager.popBackStack()
        }
    }

    fun setRecordNavigation() {
        val myNavHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.fCallEMRNav) as NavHostFragment
        val inflater = myNavHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.customer_emr_consultation_navigation).apply {
            startDestination = R.id.customerConsultationRecordDetailsFragment
        }

        myNavHostFragment.navController.graph = graph
        mBinding.flEMRNav.visible()
    }

    private fun setProximitySensor(enable: Boolean) {
//        if (enable) {
//            proximitySensorUtils.setListener {
//                if (isAudioCall && twilioVideoUtils.isConnected()) {
//                    if (it == ProximitySensorUtils.STATE_NEAR) {
//                        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
//                    } else {
//                        if (wakeLock.isHeld) wakeLock.release()
//                    }
//                } else {
//                    if (wakeLock.isHeld) wakeLock.release()
//                }
//            }
//        } else {
//            proximitySensorUtils.setListener(null)
//        }
    }

    private fun requestPermissions() {
        permissionsResultLauncher.launch(callPermissionArray)
    }

    private val permissionsResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.containsValue(false)) {
//                requestPermissions()
                showAllowPermissionsDialog()
            } else {
                twilioVideoUtils.createAudioAndVideoTracks(
                    mBinding.iSelfView.localVideoView, mBinding.iCallSingle.remoteVideoView
                )

                handleIntent()
            }
        }

    private fun showAllowPermissionsDialog() {
        DialogUtils(this).showDoubleButtonsAlertDialog(title = langData?.globalString?.warning.getSafe(),
            message = langData?.globalString?.cameraPermissions.getSafe(),
            negativeButtonStringText = langData?.globalString?.cancel.getSafe(),
            positiveButtonStringText = langData?.dialogsStrings?.permitManual.getSafe(),
            buttonCallback = {
                gotoAppSettings(baseContext)
            },
            negativeButtonCallback = {
                endCall()
            })
    }


    private fun acceptCall() {
        if (fileUtils.hasPermissions(baseContext, callPermissionArray).not()) {
            requestPermissions()
            return
        }

        intent?.let {
            accessToken = it.getStringExtra(Enums.BundleKeys.token.key).toString()
            roomName = it.getStringExtra(Enums.BundleKeys.roomName.key).toString()
        }

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(
            MyFirebaseMessagingService.callNotificationId
        )

        MyFirebaseMessagingService.noActionPerformed = false
        callAccepted = true
        LocalBroadcastManager.getInstance(baseContext)
            .sendBroadcast(Intent(MyFirebaseMessagingService.actionCallAccepted))
        twilioVideoUtils.initStream(isAudioEnabled, isVideoEnabled)
        twilioVideoUtils.setLocalParticipantListener(localParticipantListener)
        setCallAcceptedViews()
        Timber.e("roomName: $roomName")
        twilioVideoUtils.connectToRoom(roomName, accessToken, roomListener)
        playStopHeadUpNoti(true)

    }

    private fun endCall(isRejected: Boolean = false) {

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(
            MyFirebaseMessagingService.callNotificationId
        )

        LocalBroadcastManager.getInstance(baseContext)
            .sendBroadcast(Intent(MyFirebaseMessagingService.actionCallRejected))

        MyFirebaseMessagingService.noActionPerformed = false
        disconnect()
        destroyRoomApi(isRejected)
        playStopHeadUpNoti(false)
    }

    private fun disconnect() {
        try {
            if (twilioVideoUtils.connectionState == Room.State.CONNECTED) twilioVideoUtils.disconnect()

            twilioVideoUtils.removeLocalVideo()
            MyFirebaseMessagingService.callAccepted = false
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            navigateBack()
        }
    }

    private fun destroyRoomApi(isRejected: Boolean = false) {
        /**
         * single user call and doctor
         * singleUserCallRejectedToastShow -> destroyRoomApi calling two times -> onDisconnected, on EndCallPN
         */
        if (isMultiParticipantCall.not() && fromPush.not() && singleUserCallRejectedToastShow && disconnectedManually.not() && twilioVideoUtils.remoteVideoViewList.firstOrNull()?.remoteParticipant == null && ringingTimeRanOut.not()) {
            showToast(langData?.callScreen?.callRejectedSingle.getSafe())
            singleUserCallRejectedToastShow = false
        }

        val dropForcefully = (isMultiParticipantCall && fromPush.not() && callEndedSet.size == 2)

        Timber.e("dropForcefully: $dropForcefully")

        val request = ParticipantsRequest(
            orderId = callViewModel.orderId,
            dropCall = dropForcefully.getInt(),
            isRejected = isRejected.getInt(),
        )
        callViewModel.destroyRoom(request).observe(this) {
            Timber.e("destroyRoom")
        }
    }

    private fun navigateBack() {
        if (fromPush) {
            val intent = Intent(this@CallActivity, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (callAccepted && callViewModel.orderDetailsResponse?.customerUser?.id == DataCenter.getUser()?.id && callViewModel.orderDetailsResponse?.review == null) {
                intent.putExtra(
                    Enums.BundleKeys.id.key, callViewModel.orderDetailsResponse?.customerUser?.id
                )
                intent.putExtra(
                    Enums.BundleKeys.partnerUserName.key,
                    callViewModel.orderDetailsResponse?.partnerUser?.fullName
                )
                intent.putExtra(
                    Enums.BundleKeys.bookingId.key,
                    callViewModel.orderDetailsResponse?.id.toString()
                )
            }

            finishAffinity()
            startActivity(intent)
        } else finish()
    }

    ///////////////////// listeners //////////////////////

    //listener
    private val roomListener = object : Room.Listener {

        override fun onConnected(room: Room) {
            Timber.e("onConnected")

            setInitialAudioSource()

            room.remoteParticipants.forEach {
                it.setListener(remoteParticipantListener())
            }

            if (room.remoteParticipants.isNotEmpty()) {
                setCallAcceptedViews()
            }

            twilioVideoUtils.localVideoTrack?.let { localVideoTrack ->
                room.localParticipant?.publishTrack(localVideoTrack)
            }
        }

        override fun onConnectFailure(room: Room, twilioException: TwilioException) {
            Timber.e("onConnectFailure")
            twilioException.printStackTrace()
        }

        override fun onReconnecting(room: Room, twilioException: TwilioException) {
            Timber.e("onReconnecting")
            startReconnectTimer()
            mBinding.tvSlowConnection.apply {
                text = langData?.callScreen?.reconnecting
                visible()
            }
        }

        override fun onReconnected(room: Room) {
            Timber.e("onReconnected")
            stopReconnectTimer()
            mBinding.tvSlowConnection.apply {
                gone()
            }
        }

        override fun onDisconnected(room: Room, twilioException: TwilioException?) {
            Timber.e("onDisconnected")
            twilioException?.printStackTrace()
            endCall()
//            if (twilioVideoUtils.getConnectedParticipants().isEmpty()) {
//                destroyRoomApi()
//            }
        }

        override fun onParticipantConnected(room: Room, remoteParticipant: RemoteParticipant) {
            Timber.e("onParticipantConnected ${remoteParticipant.identity}")
            remoteParticipant.setListener(remoteParticipantListener())
            twilioVideoUtils.addRemoteParticipant(remoteParticipant, remoteParticipantListener())
            setCallAcceptedViews()
        }

        override fun onParticipantDisconnected(
            room: Room, remoteParticipant: RemoteParticipant
        ) {
            Timber.e("onParticipantDisconnected ${remoteParticipant.identity}")

            val doctorWasLeft =
                remoteParticipant.identity == getIdWithVariant(callViewModel.orderDetailsResponse?.partnerUser?.id.getSafe())

            twilioVideoUtils.removeRemoteParticipant(remoteParticipant)

            if (room.remoteParticipants.isNotEmpty() && isMultiParticipantCall) {
                setMultipleParticipantCallUserEndedViews(remoteParticipant.identity)
            }

            if (room.remoteParticipants.isEmpty() || doctorWasLeft) {
                endCall()
            }
        }

        override fun onRecordingStarted(room: Room) {
            Timber.e("onRecordingStarted")
        }

        override fun onRecordingStopped(room: Room) {
            Timber.e("onRecordingStopped")
        }
    }

    private fun remoteParticipantListener(): RemoteParticipant.Listener? {
        return object : RemoteParticipant.Listener {

            override fun onNetworkQualityLevelChanged(
                remoteParticipant: RemoteParticipant, networkQualityLevel: NetworkQualityLevel
            ) {
                Timber.e("remote: onNetworkQualityLevelChanged: ${networkQualityLevel.ordinal}")
                val remoteUser =
                    twilioVideoUtils.remoteVideoViewList.find { it.identity == remoteParticipant.identity }

                if (networkQualityLevel.ordinal <= 2) { //2 is below average
                    remoteUser?.let {
                        it.parentViewBinding.tvCallStatusMessage.text =
                            langData?.callScreen?.slowInternetConnection
                        it.parentViewBinding.tvCallStatusMessage.visible()
                    }
                } else {
                    remoteUser?.let {
                        it.parentViewBinding.tvCallStatusMessage.gone()
                        it.parentViewBinding.tvCallStatusMessage.text =
                            langData?.callScreen?.callStatus
                    }
                }
            }

            override fun onAudioTrackSubscribed(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
                remoteAudioTrack: RemoteAudioTrack
            ) {
                Timber.e("onAudioTrackSubscribed")
                setRemoteAudioViews(remoteAudioTrack.isEnabled, remoteParticipant)
            }

            override fun onAudioTrackPublished(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
            ) {
                Timber.e("onAudioTrackPublished")
                setRemoteAudioViews(true, remoteParticipant)
            }

            override fun onAudioTrackUnpublished(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
            ) {
                Timber.e("onAudioTrackUnpublished")
                setRemoteAudioViews(false, remoteParticipant)
            }

            override fun onAudioTrackSubscriptionFailed(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
                twilioException: TwilioException
            ) {
                Timber.e("onAudioTrackSubscriptionFailed")
            }

            override fun onAudioTrackUnsubscribed(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
                remoteAudioTrack: RemoteAudioTrack
            ) {
                Timber.e("onAudioTrackUnsubscribed")
                setRemoteAudioViews(false, remoteParticipant)
            }

            override fun onVideoTrackSubscribed(
                participant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
                remoteVideoTrack: RemoteVideoTrack
            ) {
                Timber.e("onVideoTrackSubscribed")
//                twilioVideoUtils.addRemoteParticipantVideo(remoteParticipant, remoteVideoTrack)


                if (isMultiParticipantCall) {
                    twilioVideoUtils.remoteVideoViewList.find { it.identity == participant.identity }
                        ?.apply {
                            remoteParticipant = participant
                            userHasJoined = true
                        }
                    twilioVideoUtils.addRemoteParticipant(participant, remoteParticipantListener())

                    //for last participant connected, which don't have state of older participants
                    if (remoteVideoTrack.isEnabled) onVideoTrackEnabled(
                        participant,
                        remoteVideoTrackPublication
                    )
                    else onVideoTrackDisabled(participant, remoteVideoTrackPublication)
                } else {
                    twilioVideoUtils.remoteVideoViewList[0].remoteParticipant = participant
                    twilioVideoUtils.addRemoteParticipant(participant, remoteParticipantListener())


                }

                setCallAcceptedViews()
//                if(twilioVideoUtils.getConnectedParticipants().size == 1) {
//                    setSingleMultiCallViews(false)
//
//                    twilioVideoUtils.remoteVideoViewList.add(
//                        RemoteViewAndParticipant().apply {
//                            identity = participant.identity
//                            remoteParticipant = participant
//                            videoView = mBinding.iRemoteVideoView.remoteVideoView
//                        }
//                    )
//
//                    twilioVideoUtils.addRemoteParticipant(participant, remoteParticipantListener())
//                }
//                else if(twilioVideoUtils.getConnectedParticipants().size == 2) {
//                    setSingleMultiCallViews(true)
//
//                    //part 1
//                    twilioVideoUtils.addRemoteParticipant(
//                        twilioVideoUtils.getConnectedParticipants()[0], remoteParticipantListener())
//
//                    twilioVideoUtils.remoteVideoViewList.add(
//                        RemoteViewAndParticipant().apply {
//                            identity = twilioVideoUtils.getConnectedParticipants()[0].identity,
//                            remoteParticipant = twilioVideoUtils.getConnectedParticipants()[0],
//                            videoView = mBinding.iCallMulti.iMultiRemoteView1.remoteVideoView
//                        }
//                    )
//
//                    //part 2
//                    twilioVideoUtils.addRemoteParticipant(twilioVideoUtils.getConnectedParticipants()[1], remoteParticipantListener())
//
//                    twilioVideoUtils.remoteVideoViewList.add(
//                        RemoteViewAndParticipant().apply {
//                            identity = twilioVideoUtils.getConnectedParticipants()[1].identity,
//                            remoteParticipant = twilioVideoUtils.getConnectedParticipants()[1],
//                            videoView = mBinding.iCallMulti.iMultiRemoteView2.remoteVideoView
//                        }
//                    )
//                }
            }

            override fun onVideoTrackPublished(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
            ) {
                Timber.e("onVideoTrackPublished")
                setRemoteVideoViews(true, remoteParticipant)
            }

            override fun onVideoTrackUnpublished(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
            ) {
                Timber.e("onVideoTrackUnpublished")
                setRemoteVideoViews(false, remoteParticipant)
            }

            override fun onVideoTrackSubscriptionFailed(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
                twilioException: TwilioException
            ) {
                Timber.e("onVideoTrackSubscriptionFailed")
            }

            override fun onVideoTrackUnsubscribed(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
                remoteVideoTrack: RemoteVideoTrack
            ) {
                Timber.e("onVideoTrackUnsubscribed")
                twilioVideoUtils.removeParticipantVideo(remoteParticipant, remoteVideoTrack)
                setRemoteVideoViews(true, remoteParticipant)
            }

            override fun onDataTrackPublished(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication
            ) {
                Timber.e("onDataTrackPublished")
            }

            override fun onDataTrackUnpublished(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication
            ) {
                Timber.e("onDataTrackUnpublished")
            }

            override fun onDataTrackSubscribed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                remoteDataTrack: RemoteDataTrack
            ) {
                Timber.e("onDataTrackSubscribed")
            }

            override fun onDataTrackSubscriptionFailed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                twilioException: TwilioException
            ) {
                Timber.e("onDataTrackSubscriptionFailed")
            }

            override fun onDataTrackUnsubscribed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                remoteDataTrack: RemoteDataTrack
            ) {
                Timber.e("onDataTrackUnsubscribed")
            }

            override fun onAudioTrackEnabled(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
            ) {
                Timber.e("onAudioTrackEnabled")
                setRemoteAudioViews(true, remoteParticipant)
            }

            override fun onAudioTrackDisabled(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
            ) {
                Timber.e("onAudioTrackDisabled")
                setRemoteAudioViews(false, remoteParticipant)
            }

            override fun onVideoTrackEnabled(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
            ) {
                Timber.e("onVideoTrackEnabled")
                setRemoteVideoViews(true, remoteParticipant)

            }

            override fun onVideoTrackDisabled(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
            ) {
                Timber.e("onVideoTrackDisabled")
                setRemoteVideoViews(false, remoteParticipant)
            }
        }
    }

    private val localParticipantListener = object : LocalParticipant.Listener {

        override fun onNetworkQualityLevelChanged(
            localParticipant: LocalParticipant, networkQualityLevel: NetworkQualityLevel
        ) {
            Timber.e("onNetworkQualityLevelChanged")
            mBinding.tvSlowConnection.apply {
                text = langData?.callScreen?.slowInternetConnection.getSafe()
                setVisible(networkQualityLevel.ordinal <= 2) //2 is below average
            }
        }

        override fun onAudioTrackPublished(
            localParticipant: LocalParticipant,
            localAudioTrackPublication: LocalAudioTrackPublication
        ) {
            Timber.e("onAudioTrackPublished")
        }

        override fun onAudioTrackPublicationFailed(
            localParticipant: LocalParticipant,
            localAudioTrack: LocalAudioTrack,
            twilioException: TwilioException
        ) {
            Timber.e("onAudioTrackPublicationFailed " + twilioException.message)
        }

        override fun onVideoTrackPublished(
            localParticipant: LocalParticipant,
            localVideoTrackPublication: LocalVideoTrackPublication
        ) {
            Timber.e("onVideoTrackPublished")
            setLocalVideoViews(true)
        }

        override fun onVideoTrackPublicationFailed(
            localParticipant: LocalParticipant,
            localVideoTrack: LocalVideoTrack,
            twilioException: TwilioException
        ) {
            Timber.e("onVideoTrackPublicationFailed " + twilioException.message)
        }

        override fun onDataTrackPublished(
            localParticipant: LocalParticipant, localDataTrackPublication: LocalDataTrackPublication
        ) {
            Timber.e("onDataTrackPublished")
        }

        override fun onDataTrackPublicationFailed(
            localParticipant: LocalParticipant,
            localDataTrack: LocalDataTrack,
            twilioException: TwilioException
        ) {
            Timber.e("onDataTrackPublicationFailed " + twilioException.message)
        }
    }

    private fun addRemoteViewAndParticipant(
        id: Int, remoteVideoView: VideoView, viewBinding: LayoutCallParticipantBinding
    ) {
        twilioVideoUtils.remoteVideoViewList.add(RemoteViewAndParticipant().apply {
            identity = getIdWithVariant(id)
            videoView = remoteVideoView
            parentViewBinding = viewBinding
        })
    }

    private fun getIdWithVariant(id: Int): String {
        return "${com.homemedics.app.BuildConfig.FLAVOR}-$id" //dev-1
    }

    private fun getIdWithoutVariant(identity: String): Int {
        return try {
            identity.substringAfter("-").toInt()
        } catch (e: Exception) {
            0
        }
    }

    ///////////////// call state view //////////////

    private fun setDataInViews() {
        if (fromPush) { //receiving end
            callViewModel.orderDetailsResponse?.let { order ->
                if (isMultiParticipantCall) {
                    mBinding.iCallMulti.apply {
                        //doctor participant
                        iMultiRemoteView1.apply {
                            remoteVideoView.tag = order.partnerUser?.id
                            addRemoteViewAndParticipant(
                                order.partnerUser?.id.getSafe(), remoteVideoView, iMultiRemoteView1
                            )

                            tvName.text = order.partnerUser?.fullName
                            tvNameSm.text = order.partnerUser?.fullName
                            ivThumbnail.loadImage(
                                order.partnerUser?.userProfilePicture?.file,
                                getGenderIcon(order.partnerUser?.genderId.toString())
                            )


                        }

                        //family participant
                        //main user
                        iMultiRemoteView2.apply {
                            if (DataCenter.getUser()?.id == order.customerUser?.id) {
                                remoteVideoView.tag = order.bookedForUser?.id
                                addRemoteViewAndParticipant(
                                    order.bookedForUser?.id.getSafe(),
                                    remoteVideoView,
                                    iMultiRemoteView2
                                )

                                tvName.text = order.bookedForUser?.fullName
                                tvNameSm.text = order.bookedForUser?.fullName
                                tvCallStatusMessage.text =
                                    langData?.callScreen?.callStatus?.replace(
                                        "[0]", order.bookedForUser?.fullName.getSafe()
                                    )

                                ivThumbnail.loadImage(
                                    order.bookedForUser?.userProfilePicture?.file,
                                    getGenderIcon(order.bookedForUser?.genderId.toString())
                                )
                            } else { //family connection
                                remoteVideoView.tag = order.customerUser?.id
                                addRemoteViewAndParticipant(
                                    order.customerUser?.id.getSafe(),
                                    remoteVideoView,
                                    iMultiRemoteView2
                                )

                                tvName.text = order.customerUser?.fullName
                                tvNameSm.text = order.customerUser?.fullName
                                tvCallStatusMessage.text =
                                    langData?.callScreen?.callStatus?.replace(
                                        "[0]", order.customerUser?.fullName.getSafe()
                                    )

                                ivThumbnail.loadImage(
                                    order.customerUser?.userProfilePicture?.file,
                                    getGenderIcon(order.customerUser?.genderId.toString())
                                )
                            }
                        }
                    }
                } else { //single receiving user call
                    mBinding.apply {
                        appointmentNo =
                            intent?.getStringExtra(Enums.BundleKeys.appointmentNo.key).getSafe()
                        partnerUserName =
                            intent?.getStringExtra(Enums.BundleKeys.partnerUserName.key).getSafe()
                        partnerProfilePic =
                            intent?.getStringExtra(Enums.BundleKeys.partnerProfilePic.key).getSafe()

                        addRemoteViewAndParticipant(
                            order.partnerUser?.id.getSafe(),
                            iCallSingle.remoteVideoView,
                            iCallSingle
                        )


                        callViewModel.orderDetailsResponse.apply {
                            iCallSingle.tvName.text = order.partnerUser?.fullName
                            iCallSingle.tvNameSm.text = order.partnerUser?.fullName
                            iCallSingle.ivThumbnail.loadImage(
                                order.partnerUser?.userProfilePicture?.file,
                                getGenderIcon(order.partnerUser?.genderId.toString())
                            )
                        }
                    }
                }

                mBinding.iActionbar.apply {
                    tvAppointmentNo.text =
                        "\u2066# ${order.uniqueIdentificationNumber.getSafe()} \u2069"

                }

                mBinding.iUserLarge.apply {
                    tvName.text = order.partnerUser?.fullName
                    ivThumbnail.loadImage(
                        order.partnerUser?.userProfilePicture?.file,
                        getGenderIcon(order.partnerUser?.genderId.toString())
                    )
                }
            }

            incomingCallViews()
        } else { //doctor end
            callViewModel.appointmentResponse?.let { appointment ->
                if (isMultiParticipantCall) {
                    mBinding.iCallMulti.apply {
                        //main user
                        iMultiRemoteView1.apply {
                            remoteVideoView.tag = appointment.customerUser?.id
                            addRemoteViewAndParticipant(
                                appointment.customerUser?.id.getSafe(),
                                remoteVideoView,
                                iMultiRemoteView1
                            )

                            tvName.text = appointment.customerUser?.fullName
                            tvNameSm.text = appointment.customerUser?.fullName
                            tvCallStatusMessage.text = langData?.callScreen?.callStatus?.replace(
                                "[0]", appointment.customerUser?.fullName.getSafe()
                            )

                            ivThumbnail.loadImage(
                                appointment.customerUser?.userProfilePicture?.file,
                                getGenderIcon(appointment.customerUser?.genderId.toString())
                            )

                            startMultiCallRingingTimer {
                                val identity =
                                    getIdWithVariant(appointment.customerUser?.id.getSafe())
                                val participantConnected =
                                    twilioVideoUtils.getConnectedParticipants()
                                        .find { it.identity == identity }

                                if (participantConnected == null) {
//                                    tvNameSm.gone()
                                    tvCallStatus.gone()
                                    tvName.gone()
                                    bCallAgain.visible()
                                    tvCallStatusMessage.visible()
                                    ripple.stopRippleAnimation()
                                }
                            }

                            bCallAgain.setOnClickListener {
                                addParticipantApiCall(appointment.customerUser?.id.getSafe()) {
                                    bCallAgain.gone()
                                    tvCallStatusMessage.gone()
                                    tvNameSm.gone()
                                    tvName.visible()
                                    ripple.startRippleAnimation()
                                    tvCallStatus.visible()

                                    startMultiCallRingingTimer {
                                        val identity =
                                            getIdWithVariant(appointment.customerUser?.id.getSafe())
                                        val participantConnected =
                                            twilioVideoUtils.getConnectedParticipants()
                                                .find { it.identity == identity }

                                        if (participantConnected == null) {
//                                            tvNameSm.gone()
                                            tvCallStatus.gone()
                                            tvName.gone()
                                            bCallAgain.visible()
                                            tvCallStatusMessage.visible()
                                            ripple.stopRippleAnimation()
                                        }
                                    }
                                }
                            }
                        }

                        //family connection
                        iMultiRemoteView2.apply {
                            remoteVideoView.tag = appointment.bookedForUser?.id
                            addRemoteViewAndParticipant(
                                appointment.bookedForUser?.id.getSafe(),
                                remoteVideoView,
                                iMultiRemoteView2
                            )

                            tvName.text = appointment.bookedForUser?.fullName
                            tvNameSm.text = appointment.bookedForUser?.fullName
                            tvCallStatusMessage.text = langData?.callScreen?.callStatus?.replace(
                                "[0]", appointment.bookedForUser?.fullName.getSafe()
                            )

                            ivThumbnail.loadImage(
                                appointment.bookedForUser?.userProfilePicture?.file,
                                getGenderIcon(appointment.bookedForUser?.genderId.toString())
                            )

                            startMultiCallRingingTimer {
                                val identity =
                                    getIdWithVariant(appointment.bookedForUser?.id.getSafe())
                                val participantConnected =
                                    twilioVideoUtils.getConnectedParticipants()
                                        .find { it.identity == identity }

                                if (participantConnected == null) {
//                                    tvNameSm.gone()
                                    tvCallStatus.gone()
                                    tvName.gone()
                                    bCallAgain.visible()
                                    tvCallStatusMessage.visible()
                                    ripple.stopRippleAnimation()
                                }
                            }

                            bCallAgain.setOnClickListener {
                                addParticipantApiCall(appointment.bookedForUser?.id.getSafe()) {
                                    bCallAgain.gone()
                                    tvCallStatusMessage.gone()
                                    tvNameSm.gone()
                                    tvName.visible()
                                    ripple.startRippleAnimation()
                                    tvCallStatus.visible()

                                    startMultiCallRingingTimer {
                                        val identity =
                                            getIdWithVariant(appointment.bookedForUser?.id.getSafe())
                                        val participantConnected =
                                            twilioVideoUtils.getConnectedParticipants()
                                                .find { it.identity == identity }

                                        if (participantConnected == null) {
//                                            tvNameSm.gone()
                                            tvCallStatus.gone()
                                            tvName.gone()
                                            bCallAgain.visible()
                                            tvCallStatusMessage.visible()
                                            ripple.stopRippleAnimation()
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {//single calling user call
                    mBinding.apply {
                        callViewModel.appointmentResponse.apply {
                            iUserLarge.apply {
                                tvCallStatus.text = langData?.callScreen?.ringing
                                tvCallStatus.setTextColor(
                                    ContextCompat.getColor(
                                        baseContext, R.color.orange
                                    )
                                )
                            }

                            addRemoteViewAndParticipant(
                                appointment.customerUser?.id.getSafe(),
                                iCallSingle.remoteVideoView,
                                iCallSingle
                            )

                            iUserLarge.tvName.text = appointment.customerUser?.fullName.getSafe()
                            iUserLarge.ivThumbnail.loadImage(
                                appointment.customerUser?.userProfilePicture?.file,
                                getGenderIcon(appointment.customerUser?.genderId.toString())
                            )

                            iCallSingle.tvName.text = appointment.customerUser?.fullName.getSafe()
                            iCallSingle.tvNameSm.text = appointment.customerUser?.fullName.getSafe()
                            iCallSingle.ivThumbnail.loadImage(
                                appointment.customerUser?.userProfilePicture?.file,
                                getGenderIcon(appointment.customerUser?.genderId.toString())
                            )
                        }

                        iActionbar.apply {
                            tvAppointmentNo.text =
                                "\u2066# ${callViewModel.appointmentResponse?.uniqueIdentificationNumber.getSafe()} \u2069"

                            callViewModel.appointmentResponse?.uniqueIdentificationNumber?.let {
                                tinydb.putString(
                                    "unique_id", it
                                )
                            }
                        }
                    }
                }

                mBinding.iActionbar.apply {
                    tvAppointmentNo.text =
                        "\u2066# ${appointment.uniqueIdentificationNumber.getSafe()} \u2069"

                    appointment.uniqueIdentificationNumber?.let {
                        tinydb.putString(
                            "unique_id", it
                        )
                    }
                }
            }

            setOutgoingCallViews()
        }

        mBinding.iSelfView.ivThumbnail.loadImage(
            DataCenter.getUser()?.profilePicture,
            getGenderIcon(DataCenter.getUser()?.genderId.toString())
        )
    }

    private fun incomingCallViews() {
        mBinding.apply {
            hideAllViews()
            iUserLarge.root.visible()
            rippleViewAnimation()
            iIncomingCallActionButtons.root.visible()
        }
    }

    private fun setOutgoingCallViews() {
        mBinding.apply {
            hideAllViews()

            if (isMultiParticipantCall) {
                iCallMulti.root.visible()
                iCallMulti.iMultiRemoteView1.tvName.visible()
                iCallMulti.iMultiRemoteView2.tvName.visible()

                iCallMulti.iMultiRemoteView1.ripple.startRippleAnimation()
                iCallMulti.iMultiRemoteView2.ripple.startRippleAnimation()

            } else {
                iUserLarge.root.visible()
                rippleViewAnimation()
            }

            iActionbar.apply {
                root.visible()
                ivVolume.gone()
            }
            iCallActionButtons.apply {
                root.visible()
                ivOptions.gone()
                ivEMR.gone()
                ivToggleVideo.gone()
                ivToggleAudio.gone()
            }
        }
    }

    private fun hideAllViews() {
        mBinding.apply {
            iActionbar.root.gone()
            iCallSingle.root.gone()
            iUserLarge.root.gone()
            iCallMulti.root.gone()
            iCallActionButtons.root.gone()
            iIncomingCallActionButtons.root.gone()
        }
    }

    private fun setCallAcceptedViews() {
        playStopHeadUpNoti(true)
        if (DataCenter.getUser().isDoctor()) stopRingingTimer()

        mBinding.apply {
            hideAllViews()

            if (fromPush) {
                iCallSingle.tvCallStatus.gone()
                iCallMulti.iMultiRemoteView1.tvCallStatus.gone()
                iCallMulti.iMultiRemoteView2.tvCallStatus.gone()
                iCallMulti.iMultiRemoteView1.tvNameSm.visible()
                iCallMulti.iMultiRemoteView2.tvNameSm.visible()
            }

            if (isMultiParticipantCall) {
                iCallMulti.root.visible()

                twilioVideoUtils.remoteVideoViewList.forEach {

                    if (it.remoteParticipant != null) {
                        it.parentViewBinding.tvName.gone()
                        it.parentViewBinding.ripple.stopRippleAnimation()
                        it.parentViewBinding.tvCallStatus.gone()
                        it.parentViewBinding.tvCallStatusMessage.gone()
                        it.parentViewBinding.bCallAgain.gone()
                        it.parentViewBinding.tvNameSm.visible() //just for doctor use
                    }
                }
            } else {
                iCallSingle.root.visible()
                iCallSingle.tvNameSm.visible()
                iCallSingle.tvName.gone()
                iCallSingle.tvCallStatus.gone()
                iUserLarge.root.gone()
                spaceBelowActionbar.gone()
            }

            iSelfView.root.visible()
            iActionbar.apply {
                root.visible()
                ivVolume.visible()
            }
            iCallActionButtons.apply {
                root.visible()
                ivOptions.visible()
                if (fromPush.not()) //show to only doctor
                    ivEMR.visible()
                ivToggleVideo.visible()
                ivToggleAudio.visible()
            }
        }
    }

    private fun setMultipleParticipantCallUserEndedViews(remoteParticipantIdentity: String) {
        mBinding.apply {
            twilioVideoUtils.remoteVideoViewList.find { it.identity == remoteParticipantIdentity }
                ?.apply {
                    videoView.gone()
                    parentViewBinding.tvName.gone()
                    parentViewBinding.ripple.stopRippleAnimation()
                    parentViewBinding.tvNameSm.gone()
                    parentViewBinding.ivAudioStatus.gone()
                    parentViewBinding.bCallAgain.setVisible(fromPush.not()) //if is doctor
                    parentViewBinding.ivThumbnail.visible()
                    parentViewBinding.tvCallStatus.gone()
                    parentViewBinding.root.background =
                        getDrawable(R.drawable.rounded_call_light_grey_lg)
                    val callStatusMsg = if (userHasJoined) {
                        langData?.callScreen?.callStatusAfterPartLeft
                    } else langData?.callScreen?.callStatusAfterPartReject
                    parentViewBinding.tvCallStatusMessage.text = callStatusMsg?.replace(
                        "[#]", parentViewBinding.tvName.text.toString().getSafe()
                    )

                    parentViewBinding.tvCallStatusMessage.visible()
                }
        }
    }

    private fun scalingViewAnimation(view: ImageView) {
        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat("scaleX", 0.8f),
            PropertyValuesHolder.ofFloat("scaleY", 0.8f)
        )
        scaleDown.duration = 1000
        scaleDown.repeatMode = ValueAnimator.REVERSE
        scaleDown.repeatCount = ValueAnimator.INFINITE
        scaleDown.start()
    }

    private fun rippleViewAnimation() {
        mBinding.iUserLarge.ripple.startRippleAnimation()
    }

    private val callEndedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val callEndedBy = intent?.getIntExtra(Enums.BundleKeys.pnCallEndedBy.key, 0).getSafe()
            val doctorWasLeft = callEndedBy == callViewModel.orderDetailsResponse?.partnerUser?.id

            Timber.e("callEndedBy: $callEndedBy")
            if (callEndedBy != 0) {
                callEndedSet.add(callEndedBy)
            }
            Timber.e("callEndedBy size: ${callEndedSet.size}")

//            if(isMultiParticipantCall.not()/* patient */ || doctorWasLeft || callEndedSet.size == 2 /* doctor */)
//                rejectCall()

            if (doctorWasLeft) endCall()

            when (intent?.action.getSafe()) {
                actionCallEnded -> {
                    endCall()
                }

                actionCallRejected -> {
                    if (callEndedSet.size == 2 /* doctor */) endCall()

                    if (fromPush.not()) { //doctor
                        setMultipleParticipantCallUserEndedViews(
                            getIdWithVariant(callEndedBy)
                        )
                    }
                }
            }
        }
    }

    private val headsetStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Timber.e("onReceive")

            if (intent?.action.equals(Intent.ACTION_HEADSET_PLUG)) {
                if (isBluetoothConnected) return

                val state = intent?.getIntExtra("state", -1)
                when (state) {
                    0 -> {
                        Timber.e("headphone unplugged")
                        toggleSpeaker(true)
                    }
                    1 -> {
                        Timber.e("headphone plugged")
                        toggleSpeaker(false)
                    }
                    else -> {
                        Timber.e("headphone undefined")
                        toggleSpeaker(true)
                    }
                }
            } else if (intent?.action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                Timber.e("bluetooth headphone plugged")
                isBluetoothConnected = true
                toggleSpeaker(false)
            } else if (intent?.action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Timber.e("bluetooth headphone unplugged")
                isBluetoothConnected = false
                toggleSpeaker(true)
            } else {
//                Timber.e("bluetooth headphone undefined")
//                toggleSpeaker(true)
            }
        }

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    private fun getOrderDetails(request: TokenRequest) {
        callViewModel.orderDetails(request).observe(this) {
            when (it) {
                is ResponseResult.Success -> {
                    hideLoader()
                    val response = it.data as ResponseGeneral<OrderResponse>
                    response.data?.let { ordersResponse ->
                        callViewModel.orderDetailsResponse2 = ordersResponse
//                            setDataInViews()
                    }
                }
                is ResponseResult.Failure -> {
                    hideLoader()
                    DialogUtils(this).showSingleButtonAlertDialog(
                        message = it.error.message.getSafe(),
                        buttonCallback = {},
                    )
                }
                is ResponseResult.ApiError -> {
                    hideLoader()
                    DialogUtils(this).showSingleButtonAlertDialog(
                        message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                        buttonCallback = {},
                    )
                }
                is ResponseResult.Pending -> {
                    hideLoader()
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

    private fun addParticipantApiCall(participantId: Int, onSuccess: () -> Unit) {
        val request = ParticipantsRequest(callViewModel.orderId)
        request.participantId = participantId
        callViewModel.addParticipantsToVideoCall(request).observe(this) {
            when (it) {
                is ResponseResult.Success -> {
                    callEndedSet.remove(participantId)
                    onSuccess.invoke()
                }
                is ResponseResult.Failure -> {
                    hideLoader()
                }
                is ResponseResult.ApiError -> {

                }
                is ResponseResult.Pending -> {
                    hideLoader()
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


    override fun onUserLeaveHint() {
        val pictureInPictureParamsBuilder: PictureInPictureParams.Builder =
            PictureInPictureParams.Builder()
        pictureInPictureParamsBuilder.setAspectRatio(getPipRatio())
        pictureInPictureParamsBuilder.setSourceRectHint(Rect(0, 0, 100, 100))
        val pictureInPictureParams: PictureInPictureParams = pictureInPictureParamsBuilder.build()

        if (isInPictureInPictureMode) {
            return
        } else {
            enterPictureInPictureMode(pictureInPictureParams)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        if (isInPictureInPictureMode) {
            mBinding.iSelfView.cardView.gone()
            mBinding.flEMRNav.gone()
            mBinding.iActionbar.root.gone()
            mBinding.iCallActionButtons.root.gone()
        } else {
            mBinding.iSelfView.cardView.visible()
           // mBinding.flEMRNav.visible()
            mBinding.iActionbar.root.visible()
            mBinding.iCallActionButtons.root.visible()
        }
    }


    fun getPipRatio(): Rational {
        val width = window.decorView.width
        val height = window.decorView.height
      //  return Rational(width, height)
        return Rational(12, 16)
    }
}