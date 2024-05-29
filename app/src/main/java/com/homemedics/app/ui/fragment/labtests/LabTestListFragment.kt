package com.homemedics.app.ui.fragment.labtests

import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.request.bdc.PartnerDetailsRequest
import com.fatron.network_module.models.request.labtest.LabTestCartRequest
import com.fatron.network_module.models.request.labtest.LabTestFilterRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.labtest.LabTestListResponse
import com.fatron.network_module.models.response.labtest.LabTestResponse
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.models.response.pharmacy.OrderDetailsResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentLabTestListBinding
import com.homemedics.app.databinding.ItemBorderedTextviewBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.adapter.LabTestListAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.LabTestViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class LabTestListFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentLabTestListBinding

    private lateinit var adapter: LabTestListAdapter

    private val labTestViewModel: LabTestViewModel by activityViewModels()

    var delay: Long = 1000 // 1 seconds after user stops typing
    private var taskJob: Job? = null
    private var last_text_edit: Long = 0

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.labPharmacyScreen?.labTest.getSafe()
            etSearch.hint = lang?.labPharmacyScreen?.searchLabTest.getSafe()
        }
    }

    override fun onDetach() {
        super.onDetach()

        //can't clear all filters i.e. city, country
        labTestViewModel.labTestFilterRequest.name = ""
        labTestViewModel.labTestFilterRequest.categoryName = ""
        labTestViewModel.labTestFilterRequest.categoryId = null
        labTestViewModel.labTestFilterRequest.page = 1
    }

    override fun init() {
        val savedBookingId = tinydb.getInt(com.fatron.network_module.utils.Enums.TinyDBKeys.LAB_TEST_BOOKING_ID.key)
        if(savedBookingId != 0){
            labTestViewModel.bookingIdResponse.bookingId = savedBookingId
        }

        val locale =  TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
        mBinding.flTags.isRtl = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN

        populateDoctorList()
        setupFilterChips()
        setDataInViews()
        getCartDetailsApi()

        labTestViewModel.labTestFilterRequest.page = 1

        createBookingId()
        getLabTests(labTestViewModel.labTestFilterRequest)
        if(labTestViewModel.fromSearch.not()){
            getLabTests(labTestViewModel.labTestFilterRequest)
        }
        else {
            mBinding.etSearch.requestFocus()
            showKeypad()
        }
        labTestViewModel.fromSearch = false
    }

    private fun setupFilterChips(){
        labTestViewModel.labTestFilterRequest.apply {
            if(countryName.isNullOrEmpty().not()){
                val chipView = ItemBorderedTextviewBinding.inflate(layoutInflater)
                chipView.tvTitle.apply {
                    text = countryName
                    setVisible(false)
                }
                mBinding.flTags.addView(chipView.root)
            }
            if(cityName.isNullOrEmpty().not()){
                val chipView = ItemBorderedTextviewBinding.inflate(layoutInflater)
                chipView.tvTitle.text = cityName
                mBinding.flTags.addView(chipView.root)
            }
            if(categoryName.isNullOrEmpty().not()){
                val chipView = ItemBorderedTextviewBinding.inflate(layoutInflater)
                chipView.tvTitle.text = categoryName
                mBinding.flTags.addView(chipView.root)
            }
        }

        mBinding.apply {
            tvFilterBadge.text = "${flTags.childCount}"
            tvFilterBadge.setVisible(flTags.childCount > 0)
        }
    }

    private fun setDataInViews(){
        mBinding.apply {
            actionbar.dotText = labTestViewModel.orderDetailsResponse.labCartItems?.size.getSafe().toString()
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_lab_test_list

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentLabTestListBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            actionbar.onAction2Click = {
                if(labTestViewModel.orderDetailsResponse.labCartItems.isNullOrEmpty()){
                    DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                        title = lang?.globalString?.information.getSafe(),
                        message = lang?.labPharmacyScreen?.emptyCart.getSafe(),
                    )
                }
                else {
                    findNavController().safeNavigate(LabTestListFragmentDirections.actionLabTestListFragmentToLabTestCartDetailsFragment())
                }
            }
            ivSort.setOnClickListener {
                findNavController().safeNavigate(LabTestListFragmentDirections.actionLabTestListFragmentToLabTestFilterFragment())
            }

            adapter.itemClickListener = {
                item, _ ->
                labTestViewModel.selectedLabTest = item
                etSearch.text.clear()
                labTestViewModel.labTestFilterRequest.labTestId = item.id
                findNavController().safeNavigate(LabTestListFragmentDirections.actionLabTestListFragmentToSelectMainLabFragment())
            }

            etSearch.doAfterTextChanged { s ->
                taskJob?.cancel()
                taskJob = lifecycleScope.launch {
                    if (s?.length.getSafe() > 0) {
                        last_text_edit = System.currentTimeMillis()
                        delay(delay)
                        if (System.currentTimeMillis() > last_text_edit + delay - 500) {
                            adapter.listItems.clear()
                            labTestViewModel.labTestFilterRequest.page = 0
                            getLabTests(labTestViewModel.labTestFilterRequest)
                        }
                    } else {
//                        if (last_text_edit != 0L) {
//                            adapter.listItems.clear()
////                            getLabTests(labTestViewModel.labTestFilterRequest)
//                        }
//                        last_text_edit = 0
//                        if (adapter.itemCount > 0) {
//                            adapter.listItems.clear()
//                            adapter.listItems = arrayListOf()
//                        }

                        last_text_edit = 0
                        adapter.listItems.clear()
                        adapter.listItems = arrayListOf()
                        tvNoData.visible()
                    }

                }
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private var loading = false
    var pastVisiblesItems = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var isLastPage = false
    var currentPage: Int? = 1

    private fun populateDoctorList() {
        mBinding.apply {
            adapter = LabTestListAdapter()
            val spacing = resources.getDimensionPixelSize(R.dimen.dp16)
            rvLabTests.addItemDecoration(RecyclerViewItemDecorator(spacing, RecyclerViewItemDecorator.VERTICAL))
            rvLabTests.adapter = adapter
            val layoutManager = rvLabTests.layoutManager as LinearLayoutManager

            rvLabTests.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                    if (dy > 0) { //check for scroll down
                        visibleItemCount = layoutManager.childCount
                        totalItemCount = layoutManager.itemCount
                        pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()
                        if (loading.not()) {
                            if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
//                                loading = false
                                Timber.e("Last Item Wow !")

                                labTestViewModel.labTestFilterRequest.page = currentPage?.plus(1)

                                if(isLastPage.not()){
                                    getLabTests(labTestViewModel.labTestFilterRequest)
                                    loading = true
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun getLabTests(request: LabTestFilterRequest) {
        val queryText = mBinding.etSearch.text.toString()
        if(queryText.isNotEmpty())
            request.name = queryText
        else {
            request.name = null
        }

        if (isOnline(requireActivity())) {
            labTestViewModel.getLabTests(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<LabTestListResponse>
                        response.data?.let { res ->
                            isLastPage = request.page == res.lastPage
                            currentPage = res.currentPage

                            val tempList = adapter.listItems
                            tempList.addAll((res.labTests?.data as ArrayList<LabTestResponse>).getSafe())
                            adapter.listItems = tempList.distinctBy {
//                                it.name
                                Pair(
                                    it.name,
                                    it.id
                                )
                            } as ArrayList<LabTestResponse>
                            adapter.originalList = adapter.listItems
                            adapter.notifyDataSetChanged()

                            loading = false

                            mBinding.tvNoData.setVisible(adapter.listItems.isEmpty())
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
                                message =   getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun createBookingId() {
        val request = PartnerDetailsRequest(
            serviceId = CustomServiceTypeView.ServiceType.LaboratoryService.id,
            partnerUserId = 0,
            bookingId = labTestViewModel.bookingIdResponse.bookingId
        )

        if (isOnline(requireActivity())) {
            labTestViewModel.createBookingId(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<PartnerProfileResponse>
                        response.data?.let {
                            if(labTestViewModel.bookingIdResponse.bookingId != it.bookingId)
                                labTestViewModel.flushData(true)

                            labTestViewModel.bookingIdResponse = it
                            labTestViewModel.bookingIdResponse.bookingId?.let { bookingId ->
                                tinydb.putInt(com.fatron.network_module.utils.Enums.TinyDBKeys.LAB_TEST_BOOKING_ID.key, bookingId)
                            }
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
//                        hideLoader()
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

    private fun getCartDetailsApi(){
        val request = LabTestCartRequest(
            bookingId = labTestViewModel.bookingIdResponse.bookingId
        )

        if (isOnline(requireActivity())) {
            labTestViewModel.getCartDetails(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<OrderDetailsResponse>

                        response.data?.let { details ->
                            labTestViewModel.orderDetailsResponse = details
                            setDataInViews()
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message =it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                cancellable = false,
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.Pending -> {
                        showLoader()
                    }
                    is ResponseResult.Complete -> {
//                        hideLoader()
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