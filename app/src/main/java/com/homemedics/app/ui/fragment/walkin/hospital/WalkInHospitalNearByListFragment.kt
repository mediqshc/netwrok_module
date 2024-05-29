package com.homemedics.app.ui.fragment.walkin.hospital

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.request.walkin.WalkInListRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.walkinpharmacy.WalkInItemResponse
import com.fatron.network_module.models.response.walkinpharmacy.WalkInPharmacyListResponse
import com.fatron.network_module.repository.ResponseResult
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentWalkInHospitalNearbyListBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.ui.adapter.WalkInGenericAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.WalkInViewModel
import kotlinx.coroutines.launch

class WalkInHospitalNearByListFragment : BaseFragment() {

    private lateinit var mBinding: FragmentWalkInHospitalNearbyListBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var categoryAdapter: WalkInGenericAdapter
    private val walkInViewModel: WalkInViewModel by activityViewModels()
    private val hospitalList: ArrayList<WalkInItemResponse> = arrayListOf()
    private var langData: RemoteConfigLanguage? = null
    private var currentLatLng: Location? = null
    private var cityId: Int = 0
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var delay: Long = 1000 // 1 seconds after user stops typing
    private var lastTextEdit: Long = 0
    private var fromSearch = false
    private var isReload = false
    private var loading = false
    private var isLastPage = false
    private var pastVisibleItems = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0
    private var currentPage: Int? = 1

    private val inputFinishChecker = Runnable {
        if (System.currentTimeMillis() > lastTextEdit + delay - 500) {
            val search = mBinding.etSearch.text.toString()
            cityId = DataCenter.getUser()?.cityId.getSafe()
            walkInViewModel.page = 0
            if (activity != null && isAdded) {
                lifecycleScope.launch {
                    categoryAdapter.listItems.clear()
                    getWalkInHospitalList(displayName = search, page = walkInViewModel.page)
                }
            }
        }
    }

