package com.homemedics.app.ui.bottomsheets

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.fragment.app.activityViewModels
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseBottomSheetFragment
import com.homemedics.app.databinding.BottomsheetOptionsBinding
import com.homemedics.app.ui.activity.CallActivity
import com.homemedics.app.utils.DataCenter
import com.homemedics.app.utils.isCustomer
import com.homemedics.app.viewmodel.CallViewModel

class CallDetailsBottomSheet : BaseBottomSheetFragment(), View.OnClickListener {

    val TAG = "CallDetailsBottomSheet"

    private lateinit var mBinding: BottomsheetOptionsBinding

    private val callViewModel: CallViewModel by activityViewModels()

    override fun getTheme(): Int = R.style.base_bottom_sheet_bluish

    override fun setLanguageData() {
        mBinding.langData=ApplicationClass.mGlobalData

    }

    override fun init() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun getFragmentLayout() = R.layout.bottomsheet_options

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as BottomsheetOptionsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            tvViewAttachments.setOnClickListener(this@CallDetailsBottomSheet)
            tvViewAppointmentDetails.setOnClickListener(this@CallDetailsBottomSheet)
            tvViewRecords.setOnClickListener(this@CallDetailsBottomSheet)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.tvViewAttachments -> {
                if (DataCenter.getUser().isCustomer() || callViewModel.fromPush) {
                    (requireActivity() as CallActivity).showDoctorAttachment()
                    dialog?.dismiss()
                } else {
                    (requireActivity() as CallActivity).showPatientAttachment()
                    dialog?.dismiss()
                }
            }
            R.id.tvViewAppointmentDetails -> {
                if (DataCenter.getUser().isCustomer() || callViewModel.fromPush) {
                    (requireActivity() as CallActivity).showDoctorDetails()
                    dialog?.dismiss()
                } else {
                    (requireActivity() as CallActivity).showAppointmentDetail()
                    dialog?.dismiss()
                }
            }
            R.id.tvViewRecords -> {
                try {
                    if (DataCenter.getUser().isCustomer() || callViewModel.fromPush)
                        (requireActivity() as CallActivity).showDoctorMedicalRecords()
                    else
                        (requireActivity() as CallActivity).showPatientRecords()
                }
                catch (e:Exception){e.printStackTrace()}

                dismiss()
            }
        }
    }
}