package com.homemedics.app.ui.fragment.doctorconsultation

import android.graphics.Paint
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.bdc.PartnerDetailsRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAboutDocBinding
import com.homemedics.app.ui.activity.CallActivity
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.DoctorConsultationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AboutDoctorFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentAboutDocBinding

    private val doctorConsultationViewModel: DoctorConsultationViewModel by activityViewModels()
    val langData = ApplicationClass.mGlobalData
    override fun setLanguageData() {
        mBinding.actionbar.title = langData?.globalString?.aboutDoctor.getSafe()

    }

    override fun init() {
        observe()
        getPartnerAboutApi()

        mBinding.iDoctor.vDivider.invisible()
    }

    override fun getFragmentLayout() = R.layout.fragment_about_doc

    override fun getViewModel() {

    }

    private fun getPartnerAboutApi() {
        if (isOnline(requireActivity())) {
            doctorConsultationViewModel.getPartnerAbout(
                PartnerDetailsRequest(
                    serviceId = doctorConsultationViewModel.bdcFilterRequest.serviceId,
                    partnerUserId = doctorConsultationViewModel.partnerProfileResponse.partnerUserId
                )
            ).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        val data = response.data as PartnerProfileResponse
                        mBinding.apply {
                            partner = response.data as PartnerProfileResponse
                            val reviews = partner?.totalNoOfReviews

                            iDoctor.apply {
                                tvAmount.setVisible(false)
                                tvReview.apply {
                                    setVisible(reviews != null)
                                    val noOfReview=if(reviews==0) reviews else String.format(
                                        "%02d",
                                        reviews
                                    )
                                    text = "${langData?.globalString?.reviews}($noOfReview)"
                                    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                                }
                                tvYearsExp.apply {
                                    var yearExp=ApplicationClass.mGlobalData?.globalString?.yearExp?.replace("[0]",data.experience.getSafe())
                                    if (data.experience?.isNotEmpty().getSafe()) {
                                        try {
                                            if(data.experience?.toFloat().getSafe()<1F)
                                                yearExp=ApplicationClass.mGlobalData?.globalString?.lessThanOneYearExp?.replace("[0]",data.experience.getSafe())
                                            else if(data.experience?.toFloat().getSafe()>1F)
                                                yearExp=ApplicationClass.mGlobalData?.globalString?.yearsExp?.replace("[0]",data.experience.getSafe())
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
                                    if (partner?.average_reviews_rating.getSafe() > 0.0) {
                                        tvNoRating.gone()
                                        ratingBar.apply {
                                            visible()
                                            rating =
                                                partner?.average_reviews_rating?.toFloat().getSafe()
                                        }
                                    } else {
                                        ratingBar.gone()
                                        tvNoRating.text =
                                            ApplicationClass.mGlobalData?.globalString?.noRating.getSafe()
                                        tvNoRating.visible()
                                    }
                                }
                            }
                        }

                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
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
                    title = langData?.errorMessages?.internetError.getSafe(),
                    message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAboutDocBinding
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
        }
    }


    override fun onClick(v: View?) {

    }


    private fun observe() {

    }

}