    private val startIntentSender = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        if (it.resultCode == Activity.RESULT_CANCELED) {
            cityId = DataCenter.getUser()?.cityId.getSafe()
            isNoPermission = true
            getWalkInHospitalList(page = walkInViewModel.page)
        } else if (it.resultCode == Activity.RESULT_OK) {
            cityId = 0
            showLoader()
            isPermission = true
            requestPermissions()
        }
    }

    var isPermission = false
    var isNoPermission = false

    private val permissionsResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { success ->
        if (success.isEmpty().not()) {
            if (success.containsValue(false)) {
                hideLoader()
                cityId = DataCenter.getUser()?.cityId.getSafe()
                if (walkInViewModel.fromFilter.not())
                    getWalkInHospitalList(page = walkInViewModel.page)
            } else {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                requestLocation()
            }
        }
    }

    override fun setLanguageData() {
        langData = ApplicationClass.mGlobalData
        mBinding.apply {
            lang = langData
            etSearch.hint = langData?.walkInScreens?.searchHint.getSafe()
            tvNoData.text = langData?.globalString?.noWalkInHospital.getSafe()
        }
    }

    override fun init() {
        cityId = DataCenter.getUser()?.cityId.getSafe()
        showLoader()
        setObserver()
        populateList()
        checkPermission()
        enableLocationSettings()
    }

    private fun populateList() {
        categoryAdapter = WalkInGenericAdapter()
        mBinding.rvCategory.adapter = categoryAdapter
    }

    override fun getFragmentLayout() = R.layout.fragment_walk_in_hospital_nearby_list

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentWalkInHospitalNearbyListBinding
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
                        fromSearch = true
                        lastTextEdit = System.currentTimeMillis()
                        handler.postDelayed(inputFinishChecker, delay)
                    } else {
                        lastTextEdit = 0
                        categoryAdapter.listItems.clear()
                        categoryAdapter.listItems = arrayListOf()
                        tvNoData.visible()
                        fromSearch = false
                        walkInViewModel.page = 1
                        if (etSearch.hasFocus())
                            getWalkInHospitalList(page = walkInViewModel.page)
                    }
                }
            })

            val layoutManager = rvCategory.layoutManager as LinearLayoutManager
            rvCategory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (fromSearch.not()) {
                        if (dy > 0) { //check for scroll down
                            visibleItemCount = layoutManager.childCount
                            totalItemCount = layoutManager.itemCount
                            pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
                            if (loading.not()) {
                                if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                                    walkInViewModel.page = currentPage?.plus(1)
                                    if(isLastPage.not()) {
                                        getWalkInHospitalList(page = walkInViewModel.page)
                                        loading = true
                                    }
                                }
                            }
                        }
                    }
                }
            })

            categoryAdapter.itemClickListener = { item, _ ->
                walkInViewModel.walkInItem = item
                walkInViewModel.fromCode = false
                walkInViewModel.hospitalId = item.id
                walkInViewModel.walkInHospitalName = item.displayName
                walkInViewModel.cityId = item.cityId.getSafe()
                walkInViewModel.mapLatLng.apply {
                    latitude = item.latitude.getSafe()
                    longitude = item.longitude.getSafe()
                }
                if (walkInViewModel.isDiscountCenter) {
                    findNavController().safeNavigate(
                        WalkInHospitalServicesFragmentDirections.actionWalkInHospitalServicesFragmentToWalkInDiscountsCenterFragment3()
                    )
                } else {
                    findNavController().safeNavigate(
                        WalkInHospitalServicesFragmentDirections.actionWalkInHospitalServicesFragmentToWalkInHospitalSelectServiceFragment()
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hospitalList.clear()
        categoryAdapter.listItems.clear()
        walkInViewModel.page = 1
        walkInViewModel.walkInHospitalListLiveData.postValue(arrayListOf())
        if (isPermission.not() && isNoPermission.not())
            if (walkInViewModel.fromCode && walkInViewModel.fromFilter.not()) {
                walkInViewModel.noReload = true
                getWalkInHospitalList(page = walkInViewModel.page)
            }

    }

    override fun onPause() {
        super.onPause()
        walkInViewModel.fromCode = true
    }

    override fun onStop() {
        super.onStop()
        closeKeypad()
        isReload = true
        hospitalList.clear()
        categoryAdapter.listItems.clear()
        walkInViewModel.page = 1
        walkInViewModel.walkInHospitalListLiveData.postValue(arrayListOf())
        walkInViewModel.fromFilter = false
    }

    override fun onDestroy() {
        super.onDestroy()
        // flush data
        mBinding.etSearch.setText("")
        isReload = false
        fromSearch = false
        walkInViewModel.fromCode = false
        walkInViewModel.fromFilter = false
        walkInViewModel.noReload = false
        handler.removeCallbacks(inputFinishChecker)
        hospitalList.clear()
        categoryAdapter.listItems.clear()
        walkInViewModel.page = 1
        walkInViewModel.walkInHospitalListLiveData.postValue(arrayListOf())
    }

    private fun setObserver() {
        if (isReload.not()) {
            walkInViewModel.walkInHospitalListLiveData.observe(this) { walkInHospitalResponse ->
                walkInHospitalResponse?.let { hospitalResponse ->
                    mBinding.apply {
                        rvCategory.setVisible((hospitalResponse.isEmpty().not()))
                        tvNoData.setVisible((hospitalResponse.isEmpty()))
                    }
                    hospitalList.apply {
                        addAll(hospitalResponse)
                    }
                    categoryAdapter.listItems = hospitalList
                }
            }
        }
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
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            requestLocation()
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

    private fun requestLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
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
                hideLoader()
                if (walkInViewModel.fromFilter.not() && walkInViewModel.noReload.not()) {
                    if (walkInViewModel.packageAccountId != 0 || walkInViewModel.hosiptalServiceId != 0)
                        getWalkInHospitalList(page = walkInViewModel.page, fromFilter = true)
                    else if (isPermission)
                        getWalkInHospitalList(page = walkInViewModel.page)
                    else
                        getWalkInHospitalList(page = walkInViewModel.page)
                }

            } else {
                hideLoader()
            }
        }.addOnCompleteListener { }
    }

    private fun getWalkInHospitalList(displayName: String? = null, page: Int? = null, fromFilter: Boolean? = null) {
        val request = if (fromFilter.getSafe()) {
            WalkInListRequest(
                latitude = walkInViewModel.currentLatLng?.latitude.getSafe(),
                longitude = walkInViewModel.currentLatLng?.longitude.getSafe(),
                service = if (walkInViewModel.hosiptalServiceId != 0) walkInViewModel.hosiptalServiceId else null,
                filterPackage = if (walkInViewModel.packageAccountId != 0) walkInViewModel.packageAccountId else null,
                page = 0
            )
        } else { WalkInListRequest(
            currentLatLng?.latitude.getSafe(),
            currentLatLng?.longitude.getSafe(),
            search = displayName,
            cityId = if (currentLatLng?.latitude.getSafe() > 0.0 && currentLatLng?.longitude.getSafe() > 0.0) null else DataCenter.getUser()?.cityId.getSafe(),
            page = page,
            discountCenter = if (walkInViewModel.isDiscountCenter) 1 else 0
        ) }

        if (isAdded)
            if (isOnline(requireActivity())) {
                walkInViewModel.getWalkInHospitalList(request).observe(this) {
                    when(it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<*>
                            (response.data as WalkInPharmacyListResponse).let { hospitalList ->
                                isLastPage = request.page == hospitalList.walkInHospitals?.lastPage
                                currentPage = hospitalList.walkInHospitals?.currentPage
                                walkInViewModel.walkInHospitalListLiveData.postValue(
                                    hospitalList.walkInHospitals?.walkInList.getSafe()
                                )
                                loading = false
                                mBinding.tvNoData.setVisible(categoryAdapter.listItems.isEmpty())
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