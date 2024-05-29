package com.homemedics.app.ui.fragment.doctorconsultation

import android.graphics.Paint
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.response.partnerprofile.EducationResponse
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentDocEducationBinding
import com.homemedics.app.ui.activity.CallActivity
import com.homemedics.app.ui.adapter.DocEducationAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.DoctorConsultationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DocEducationFragment  : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentDocEducationBinding

    private lateinit var doctorEducationAdapter: DocEducationAdapter

    private val doctorConsultationViewModel: DoctorConsultationViewModel by activityViewModels()
    private val langData=ApplicationClass.mGlobalData
    override fun setLanguageData() {
        mBinding.apply {
            tvNoData.text=langData?.bookingScreen?.noEducationFound
            actionbar.title=langData?.partnerProfileScreen?.education.getSafe()
        }
    }

    override fun init() {
        observe()
//        getPartnerReviews()

        setDataInViews()
    }

    override fun getFragmentLayout() = R.layout.fragment_doc_education

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentDocEducationBinding
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

    private fun setDataInViews() {
        mBinding.apply {
            partner = doctorConsultationViewModel.partnerProfileResponse
            iDoctor.vDivider.invisible()

            doctorEducationAdapter = DocEducationAdapter()
            rvItems.adapter = doctorEducationAdapter
            doctorEducationAdapter.listItems = (doctorConsultationViewModel.partnerProfileResponse.educations?.map {
                it.drawable = R.drawable.ic_school
                it.desc = "${it.degree} | ${metaData?.allCountries?.find { item -> item.id == it.countryId }?.shortName} | ${it.year}"
                it
            } as ArrayList<EducationResponse>)

            mBinding.apply {
                rvItems.setVisible((doctorEducationAdapter.listItems.isNullOrEmpty().not()))
                tvNoData.setVisible((doctorEducationAdapter.listItems.isNullOrEmpty()))
            }

            val reviews = partner?.totalNoOfReviews

            mBinding.iDoctor.apply {
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
                }
            }
        }
    }
}