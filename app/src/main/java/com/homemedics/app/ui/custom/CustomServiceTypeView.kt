package com.homemedics.app.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.databinding.ViewConsultationTypeCheckableBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.utils.getAppContext
import com.homemedics.app.utils.getSafe

class CustomServiceTypeView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val mBinding: ViewConsultationTypeCheckableBinding =
        ViewConsultationTypeCheckableBinding.inflate(LayoutInflater.from(context), this, true)

    var onItemSelected: ((item: ServiceType)->Unit)? = null
        set(value) {
            field = value
            //setting default selection

            if(selectedServiceType == null)
                selectedServiceType = getDefaultService()

            selectedServiceType?.let {
                value?.invoke(it)
                setServiceType(it.id.getSafe())
            }
        }
    var selectedServiceType: ServiceType? = null
        private set

    init {
        val styledAttributes =
            context.obtainStyledAttributes(attrs, R.styleable.CustomServiceTypeView)

        setListeners()

        mBinding.apply {
            langData=ApplicationClass.mGlobalData
            //temporary disabling clinic
            cclClinic.isEnabled = false
            cclClinic.isClickable = false
if(TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)==DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_UR)
    cclVideoCall.background=ResourcesCompat.getDrawable(resources,R.drawable.selector_rounded_right_primary_transparent ,null)
   else
       cclVideoCall.background=ResourcesCompat.getDrawable(resources,R.drawable.selector_rounded_left_primary_transparent,null)
            (cclClinic.getChildAt(0) as TextView).apply {
                setTextColor(resources.getColor(R.color.primary20, context.theme))
            }
        }

    }

    private fun getDefaultService(): ServiceType? {
        mBinding.apply {
            return if(cclVideoCall.isEnabled)
                ServiceType.VideoCall
            else if(cclMessage.isEnabled)
                ServiceType.Message
            else if (cclHome.isEnabled)
                ServiceType.HomeVisit
            else if(cclClinic.isEnabled)
                ServiceType.Clinic
            else return null
        }
    }

    fun setListeners() {

        mBinding.apply {
            cclVideoCall.setOnClickListener { // 1
                onItemSelected?.invoke(ServiceType.VideoCall)
                selectedServiceType = ServiceType.VideoCall
                setCheckedConsultationType(it)
            }
            cclHome.setOnClickListener { //2
                onItemSelected?.invoke(ServiceType.HomeVisit)
                selectedServiceType = ServiceType.HomeVisit
                setCheckedConsultationType(it)
            }
            cclMessage.setOnClickListener { //3
                onItemSelected?.invoke(ServiceType.Message)
                selectedServiceType = ServiceType.Message
                setCheckedConsultationType(it)
            }
            cclClinic.setOnClickListener { //4
                onItemSelected?.invoke(ServiceType.Clinic)
                selectedServiceType = ServiceType.Clinic
                setCheckedConsultationType(it)
            }
        }
    }

    private fun setCheckedConsultationType(view: View){
        mBinding.apply {
            llCheckableConsultationType.children.forEach {
                if(it is CustomCheckableLinearLayout){
                    it.isChecked = it.id == view.id
                }
            }
        }
    }

    fun setServiceType(id: Int){
        mBinding.apply {
            when (id){
                ServiceType.VideoCall.id -> {
                    cclVideoCall.isChecked = true
                    selectedServiceType = ServiceType.VideoCall
                    onItemSelected?.invoke(ServiceType.VideoCall)
                }
                ServiceType.HomeVisit.id -> {
                    cclHome.isChecked = true
                    selectedServiceType = ServiceType.HomeVisit
                    onItemSelected?.invoke(ServiceType.HomeVisit)
                }
                ServiceType.Message.id -> {
                    cclMessage.isChecked = true
                    selectedServiceType = ServiceType.Message
                    onItemSelected?.invoke(ServiceType.Message)
                }
                ServiceType.Clinic.id -> {
                    cclClinic.isChecked = true
                    selectedServiceType = ServiceType.Clinic
                    onItemSelected?.invoke(ServiceType.Clinic)
                }
            }
        }
    }

    fun setDisabledServices(serviceIds: ArrayList<Int>?){
        mBinding.apply {
            serviceIds?.forEach { id ->
                when (id){
                    ServiceType.VideoCall.id -> {
                        cclVideoCall.isEnabled = false
                        cclVideoCall.isClickable = false
                        (cclVideoCall.getChildAt(0) as TextView).apply {
                            setTextColor(resources.getColor(R.color.primary20, context.theme))
                        }
                    }
                    ServiceType.HomeVisit.id -> {
                        cclHome.isEnabled = false
                        cclHome.isClickable = false
                        (cclHome.getChildAt(0) as TextView).apply {
                            setTextColor(resources.getColor(R.color.primary20, context.theme))
                        }
                    }
                    ServiceType.Message.id -> {
                        cclMessage.isEnabled = false
                        cclMessage.isClickable = false
                        (cclMessage.getChildAt(0) as TextView).apply {
                            setTextColor(resources.getColor(R.color.primary20, context.theme))
                        }
                    }
                    ServiceType.Clinic.id -> {
                        cclClinic.isEnabled = false
                        cclClinic.isClickable = false
                        (cclClinic.getChildAt(0) as TextView).apply {
                            setTextColor(resources.getColor(R.color.primary20, context.theme))
                        }
                    }
                }
            }
        }
    }


    enum class ServiceType(val id: Int, val value: String, val icon: Int){
        VideoCall(1, ApplicationClass.mGlobalData?.globalString?.videoCalling.getSafe(), R.drawable.ic_call_black),
        HomeVisit(2, ApplicationClass.mGlobalData?.tabString?.home.getSafe(), R.drawable.ic_home_black),
        Message(3, ApplicationClass.mGlobalData?.globalString?.Messages.getSafe(), R.drawable.ic_chat_black),
        Clinic(4, ApplicationClass.mGlobalData?.globalString?.clinic.getSafe(), R.drawable.ic_company),
        HealthCare(5, ApplicationClass.mGlobalData?.bookingScreen?.homeService.getSafe(), R.drawable.ic_home_black),
        LaboratoryService(6, getAppContext().getString(R.string.laboratory_service), R.drawable.ic_lab_test),
        PharmacyService(7, getAppContext().getString(R.string.pharmacy_service), R.drawable.ic_local_pharmacy),
        Claim(8, ApplicationClass.mGlobalData?.claimScreen?.claim.getSafe(), R.drawable.ic_healthcare_black),
        OtherWalkIn(9, ApplicationClass.mGlobalData?.walkInScreens?.walkInPharmacy.getSafe(), R.drawable.ic_local_pharmacy),
        WalkInPharmacy(10, ApplicationClass.mGlobalData?.walkInScreens?.walkInPharmacy.getSafe(), R.drawable.ic_local_pharmacy),
        WalkInLaboratory(11, ApplicationClass.mGlobalData?.walkInScreens?.walkinLaboratory .getSafe(), R.drawable.ic_lab_test),
        WalkInHospital(12, ApplicationClass.mGlobalData?.walkInScreens?.walkInHospital .getSafe(), R.drawable.ic_apartment);

        companion object {
            fun getServiceById(id: Int): ServiceType?{
                return values().find { it.id == id }
            }
        }
    }
}