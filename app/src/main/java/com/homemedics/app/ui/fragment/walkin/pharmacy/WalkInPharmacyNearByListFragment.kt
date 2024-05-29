package com.homemedics.app.ui.fragment.walkin.pharmacy

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
import com.fatron.network_module.models.response.meta.City
import com.fatron.network_module.models.response.walkinpharmacy.WalkInItemResponse
import com.fatron.network_module.models.response.walkinpharmacy.WalkInPharmacyListResponse
import com.fatron.network_module.repository.ResponseResult
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentWalkInPharmacyNearbyListBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.model.UserModel
import com.homemedics.app.ui.adapter.WalkInGenericAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ProfileViewModel
import com.homemedics.app.viewmodel.WalkInViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class WalkInPharmacyNearByListFragment : BaseFragment() {

    private lateinit var mBinding: FragmentWalkInPharmacyNearbyListBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var categoryAdapter: WalkInGenericAdapter
    private val walkInViewModel: WalkInViewModel by activityViewModels()
    private val profileViewModel : ProfileViewModel by activityViewModels()
    private var langData: RemoteConfigLanguage? = null
    private var currentLatLng: Location? = null
    private var cityId: Int = 0
    private var citylist: List<City>? = null
    private var fromSearch = false
    private var isReload = false
    var isPermission = false
    private val pharmaList: ArrayList<WalkInItemResponse> = arrayListOf()

    private val startIntentSender =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_CANCELED) {
                cityId = DataCenter.getUser()?.cityId.getSafe()
                getWalkInPharmacyList(page = walkInViewModel.page)
                walkInViewModel.fromCode = false
                isPermission = false
            } else if (it.resultCode == Activity.RESULT_OK) {
                cityId = 0
                showLoader()
                isPermission = true
                requestPermissions()
            }
        }

    private val permissionsResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { success ->
            if (success.isEmpty().not()) {
                if (success.containsValue(false)) {
                    hideLoader()
                    cityId = DataCenter.getUser()?.cityId.getSafe()
                    getWalkInPharmacyList(page = walkInViewModel.page)
                } else {
                    fusedLocationClient =
                        LocationServices.getFusedLocationProviderClient(requireActivity())
                    requestLocation()
                }
            }
        }

    private var handler: Handler = Handler(Looper.getMainLooper())

    private var delay: Long = 1000 // 1 seconds after user stops typing

    private var lastTextEdit: Long = 0

    private val inputFinishChecker = Runnable {

        if (System.currentTimeMillis() > lastTextEdit + delay - 500) {
            val search = mBinding.etSearch.text.toString()

            cityId = DataCenter.getUser()?.cityId.getSafe()
            walkInViewModel.page = 0
            if (activity != null && isAdded) {
                lifecycleScope.launch {
                    categoryAdapter.listItems.clear()
                    getWalkInPharmacyList(displayName = search, page = walkInViewModel.page)
                }
            }
        }
    }

    private var loading = false

    private var isLastPage = false

    private var pastVisibleItems = 0

    private var visibleItemCount = 0

    private var totalItemCount = 0

    private var currentPage: Int? = 1

    override fun setLanguageData() {
        langData = ApplicationClass.mGlobalData
        mBinding.apply {
            lang = langData
            textView.text = lang?.walkInScreens?.walkInDesc
            cdCity.hint = langData?.globalString?.city.getSafe()
            etSearch.hint = langData?.walkInScreens?.searchHintPharmacy.getSafe()
            tvSearchCategory.text = langData?.walkInScreens?.nearbyPharmacies.getSafe()
            tvNoData.text = langData?.globalString?.noResultFound.getSafe()
        }
    }

    override fun init() {
        // cityId = DataCenter.getUser()?.cityId.getSafe()
        setCityList(DataCenter.getUser()?.countryId.getSafe())

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

    override fun getFragmentLayout() = R.layout.fragment_walk_in_pharmacy_nearby_list

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentWalkInPharmacyNearbyListBinding
    }

    private fun setCityList(countryId: Int) {

        citylist = getCityList(countryId)
        val cityList = citylist?.map { it.name } as ArrayList<String>

        if (cityList.size.getSafe() > 0) {
            mBinding.apply {
                cdCity.data = cityList
                if (DataCenter.getUser()?.cityId != 0) {
                    val index =
                        citylist?.indexOfFirst { it.id == DataCenter.getUser()?.cityId}
                    if (index != -1)
                        mBinding.cdCity.selectionIndex = index.getSafe()
//                else
//                    mBinding.cdCity.selectionIndex = 0 //  select 0 index city
                } else {
                    profileViewModel._userProfile.value?.cityId = metaData?.cities?.get(0)?.id.getSafe()
                }
                cdCity.onItemSelectedListener = { _, position: Int ->
                    cityId = citylist?.get(position)?.id.getSafe()
                    mBinding.cdCity.errorText = null
                    //again call the api with updated cityID
                    getWalkInPharmacyList(page = walkInViewModel.page)
                }
            }
        } else
            mBinding.cdCity.data = arrayListOf<String>()
    }

    override fun setListeners() {
        mBinding.apply {
            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

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
                            getWalkInPharmacyList(page = walkInViewModel.page)
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
                                    if (isLastPage.not()) {
                                        getWalkInPharmacyList(page = walkInViewModel.page)
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
                walkInViewModel.pharmacyId = item.id
                walkInViewModel.walkInPharmacyName = item.displayName
                walkInViewModel.cityId = item.cityId.getSafe()
                walkInViewModel.mapLatLng.apply {
                    latitude = item.latitude.getSafe()
                    longitude = item.longitude.getSafe()
                }
                if (walkInViewModel.isDiscountCenter) {
                    findNavController().safeNavigate(
                        WalkInPharmacyServicesFragmentDirections.actionWalkInPharmacyServicesFragmentToWalkInDiscountsCenterFragment()
                    )
                } else {
                    findNavController().safeNavigate(
                        WalkInPharmacyServicesFragmentDirections.actionWalkInPharmacyServicesFragmentToWalkInPharmacyDetailsFragment()
                    )
                }
            }
        }
    }

    private fun setObserver() {
        if (isReload.not()) {
            walkInViewModel.walkInPharmacyListLiveData.observe(this) { walkInPharmaResponse ->
                walkInPharmaResponse?.let { pharmacyResponse ->
                    mBinding.apply {
                        rvCategory.setVisible((pharmacyResponse.isEmpty().not()))
                        tvNoData.setVisible((pharmacyResponse.isEmpty()))
                    }
                    pharmaList.clear()
                    pharmaList.apply {
                        addAll(pharmacyResponse)
                    }
                    categoryAdapter.listItems = pharmaList

                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        walkInViewModel.fromCode = true
        pharmaList.clear()
        walkInViewModel.page = 1
        walkInViewModel.walkInPharmacyListLiveData.postValue(arrayListOf())
    }

    override fun onResume() {
        super.onResume()
        pharmaList.clear()
        categoryAdapter.listItems.clear()
        walkInViewModel.page = 1
        walkInViewModel.walkInPharmacyListLiveData.postValue(arrayListOf())
        if (isPermission.not())
            if (walkInViewModel.fromCode && walkInViewModel.fromFilter.not()) {
                getWalkInPharmacyList(page = walkInViewModel.page)
            }
    }

    override fun onStop() {
        super.onStop()
        closeKeypad()
        isReload = true
        pharmaList.clear()
        walkInViewModel.page = 1
        walkInViewModel.walkInPharmacyListLiveData.postValue(arrayListOf())
    }

    override fun onDestroy() {
        super.onDestroy()
        // flush data
        mBinding.etSearch.setText("")
        isReload = false
        fromSearch = false
        walkInViewModel.fromCode = false
        handler.removeCallbacks(inputFinishChecker)
        walkInViewModel.page = 1
        walkInViewModel.walkInPharmacyListLiveData.postValue(arrayListOf())
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
            Timber.e("success")
            if (location != null) {
                currentLatLng = Location("")
                currentLatLng?.latitude = location.latitude
                currentLatLng?.longitude = location.longitude
                walkInViewModel.currentLatLng = currentLatLng
                hideLoader()
                if (walkInViewModel.fromCode.not())
                    getWalkInPharmacyList(page = walkInViewModel.page)

                if (isPermission) {
                    getWalkInPharmacyList(page = walkInViewModel.page)
                }
            } else {
                hideLoader()
            }
        }.addOnCompleteListener { }
    }

    private fun getWalkInPharmacyList(
        displayName: String? = null,
        page: Int? = null
    ) {
        val cityID =
            if ((currentLatLng?.latitude.getSafe() > 0.0 && currentLatLng?.longitude.getSafe() > 0.0) && cityId == 0) {
                null
            } else if (!(currentLatLng?.latitude.getSafe() > 0.0 && currentLatLng?.longitude.getSafe() > 0.0) && cityId == 0) {
                DataCenter.getUser()?.cityId.getSafe()
            } else {
                cityId
            }


        val request = WalkInListRequest(
            latitude = if (cityId != 0) 0.0 else currentLatLng?.latitude.getSafe(),
            longitude = if (cityId != 0) 0.0 else currentLatLng?.longitude.getSafe(),
            search = displayName,
            //cityId = if (currentLatLng?.latitude.getSafe() > 0.0 && currentLatLng?.longitude.getSafe() > 0.0) null else DataCenter.getUser()?.cityId.getSafe() ,
            cityId = cityID,
            page = page,
            discountCenter = if (walkInViewModel.isDiscountCenter) 1 else 0
        )
        if (isAdded)
            if (isOnline(requireContext())) {
                walkInViewModel.getWalkInPharmacyList(request).observe(this) {
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<*>
                            (response.data as WalkInPharmacyListResponse).let { pharmaciesList ->
                                isLastPage =
                                    request.page == pharmaciesList.walkInPharmacies?.lastPage
                                currentPage = pharmaciesList.walkInPharmacies?.currentPage
                                walkInViewModel.walkInPharmacyListLiveData.postValue(
                                    pharmaciesList.walkInPharmacies?.walkInList.getSafe()
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