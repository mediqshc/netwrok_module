package com.homemedics.app.ui.fragment.walkin.laboratory

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.walkin.WalkInListRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.walkinpharmacy.WalkInItemResponse
import com.fatron.network_module.models.response.walkinpharmacy.WalkInPharmacyListResponse
import com.fatron.network_module.repository.ResponseResult
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogMapGenericBinding
import com.homemedics.app.databinding.FragmentWalkInPharmacyMapViewBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.WalkInViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WalkinLabMapViewFragment : BaseFragment(), GoogleMap.OnCameraIdleListener {

    private lateinit var mBinding: FragmentWalkInPharmacyMapViewBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var dialogSelectLaboratory: DialogMapGenericBinding
    private lateinit var builder: AlertDialog
    private lateinit var dialogSaveButton: Button
    private val walkInViewModel: WalkInViewModel by activityViewModels()
    private var langData: RemoteConfigLanguage? = null
    private var currentLatLng: Location? = null
    private var cityId: Int = 0

    private val startIntentSender = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        if (it.resultCode == Activity.RESULT_CANCELED) {
            cityId = DataCenter.getUser()?.cityId.getSafe()
            hideLoader()
            setMap()
            zoomMap(24.8607, 67.0011) // if canceled then show Karachi by default
        } else if (it.resultCode == Activity.RESULT_OK) {
            requestPermissions()
        }
    }

    private val permissionsResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { success ->
        if (success.isEmpty().not()) {
            if (success.containsValue(false)) {
                hideLoader()
                cityId = DataCenter.getUser()?.cityId.getSafe()
                getWalkInLaboratoryList()
            } else {
                initMethods()
            }
        }
    }

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnCameraIdleListener(this)
        requestLocation()
    }

    private var handler: Handler = Handler(Looper.getMainLooper())

    private var delay: Long = 1000 // 1 seconds after user stops typing

    private var lastTextEdit: Long = 0

    private val inputFinishChecker = Runnable {
        if (System.currentTimeMillis() > lastTextEdit + delay - 500) {
            val search = mBinding.etSearch.text.toString()
            cityId = DataCenter.getUser()?.cityId.getSafe()
            if (activity != null && isAdded) {
                lifecycleScope.launch {
                    mMap.clear()
                    getWalkInLaboratoryList(displayName = search)
                }
            }
        }
    }

    override fun setLanguageData() {
        langData = ApplicationClass.mGlobalData
        mBinding.apply {
            lang = langData
            textView.text=lang?.walkInScreens?.walkinLaboratoryDesc.getSafe()
            etSearch.hint = langData?.walkInScreens?.searchHintLab.getSafe()
        }
    }

    override fun init() {
        setObserver()
        checkPermission()
        enableLocationSettings()
    }

    override fun getFragmentLayout() = R.layout.fragment_walk_in_pharmacy_map_view

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentWalkInPharmacyMapViewBinding
    }

    override fun setListeners() {
        mBinding.apply {
            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    handler.removeCallbacks(inputFinishChecker)
                }

                override fun afterTextChanged(s: Editable?) {
                    //avoid triggering event when text is empty
                    if (s?.length.getSafe() > 0) {
                        lastTextEdit = System.currentTimeMillis()
                        handler.postDelayed(inputFinishChecker, delay)
                    } else {
                        lastTextEdit = 0
                        mMap.clear()
                    }
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        closeKeypad()
    }

    private fun setObserver() {
        walkInViewModel.walkInLaboratoryMapLiveData.observe(this) { walkInLabResponse ->
            lifecycleScope.launch {
                showLoader()
                setMap()
                delay(900)
                if(::mMap.isInitialized) {
                    hideLoader()
                    walkInLabResponse?.forEachIndexed{ index, walkInItem ->
                        mMap.addMarker(MarkerOptions()
                            .position(LatLng(walkInItem.latitude.getSafe(), walkInItem.longitude.getSafe()))
                            .anchor(0.5f, 0.5f)
                            .icon(bitmapDescriptorFromVector(requireActivity(), R.drawable.ic_location_on))
                        )?.apply {
                            this.tag = index.toString()
                        }
                        mMap.setOnMarkerClickListener {
                            val pos = it.tag.toString().toInt()
                            showSelectLabDialog(walkInLabResponse[pos])
                            false
                        }
                    }
                    animateCameraToLatLng(currentLatLng?.latitude, currentLatLng?.longitude)
                }
            }
        }
    }

    private fun setMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync {
            mMap = it
            mMap.setOnCameraIdleListener(this)
        }
    }

    private fun initMethods() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun checkPermission() {
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED && context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED
        ) {
            initMethods()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        permissionsResultLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun enableLocationSettings() {
        val locationRequest = LocationRequest.create()
            .setInterval(500)
            .setFastestInterval(100)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        LocationServices
            .getSettingsClient(requireContext())
            .checkLocationSettings(builder.build())
            .addOnSuccessListener(requireActivity()) { }
            .addOnFailureListener(requireActivity()) { ex ->
                if (ex is ResolvableApiException) {
                    // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                        val intentSenderRequest =
                            IntentSenderRequest.Builder(ex.resolution).build()
                        startIntentSender.launch(intentSenderRequest)

                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
    }

    private fun zoomMap(latitude: Double?, longitude: Double?) {
        latitude?.let { longitude?.let { lon -> LatLng(it, lon) } }?.let { it ->
            CameraUpdateFactory.newLatLngZoom(
                it,
                Constants.ZOOM_LEVEL_WALK_IN
            )
        }?.let { result ->
            if(::mMap.isInitialized)
                mMap.moveCamera(
                    result
                )
        }
    }

    override fun onCameraIdle() {

    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        showLoader()
        mMap.isMyLocationEnabled = true
        val cancel = CancellationTokenSource().token

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancel
        ).addOnSuccessListener(requireActivity()) { location ->
            if (location != null) {
                currentLatLng = Location("")
                currentLatLng?.latitude = location.latitude
                currentLatLng?.longitude = location.longitude
                walkInViewModel.currentLatLng = currentLatLng
                animateCameraToLatLng(location.latitude, location.longitude)
                hideLoader()
                getWalkInLaboratoryList()
            }
        }.addOnCompleteListener {
            hideLoader()
        }
    }

    private fun animateCameraToLatLng(latitude: Double?, longitude: Double?) {
        if (this::mMap.isInitialized) {
            zoomMap(latitude, longitude)
        } else {
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync {
                mMap = it
                zoomMap(latitude, longitude)
            }
        }
    }

    private fun showSelectLabDialog(walkInItem: WalkInItemResponse) {
        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogSelectLaboratory = DialogMapGenericBinding.inflate(layoutInflater).apply {
                ivIcon.loadImage(walkInItem.logo, R.drawable.ic_placeholder)
                tvTitle.text = walkInItem.displayName
                tvDesc.text = walkInItem.streetAddress
                tvDistance.apply {
                    setVisible(walkInItem.kilometer != null && walkInItem.kilometer != 0.0)
                    text = "${"%.2f".format(walkInItem.kilometer)} ${langData?.globalString?.kilometer.getSafe()}"
                }
            }
            setView(dialogSelectLaboratory.root)
            setTitle(mBinding.lang?.globalString?.selectLaboratory.getSafe())
            setPositiveButton(mBinding.lang?.globalString?.select.getSafe(),null)
            setNegativeButton(mBinding.lang?.globalString?.cancel.getSafe(), null)
        }.create()
        builder.setOnShowListener{
            dialogSaveButton = builder.getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                walkInViewModel.walkInItem = walkInItem
                walkInViewModel.labId = walkInItem.id
                walkInViewModel.walkInLabName = walkInItem.displayName
                walkInViewModel.cityId = walkInItem.cityId.getSafe()
                walkInViewModel.mapLatLng.apply {
                    latitude = walkInItem.latitude.getSafe()
                    longitude = walkInItem.longitude.getSafe()
                }
                if (walkInViewModel.isDiscountCenter) {
                    findNavController().safeNavigate(
                        WalkinLabServiceFragmentDirections.actionWalkinLabServiceFragmentToWalkInDiscountsCenterFragment22()
                    )
                } else {
                    findNavController().safeNavigate(
                        WalkinLabServiceFragmentDirections.actionWalkinLabServiceFragmentToWalkinLabDetailFragment()
                    )
                }

                builder.dismiss()
            }
        }
        builder.show()
    }

    private fun getWalkInLaboratoryList(displayName: String? = null, page: Int? = null) {
        val request = WalkInListRequest(
            currentLatLng?.latitude.getSafe(),
            currentLatLng?.longitude.getSafe(),
            search = displayName,
            cityId = if (currentLatLng?.latitude.getSafe() > 0.0 && currentLatLng?.longitude.getSafe() > 0.0) null else  DataCenter.getUser()?.cityId.getSafe(),
            page = 0,
            discountCenter = if (walkInViewModel.isDiscountCenter) 1 else 0
        )
        if (isOnline(requireActivity())) {
            walkInViewModel.getWalkInLaboratoryList(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        (response.data as WalkInPharmacyListResponse).let { labsList ->
                            walkInViewModel.walkInLaboratoryMapLiveData.postValue(
                                labsList.walkInLabs?.walkInList.getSafe()
                            )
                            if ((labsList.walkInLabs?.walkInList.getSafe()).isEmpty())
                                showToast(mBinding.lang?.globalString?.noWalkInLab.getSafe())
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.Pending -> {
                        showLoader()
                    }
                    is ResponseResult.Complete -> {
                        hideLoader()
                    }
                    else -> { hideLoader() }
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}