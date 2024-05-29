package com.homemedics.app.ui.bottomsheets

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.format.DateFormat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.fatron.network_module.utils.TinyDB
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseBottomSheetFragment
import com.homemedics.app.databinding.DialogAppointmentDetailsCallBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.CallViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppointmentDetailsBottomSheet : BaseBottomSheetFragment() {

    val TAG = "AppointmentDetailsBottomSheet"

    private lateinit var mBinding: DialogAppointmentDetailsCallBinding

    private val callViewModel: CallViewModel by activityViewModels()

    override fun getTheme(): Int = R.style.base_bottom_sheet

    override fun setLanguageData() {
        mBinding.langData=ApplicationClass.mGlobalData
    }

    @SuppressLint("SetTextI18n")
    override fun init() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBinding.dataManager = callViewModel.appointmentResponse

        val roundedRadius = resources.getDimensionPixelSize(R.dimen.dp16)
        mBinding.actionbar.mBinding.clContainer.background = MaterialShapeDrawable(
            ShapeAppearanceModel.Builder()
                .setTopRightCorner(CornerFamily.ROUNDED, roundedRadius.toFloat())
                .setTopLeftCorner(CornerFamily.ROUNDED, roundedRadius.toFloat())
                .setTopLeftCorner(RoundedCornerTreatment())
                .setTopRightCorner(RoundedCornerTreatment())
                .build()
        ).apply { fillColor = ColorStateList.valueOf(resources.getColor(R.color.primary, requireContext().theme)) }

        val uniqueId = TinyDB.instance.getString("unique_id")

        mBinding.actionbar.title ="\u2066# $uniqueId \u2069"
        mBinding.actionbar.desc = callViewModel.appointmentResponse?.bookingStatus.getSafe().firstCap()

        lifecycleScope.launch {
            delay(200)

            if (callViewModel.appointmentResponse?.patientDetails?.profilePicture != null) {
                mBinding.iDoctorHeader.ivThumbnail.loadImage(callViewModel.appointmentResponse?.patientDetails?.profilePicture, getGenderIcon(callViewModel.appointmentResponse?.patientDetails?.genderId.toString()))
            }

            var formattedStartDate = ""
            var formattedEndDate = ""
            val currentFormat = getString(R.string.timeFormat24)
            val timeFormat =
                if (DateFormat.is24HourFormat(requireContext())) getString(R.string.timeFormat24) else getString(
                    R.string.timeFormat12
                )

            if(callViewModel.appointmentResponse?.startTime.isNullOrEmpty().not() && callViewModel.appointmentResponse?.endTime.isNullOrEmpty().not()){
                formattedStartDate = getDateInFormat(
                    StringBuilder(callViewModel.appointmentResponse?.startTime).insert(2, ":").toString(), currentFormat,
                    timeFormat
                )

                formattedEndDate = getDateInFormat(
                    StringBuilder(callViewModel.appointmentResponse?.endTime.getSafe()).insert(2, ":").toString(),
                    currentFormat,
                    timeFormat
                )
            }
            val locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
            val fee = if (callViewModel.appointmentResponse?.fee?.contains("Free", true).getSafe()) mBinding.langData?.globalString?.free else callViewModel.appointmentResponse?.fee
            val currencyData = metaData.currencies?.find { it.genericItemId == callViewModel.appointmentResponse?.currencyId }
            mBinding.currency = currencyData?.genericItemName.getSafe()
            mBinding.tvAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "$fee ${mBinding.currency.getSafe()}" else "${mBinding.currency.getSafe()} $fee"
            mBinding.tvServiceAmount.text = callViewModel.appointmentResponse?.serviceType
            mBinding.iDoctorHeader.tvDetail.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${Constants.START}${callViewModel.appointmentResponse?.bookingDate}${Constants.END} ${Constants.PIPE} ${Constants.START}$formattedStartDate${Constants.END} ${mBinding.langData?.globalString?.to} ${Constants.START}$formattedEndDate${Constants.END} ${Constants.PIPE} $fee ${mBinding.currency}" else "${Constants.START}${callViewModel.appointmentResponse?.bookingDate}${Constants.END} ${Constants.PIPE} ${Constants.START}$formattedStartDate${Constants.END} ${mBinding.langData?.globalString?.to} ${Constants.START}$formattedEndDate${Constants.END} ${Constants.PIPE} ${mBinding.currency} $fee"

            mBinding.tvSpecialInstructionDesc.text = if (callViewModel.appointmentResponse?.instructions.isNullOrEmpty().not())
                callViewModel.appointmentResponse?.instructions
            else
                 mBinding.langData?.globalString?.noInstructions

            val cal: Calendar = Calendar.getInstance(Locale.US)
            val format = SimpleDateFormat("yyyy-MM-dd")
            format.parse(callViewModel.appointmentResponse?.patientDetails?.dateOfBirth.getSafe())?.let { cal.setTime(it) }
            val today = Calendar.getInstance(Locale.US)

            val time: Long = (today.time.time / 1000) - (cal.time.time / 1000)

            val years: Long = Math.round(time.toDouble()) / 31536000
            val months: Long = Math.round(time.toDouble() - years * 31536000) / 2628000

            val monthsToShow =
                if(months > 0) ", $months ${ mBinding.langData?.globalString?.monthhs}"
                else ""

            if(years.toInt() == 0 && months.toInt() == 0){
                mBinding.iDoctorHeader.tvDescription.text =
                    "\u2066${callViewModel.appointmentResponse?.patientDetails?.gender} | ${ mBinding.langData?.globalString?.lessThanYear}\u2069"
            }
            else if (years > 0) {
                var year= mBinding.langData?.globalString?.year
                if(  years.toInt()>1)
                    year= mBinding.langData?.globalString?.years
                mBinding.iDoctorHeader.tvDescription.text =
                    "\u2066${callViewModel.appointmentResponse?.patientDetails?.gender} \u2069\u2069| $years ${
                        year
                    }, $monthsToShow\u2066"
            } else {
                mBinding.iDoctorHeader.tvDescription.text =
                    "\u2066${callViewModel.appointmentResponse?.patientDetails?.gender} \u2069\u2069| $months ${
                        mBinding.langData?.globalString?.monthhs
                    }\u2066"
            }

            mBinding.executePendingBindings()
        }
    }

    override fun getFragmentLayout() = R.layout.dialog_appointment_details_call

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBinding = binding as DialogAppointmentDetailsCallBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                setOnClickListener {
                    dismiss()
                }
                onAction1Click = {
                    dismiss()
                }
                onAction2Click = {
                    dismiss()
                }
            }
        }
    }
}