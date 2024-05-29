package com.homemedics.app.ui.fragment.address

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.utils.TinyDB
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAddAddressBinding
import com.homemedics.app.model.AddressModel
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.util.*


class AddAddressFragment : BaseFragment(), View.OnClickListener, GoogleMap.OnCameraIdleListener {
    private lateinit var mBinding: FragmentAddAddressBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val addressModel = AddressModel()
    private var permissionCheck: Boolean = false
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            ApplicationClass.localeManager.updateLocaleData(requireContext(), TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key))

            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    if (data != null) {
                        val place = data?.let { Autocomplete.getPlaceFromIntent(it) }
                        animateCameraToLatLng(place?.latLng?.latitude.getSafe(), place?.latLng?.longitude.getSafe())
                        getLocation(place?.latLng)
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    if (data != null) {
                        val status = data?.let { Autocomplete.getStatusFromIntent(it) }
                        Toast.makeText(
                            context,
                            "Error: " + status?.statusMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
    private val startIntentSender =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_CANCELED) {
                hideLoader()
//                findNavController().popBackStack()
                setMap()
                zoomMap(30.3753, 69.3451) // if canceled then show Pakistan by default
                if (::mMap.isInitialized)
                    getLocation(mMap.cameraPosition.target)
            } else if (it.resultCode == Activity.RESULT_OK) {

                requestPermissions()
            }
        }

    private val permissionsResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { success ->
            if (success.isEmpty().not()) {
                if (success.containsValue(false)) {
                    displayNeverAskAgainDialog()
                } else {

                    initMethods()
                }
            }
        }
    private val viewModel: ProfileViewModel by activityViewModels()
    private lateinit var mMap: GoogleMap
    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnCameraIdleListener(this)

        requestLocation()
    }

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            customActionbar.title = lang?.globalString?.chooseLocation.getSafe()
        }
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_add_address

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAddAddressBinding
    }

    override fun setListeners() {
        mBinding. customActionbar.onAction2Click = {
            findNavController().popBackStack()
        }
        mBinding.bConfirmLoc.setOnClickListener(this)
        mBinding.edit.setOnClickListener(this)
    }

    private fun setMap(){
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync {
            mMap = it
            mMap.setOnCameraIdleListener(this)
        }
    }

    override fun init() {

        checkPermission()
        enableLocationSettings()
        handleBackPress()
        mBinding.lifecycleOwner = this@AddAddressFragment

        val apiKey =getString(R.string.google_map_key)
        if (Places.isInitialized().not()) {
            Places.initialize(requireContext(), apiKey)
        }


        setObserver()
    }
    private fun setObserver(){
        viewModel.address.observe(this) {
            it?.let {
                mBinding.addressModel = it
            }
        }
    }


    private fun initMethods() {

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.bConfirmLoc -> {
                val fromBDC=arguments?.getBoolean("fromBDC")
                findNavController().safeNavigate(R.id.action_addAddressFragment_to_addressBottomSheet,
                    bundleOf("fromBDC" to fromBDC))

            }
            R.id.edit -> {
                searchCalled()
            }

        }
    }

    private fun searchCalled() {
        // Set the fields to specify which types of place data to return.
        val fields = Arrays.asList(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )
        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN, fields
        )
//            .setCountry("PK") //Pakistan
            .build(context)
        resultLauncher.launch(intent)

    }

    override fun onCameraIdle() {
        mMap.setOnCameraIdleListener {
            getLocation(mMap.cameraPosition.target)
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

    private fun zoomMap(latitude: Double?, longitude: Double?) {
        latitude?.let { longitude?.let { lon -> LatLng(it, lon) } }?.let { it ->
            CameraUpdateFactory.newLatLngZoom(
                it,
                Constants.ZOOM_LEVEL
            )
        }?.let { result ->
            if(::mMap.isInitialized)
            mMap.moveCamera(
                result
            )
        }
    }

    private fun getLocation(coordinates: LatLng?) {
        val longitude = coordinates?.longitude
        val latitude = coordinates?.latitude

        CoroutineScope(Dispatchers.IO).launch {
            if (activity != null && isAdded) {
                val locationAddress = getAddressFromLatLng(
                    latitude.getSafe(),
                    longitude.getSafe()
                )
                if (locationAddress != null) {
                    withContext(Dispatchers.Main) {
                        setValues(locationAddress)
                    }
                } else {
                    Timber.i("No location found")

                }
            }
        }
    }

    private fun setValues(locationAddress: Address) {
        var city: String = locationAddress.locality ?: ""
        val state: String = locationAddress.adminArea ?: ""
        if(city.isNotEmpty())
            city="$city ,"
        addressModel.apply {
            latitude=locationAddress.latitude
            longitude=locationAddress.longitude
            address=locationAddress.getAddressLine(0) ?: ""
            subLocality=locationAddress.subLocality ?: ""
            streetAddress=locationAddress.thoroughfare?:""
            region="$city $state"
        }
        mBinding.bConfirmLoc.isEnabled = true
        viewModel.pickedAddressModel = addressModel
        viewModel._address.postValue(addressModel)
        mBinding.addressModel = viewModel._address.value //observer was not being called in some cases thats why
        hideLoader()
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
                animateCameraToLatLng(location.latitude, location.longitude)

                CoroutineScope(Dispatchers.IO).launch {
                    if (activity != null && isAdded) {
                        val locationAddress = getAddressFromLatLng(
                            location.latitude.getSafe(),
                            location.longitude.getSafe()
                        )
                        if (locationAddress != null) {
                            withContext(Dispatchers.Main) {

                                //set address
                                setValues(locationAddress = locationAddress)
                            }
                        }
                    }
                }

            }
        }.addOnCompleteListener {

        }
    }

    private fun getAddressFromLatLng(
        latitude: Double?,
        longitude: Double?,
        locationName: String = ""
    ): Address? {
        val addresses: List<Address>?

        val geocoder = Geocoder(requireActivity())
        try {
            if (locationName.isEmpty().not()) {
                addresses = geocoder.getFromLocationName(locationName, 1)
            } else {
                addresses = latitude?.let {
                    longitude?.let { it1 ->
                        geocoder.getFromLocation(
                            it,
                            it1,
                            1
                        )
                    }

                }
            }

            return if (addresses?.size?.equals(0)?.not() == true) {
                addresses[0]
            } else {
                null
            }
        } catch (e: IOException) {
            return null
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.e("onResume")
        if (permissionCheck){
            permissionCheck=false
            checkPermission()
        }
    }

    override fun onStop() {
        super.onStop()
        Timber.e("onstop")
        permissionCheck = true
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

    private fun checkUserRequestedDontAskAgain() {
        val rationalFalgFINE =
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
        val rationalFalgCOARSE =
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (!rationalFalgFINE && !rationalFalgCOARSE) {
            displayNeverAskAgainDialog()
        } else {
            requestPermissions()
        }
    }

    private fun displayNeverAskAgainDialog() {
        DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
            message = mBinding.lang?.locationString?.locationPermission.getSafe(),
            positiveButtonStringText = mBinding.lang?.dialogsStrings?.permitManual.getSafe(),
            negativeButtonStringText = mBinding.lang?.globalString?.close.getSafe(),
            buttonCallback = {
                context?.let { gotoAppSettings(it) }
            },
            negativeButtonCallback = {
                setMap()
            },
            cancellable = false
        )

    }

    private fun requestPermissions() {
        permissionsResultLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    private fun handleBackPress(){
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Do custom work here

                    // if you want onBackPressed() to be called as normal afterwards
                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
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
            .addOnSuccessListener(requireActivity()) { response: LocationSettingsResponse? -> }
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

}