package com.homemedics.app.ui.bottomsheets

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.format.DateFormat
import androidx.fragment.app.activityViewModels
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.response.ordersdetails.OrderResponse
import com.fatron.network_module.utils.TinyDB
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseBottomSheetFragment
import com.homemedics.app.databinding.DialogRecordsCallBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.activity.CallActivity
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.CallViewModel
import com.homemedics.app.viewmodel.EMRViewModel
import java.text.SimpleDateFormat
import java.util.*

class PatientMedicalRecordsBottomSheet : BaseBottomSheetFragment() {

    val TAG = "PatientMedicalRecordsBottomSheet"

    private lateinit var mBinding: DialogRecordsCallBinding

    private val callViewModel: CallViewModel by activityViewModels()

    private val emrViewModel: EMRViewModel by activityViewModels()

    private var itemsAdapter = AddMultipleViewAdapter()

    override fun getTheme(): Int = R.style.base_bottom_sheet

    override fun setLanguageData() {
        mBinding.langData = ApplicationClass.mGlobalData
    }

    @SuppressLint("SetTextI18n")
    override fun init() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBinding.apply {
            dataManager = callViewModel.appointmentResponse

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

            actionbar.apply {
                title = "\u2066# $uniqueId \u2069 "
                actionbar.desc = callViewModel.appointmentResponse?.bookingStatus.getSafe().firstCap()
            }

            if (callViewModel.appointmentResponse?.patientDetails?.profilePicture != null) {
                iDoctorHeader.ivThumbnail.loadImage(callViewModel.appointmentResponse?.patientDetails?.profilePicture, getGenderIcon(callViewModel.appointmentResponse?.patientDetails?.genderId.toString()))
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
            currency = currencyData?.genericItemName.getSafe()
            iDoctorHeader.tvDetail.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${Constants.START}${callViewModel.appointmentResponse?.bookingDate}${Constants.END} ${Constants.PIPE} ${Constants.START}$formattedStartDate${Constants.END} ${mBinding.langData?.globalString?.to} ${Constants.START}$formattedEndDate${Constants.END} ${Constants.PIPE} $fee ${mBinding.currency}" else "${Constants.START}${callViewModel.appointmentResponse?.bookingDate}${Constants.END} ${Constants.PIPE} ${Constants.START}$formattedStartDate${Constants.END} ${mBinding.langData?.globalString?.to} ${Constants.START}$formattedEndDate${Constants.END} ${Constants.PIPE} ${mBinding.currency} $fee"

            val cal: Calendar = Calendar.getInstance(Locale.US)
            val format = SimpleDateFormat("yyyy-MM-dd")
            format.parse(callViewModel.appointmentResponse?.patientDetails?.dateOfBirth.getSafe())?.let { cal.setTime(it) }
            val today = Calendar.getInstance(Locale.US)

            val time: Long = (today.time.time / 1000) - (cal.time.time / 1000)

            val years: Long = Math.round(time.toDouble()) / 31536000
            val months: Long = Math.round(time.toDouble() - years * 31536000) / 2628000

            val monthsToShow =
                if(months > 0) ", $months ${mBinding.langData?.globalString?.monthhs}"
                else ""

            if(years.toInt() == 0 && months.toInt() == 0){
                mBinding.iDoctorHeader.tvDescription.text =
                    "\u2066${callViewModel.appointmentResponse?.patientDetails?.gender} | ${mBinding.langData?.globalString?.lessThanYear}\u2069"
            }
            else if (years > 0) {
                val year= if(years.toInt()>1) ApplicationClass.mGlobalData?.globalString?.years else ApplicationClass.mGlobalData?.globalString?.year

                mBinding.iDoctorHeader.tvDescription.text =
                    "\u2066${callViewModel.appointmentResponse?.patientDetails?.gender} | $years $year , $monthsToShow\u2069"
            } else {
                mBinding.iDoctorHeader.tvDescription.text =
                    "\u2066${callViewModel.appointmentResponse?.patientDetails?.gender} | $months ${
                        mBinding.langData?.globalString?.monthhs
                    }\u2069"
            }

            executePendingBindings()

            getMedicalRecords(callViewModel.orderDetailsResponse2)
        }
    }

    override fun getFragmentLayout() = R.layout.dialog_records_call

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBinding =  binding as DialogRecordsCallBinding
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
            itemsAdapter.onItemClick = { item, _ ->
                emrViewModel.apply {
                    emrID = item.itemId?.toInt().getSafe()
                    selectedEMRType = Enums.EMRType.values().find { it.key == item.extraInt }
                    isPatient = true
                }
                (requireActivity() as CallActivity).setRecordNavigation()
                dismiss()
            }
        }
    }

    private fun getMedicalRecords(data: OrderResponse?) {
        val list = arrayListOf<MultipleViewItem>()
        list.clear()
        data?.medicalRecord?.forEach { medicalRecords ->

            list.apply {
                add(
                    MultipleViewItem(
                        title = medicalRecords.emrName,
                        itemId = medicalRecords.emrId.toString(),
                        desc = "${Constants.START}${medicalRecords.date}${Constants.END} ${Constants.PIPE} ${mBinding.langData?.globalString?.recordNum} ${Constants.HASH} ${medicalRecords.emrNumber}",
                        drawable = R.drawable.ic_medical_rec,
                    ).apply {
                        itemEndIcon = R.drawable.ic_expand_more
                        extraInt = medicalRecords.emrType
                    }
                )
            }
        }
        mBinding.rvMedicalRecords.adapter = itemsAdapter
        itemsAdapter.listItems = list
        mBinding.tvNoData.setVisible(itemsAdapter.listItems.isEmpty())
    }
}