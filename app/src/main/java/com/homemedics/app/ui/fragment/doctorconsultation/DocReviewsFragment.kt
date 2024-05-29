package com.homemedics.app.ui.fragment.doctorconsultation

import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.bdc.PartnerDetailsRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.partnerprofile.PartnerReviews
import com.fatron.network_module.models.response.partnerprofile.PartnerReviewsResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentDocReviewsBinding
import com.homemedics.app.ui.activity.CallActivity
import com.homemedics.app.ui.adapter.DocReviewsAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.DoctorConsultationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DocReviewsFragment  : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentDocReviewsBinding

    private lateinit var doctorReviewsAdapter: DocReviewsAdapter

    private val doctorConsultationViewModel: DoctorConsultationViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title=langData?.globalString?.reviews.getSafe()
        }
    }

    override fun init() {
        observe()
        getPartnerReviews()

        setDataInViews()
    }

    override fun getFragmentLayout() = R.layout.fragment_doc_reviews

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentDocReviewsBinding
    }

    override fun setListeners() {

        mBinding.apply {
            actionbar.onAction1Click = {
                if (activity is CallActivity) {
                    (activity as CallActivity).removeFragment()
                } else {
                    findNavController().popBackStack()
                }
            }

            tvDesc.setOnClickListener {
                findNavController().safeNavigate(DocReviewsFragmentDirections.actionDocReviewsFragmentToAboutDoctorFragment())
            }
            tvQualification.setOnClickListener {
                findNavController().safeNavigate(DocReviewsFragmentDirections.actionDocReviewsFragmentToDocEducationFragment())
            }
        }

        if (activity is CallActivity) {
            setBackPressedListener()
        }
    }

    override fun onClick(v: View?) {

    }

    private fun observe() {

    }

    private fun setBackPressedListener() {
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (activity as CallActivity).removeFragment()
            }
        })
    }

    private fun setDataInViews() {
        mBinding.apply {
            partner = doctorConsultationViewModel.partnerProfileResponse
            iDoctor.vDivider.invisible()

            doctorReviewsAdapter = DocReviewsAdapter()
            rvItems.adapter = doctorReviewsAdapter

            mBinding.apply {
                val reviews = doctorConsultationViewModel.partnerProfileResponse.totalNoOfReviews
                iDoctor.apply {
                    tvAmount.setVisible(false)
                    tvReview.apply {
                        setVisible(reviews != null)
                        val noOfReview=if(reviews==0) reviews else String.format(
                            "%02d",
                            reviews
                        )
                        text = "${langData?.globalString?.reviews}($noOfReview)"
                    }
                    tvYearsExp.apply {
                        var yearExp=ApplicationClass.mGlobalData?.globalString?.yearExp?.replace("[0]",doctorConsultationViewModel.partnerProfileResponse.experience.getSafe())
                        if (doctorConsultationViewModel.partnerProfileResponse.experience?.isNotEmpty().getSafe()) {
                            try {
                                if(doctorConsultationViewModel.partnerProfileResponse.experience?.toFloat().getSafe()<1F)
                                    yearExp=ApplicationClass.mGlobalData?.globalString?.lessThanOneYearExp?.replace("[0]",doctorConsultationViewModel.partnerProfileResponse.experience.getSafe())
                                else if(doctorConsultationViewModel.partnerProfileResponse.experience?.toFloat().getSafe()>1F)
                                    yearExp=ApplicationClass.mGlobalData?.globalString?.yearsExp?.replace("[0]",doctorConsultationViewModel.partnerProfileResponse.experience.getSafe())
                            }
                            catch (e:Exception){
                                e.printStackTrace()
                                yearExp = ""
                            }

                        } else {
                            yearExp = ""
                        }
                        text = yearExp
                    }
                    lifecycleScope.launch {
                        delay(200) // avoid data binding
                        if (doctorConsultationViewModel.partnerProfileResponse.average_reviews_rating.getSafe() > 0.0) {
                            tvNoRating.gone()
                            ratingBar.apply {
                                visible()
                                rating = doctorConsultationViewModel.partnerProfileResponse.average_reviews_rating?.toFloat().getSafe()
                            }
                        } else {
                            ratingBar.gone()
                            tvNoRating.text =
                                ApplicationClass.mGlobalData?.globalString?.noRating.getSafe()
                            tvNoRating.visible()
                        }

                        val desc = doctorConsultationViewModel.partnerProfileResponse.overview
                        mBinding.tvDesc.setVisible(desc != null && desc.isNotEmpty())
                    }
                }
            }
        }
    }

    private fun getPartnerReviews() {
        val request = PartnerDetailsRequest(
            serviceId = doctorConsultationViewModel.bdcFilterRequest.serviceId,
            partnerUserId = doctorConsultationViewModel.partnerProfileResponse.partnerUserId
        )

        if (isOnline(requireActivity())) {
            doctorConsultationViewModel.getPartnerReviews(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<PartnerReviewsResponse>
                        response.data?.let {
                            if (it.reviews?.isNotEmpty().getSafe()) {
                                doctorReviewsAdapter.listItems = it.reviews?.map {
                                    it.serviceName = metaData?.partnerServiceType?.find { type -> type.id == it.serviceId }?.name.getSafe()
                                    it.ratingStars = "${it.rating} ${mBinding.langData?.globalString?.stars.getSafe()}"
                                    it
                                } as ArrayList<PartnerReviews>
                            } else {
                                mBinding.apply {
                                    rvItems.setVisible((doctorReviewsAdapter.listItems.isNullOrEmpty().not()))
                                    tvNoData.setVisible((doctorReviewsAdapter.listItems.isNullOrEmpty()))
                                }
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
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message =mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}