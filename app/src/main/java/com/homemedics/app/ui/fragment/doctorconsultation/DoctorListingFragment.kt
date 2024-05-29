package com.homemedics.app.ui.fragment.doctorconsultation

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.meta.Specialties
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentDoctorListBinding
import com.homemedics.app.databinding.ItemBorderedTextviewBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.activity.AuthActivity
import com.homemedics.app.ui.adapter.DoctorListAdapter
import com.homemedics.app.ui.adapter.LoaderStateAdapter
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.safeNavigate
import com.homemedics.app.utils.setVisible
import com.homemedics.app.viewmodel.DoctorConsultationViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class DoctorListingFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentDoctorListBinding

    private lateinit var doctorListingAdapter: DoctorListAdapter
    var delay: Long = 1000 // 1 seconds after user stops typing

    var last_text_edit: Long = 0

    var handler: Handler = Handler(Looper.getMainLooper())
    private val doctorConsultationViewModel: DoctorConsultationViewModel by activityViewModels()
    private val langData = ApplicationClass.mGlobalData
    lateinit var loaderStateAdapter: LoaderStateAdapter
    override fun setLanguageData() {
        val bookingData = langData?.bookingScreen
        mBinding.apply {
            etSearch.hint = bookingData?.searchDoctor
            tvNoData.text = bookingData?.noDoctorFound
            actionbar.title = bookingData?.bookConsult.getSafe()
        }
    }

    override fun onDetach() {
        super.onDetach()
        doctorConsultationViewModel.bdcFilterRequest.partnerName = ""
    }

    override fun init() {
        populateDoctorList()
        setupFilterChips()
        val locale = TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
        mBinding.flTags.isRtl = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN


        lifecycleScope.launch {
            doctorListingAdapter.loadStateFlow.collect { loadState ->
                val isListEmpty =
                    loadState.refresh is LoadState.NotLoading && doctorListingAdapter.itemCount == 0

                mBinding.apply {
                    rvDoctor.setVisible(isListEmpty.not())
                    tvNoData.setVisible((!doctorConsultationViewModel.fromSearch && doctorListingAdapter.itemCount == 0) || (isListEmpty && last_text_edit > 0))

                }

                if (loadState.refresh is LoadState.Loading)
                    showLoader()
                else
                    hideLoader()
            }
        }

        if (doctorConsultationViewModel.fromSearch.not()) {
            lifecycleScope.launch {
                doctorConsultationViewModel.items.collectLatest {

                    setData(it)
                }

            }
        } else {
            mBinding.etSearch.requestFocus()
            showKeypad()
        }
        doctorConsultationViewModel.fromSearch = false
    }

    private fun setupFilterChips() {
        doctorConsultationViewModel.bdcFilterRequest.apply {
            if (countryName.isNotEmpty()) {
                val chipView = ItemBorderedTextviewBinding.inflate(layoutInflater)
                chipView.tvTitle.apply {
                    text = countryName
                    setVisible(false)
                }
                mBinding.flTags.addView(chipView.root)
            }
            if (cityName.isNotEmpty()) {
                val chipView = ItemBorderedTextviewBinding.inflate(layoutInflater)
                chipView.tvTitle.text = cityName
                mBinding.flTags.addView(chipView.root)
            }
            if (specialityName.isNotEmpty()) {
                val chipView = ItemBorderedTextviewBinding.inflate(layoutInflater)
                chipView.tvTitle.text = specialityName
                mBinding.flTags.addView(chipView.root)
            }
            if (genderName.isNotEmpty()) {
                val chipView = ItemBorderedTextviewBinding.inflate(layoutInflater)
                chipView.tvTitle.apply {
                    text = countryName
                    setVisible(false)
                }
                mBinding.flTags.addView(chipView.root)
            }
            if (serviceName.isNotEmpty()) {
                val chipView = ItemBorderedTextviewBinding.inflate(layoutInflater)
                chipView.tvTitle.text = serviceName
                mBinding.flTags.addView(chipView.root)
            }
        }

        mBinding.apply {
            tvFilterBadge.text = "${flTags.childCount}"
            tvFilterBadge.setVisible(flTags.childCount > 0)
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_doctor_list

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentDoctorListBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            ivSort.setOnClickListener {
                findNavController().safeNavigate(DoctorListingFragmentDirections.actionDoctorListingFragmentToDoctorFilterFragment())
            }

            doctorListingAdapter.itemClickListener = { item, _ ->
                if (isUserLoggedIn()) {
                    doctorConsultationViewModel.partnerProfileResponse = item
                    item as PartnerProfileResponse
                    item.specialities?.apply {
                        this.forEach {
                            val temp = it.genericItemId?.toInt()
                            doctorConsultationViewModel.bdcFilterRequest.specialityId = temp
                        }
                    }



                    handler.removeCallbacks(input_finish_checker);
                    etSearch.text.clear()
                    findNavController().safeNavigate(DoctorListingFragmentDirections.actionDoctorListingFragmentToBookConsultationDetailsFragment())
                } else {
                    val intent = Intent(requireActivity(), AuthActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }

            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    handler.removeCallbacks(input_finish_checker);
                }

                override fun afterTextChanged(s: Editable?) {
                    //avoid triggering event when text is empty
                    if (s?.length.getSafe() > 0) {
                        last_text_edit = System.currentTimeMillis();
                        handler.postDelayed(input_finish_checker, delay);
                    } else {
                        last_text_edit = 0
                        if (doctorListingAdapter.itemCount > 0)
                            doctorListingAdapter.submitData(lifecycle, PagingData.empty())

//                        doctorConsultationViewModel.doctorListing.postValue(arrayListOf())
                    }
                }
            }

            )

        }
    }

    private val input_finish_checker = Runnable {
        if (System.currentTimeMillis() > last_text_edit + delay - 500) {
            doctorConsultationViewModel.bdcFilterRequest.partnerName =
                mBinding.etSearch.text.toString()
            doctorListingAdapter.submitData(lifecycle, PagingData.empty())
//            showLoader()
            lifecycleScope.launch {
                doctorConsultationViewModel.items.collectLatest {
                    setData(it)

                }
            }
//            getDoctors(doctorConsultationViewModel.bdcFilterRequest)
        }
    }


    private suspend fun setData(pagingData: PagingData<PartnerProfileResponse>) {
        doctorListingAdapter.submitData(pagingData)

    }

    override fun onClick(v: View?) {

    }

    private fun populateDoctorList() {
        mBinding.apply {
            doctorListingAdapter = DoctorListAdapter()
            loaderStateAdapter = LoaderStateAdapter { doctorListingAdapter.retry() }
            rvDoctor.adapter = doctorListingAdapter.withLoadStateFooter(loaderStateAdapter)

        }
    }

}