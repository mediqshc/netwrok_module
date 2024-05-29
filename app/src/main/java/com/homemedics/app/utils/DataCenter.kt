package com.homemedics.app.utils

import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.meta.MetaDataResponse
import com.fatron.network_module.models.response.user.UserResponse
import com.fatron.network_module.models.response.walkinpharmacy.ServiceTypes
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.R
import com.homemedics.app.firebase.GenericString
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.santalu.maskara.Mask
import com.santalu.maskara.MaskChangedListener
import com.santalu.maskara.MaskStyle
import java.text.SimpleDateFormat
import java.util.*

object DataCenter {
    val otpMask = Mask(value = getString(R.string.otp_format), character = '_', MaskStyle.NORMAL)
    val otpMaskListener = MaskChangedListener(otpMask)
    val cnicMask =
        Mask(value = getString(R.string.pk_cnic_format), character = '_', MaskStyle.NORMAL)
    val cnicMaskListener = MaskChangedListener(cnicMask)
    fun getUser(): UserResponse? {
        return TinyDB.instance.getObject(
            Enums.TinyDBKeys.USER.key,
            UserResponse::class.java
        ) as UserResponse?
    }

    fun getMeta(): MetaDataResponse? {
        return TinyDB.instance.getObject(
            Enums.TinyDBKeys.META.key,
            MetaDataResponse::class.java
        ) as MetaDataResponse?
    }

    fun getEMRTypeList(lang: RemoteConfigLanguage?): ArrayList<GenericItem> {
        return arrayListOf(
            GenericItem(
                genericItemId = com.homemedics.app.utils.Enums.EMRType.CONSULTATION.key,
                genericItemName = lang?.emrScreens?.consultaion.getSafe(),
            ).apply { drawable = R.drawable.ic_consultation },
            GenericItem(
                genericItemId = com.homemedics.app.utils.Enums.EMRType.REPORTS.key,
                genericItemName = lang?.emrScreens?.reports.getSafe(),
            ).apply { drawable = R.drawable.ic_document },
            GenericItem(
                genericItemId = com.homemedics.app.utils.Enums.EMRType.MEDICATION.key,
                genericItemName = lang?.emrScreens?.medications.getSafe(),
            ).apply { drawable = R.drawable.ic_medication },
            GenericItem(
                genericItemId = com.homemedics.app.utils.Enums.EMRType.VITALS.key,
                genericItemName = lang?.emrScreens?.vitals.getSafe(),
            ).apply { drawable = R.drawable.ic_bloodtype },
        )
    }

    fun getDoctorSpecialities(): ArrayList<GenericItem> {
        return getMeta()?.specialties?.doctorSpecialties as ArrayList<GenericItem>
    }

    fun getMedicalStaffSpecialities(): ArrayList<GenericItem> {
        return getMeta()?.specialties?.medicalStaffSpecialties as ArrayList<GenericItem>
    }

    fun getDates(range: Int): ArrayList<GenericItem> {
        val cal = Calendar.getInstance()
        val list = arrayListOf<GenericItem>()
        for (i in 0 until range) {
            if (i > 0)
                cal.add(Calendar.DAY_OF_MONTH, 1)
            val formattedDay = getFormattedDay(cal)
            val splitted = formattedDay.split("-")

            val date = splitted[0]
            val day = splitted[1]

            list.add(
                GenericItem(
                    genericItemName = day,
                    description = date,
                    isChecked = false
                )
            )
        }
        return list
    }

    fun getFormattedDay(cal: Calendar): String {
        val day: Date = cal.time as Date
        val sdf = SimpleDateFormat("dd-EEE")
        return sdf.format(day)
    }

    fun getHomeSliderImages(): ArrayList<String> {
        return arrayListOf(
            "https://mediq-bucket.s3.amazonaws.com/mediq/Certified+Doctors.png",
            "https://mediq-bucket.s3.amazonaws.com/mediq/Lab-Sample.png",
            "https://mediq-bucket.s3.amazonaws.com/mediq/Medicines-delivery.png"


        )
    }

    fun getPharmacyServicesList(lang: GenericString?, serviceTypes: ServiceTypes): ArrayList<GenericItem> {
        val pharmaList = arrayListOf<GenericItem>().apply {
            if (serviceTypes.online?.getBoolean().getSafe())
                add(
                    GenericItem(
                        genericItemId = 0,
                        genericItemName = lang?.onlineBooking.getSafe(),
                        description = lang?.pharmacyOnlineDesc.getSafe()
                    ).apply {
                        drawable = R.drawable.ic_online
                    }
                )

            if (serviceTypes.walkInCashless?.getBoolean().getSafe())
                add(
                    GenericItem(
                        genericItemId = 1,
                        genericItemName = lang?.walkInCashless.getSafe(),
                        description = lang?.pharmacyWalkInDesc.getSafe()
                    ).apply {
                        drawable = R.drawable.ic_cashless
                    }
                )

            if (serviceTypes.walkInDiscountCenter?.getBoolean().getSafe())
                add(
                    GenericItem(
                        genericItemId = 1,
                        genericItemName = lang?.walkInDiscount.getSafe(),
                        description = lang?.pharma_walk_in_discount_desc.getSafe()
                    ).apply {
                        drawable = R.drawable.ic_discount
                    }
                )
        }
        return pharmaList
    }

    fun getLabServicesList(lang: GenericString?, serviceTypes: ServiceTypes): ArrayList<GenericItem> {
        val labList = arrayListOf<GenericItem>().apply {
            if (serviceTypes.online?.getBoolean().getSafe())
                add(
                    GenericItem(
                        genericItemId = 0,
                        genericItemName = lang?.onlineBooking.getSafe(),
                        description = lang?.labOnlineDesc.getSafe()
                    ).apply {
                        drawable = R.drawable.ic_online
                    }
                )
            if (serviceTypes.walkInCashless?.getBoolean().getSafe())
                add(
                    GenericItem(
                        genericItemId = 1,
                        genericItemName = lang?.walkInCashless.getSafe(),
                        description = lang?.labWalkInDesc.getSafe()
                    ).apply {
                        drawable = R.drawable.ic_cashless
                    }
                )
            if (serviceTypes.walkInDiscountCenter?.getBoolean().getSafe())
                add(
                    GenericItem(
                        genericItemId = 1,
                        genericItemName = lang?.walkInDiscount.getSafe(),
                        description = lang?.lab_walk_in_discount_desc.getSafe()
                    ).apply {
                        drawable = R.drawable.ic_discount
                    }
                )
        }

        return labList
    }

    fun getHomeAndHospitalServicesList(lang: GenericString?, serviceTypes: ServiceTypes): ArrayList<GenericItem> {
        val hospitalList = arrayListOf<GenericItem>().apply {
            if (serviceTypes.online?.getBoolean().getSafe())
                add(
                    GenericItem(
                        genericItemId = 0,
                        genericItemName = lang?.homeHealthCare.getSafe(),
                        description = lang?.walk_in_online_desc.getSafe()
                    ).apply {
                        drawable = R.drawable.ic_online
                    }
                )

            if (serviceTypes.walkInCashless?.getBoolean().getSafe())
                add(
                    GenericItem(
                        genericItemId = 1,
                        genericItemName = lang?.walkinHospital.getSafe(),
                        description = lang?.walk_in_services_desc.getSafe()
                    ).apply {
                        drawable = R.drawable.ic_cashless
                    }
                )

            if (serviceTypes.walkInDiscountCenter?.getBoolean().getSafe())
                add(
                    GenericItem(
                        genericItemId = 1,
                        genericItemName = lang?.walkInDiscount.getSafe(),
                        description = lang?.hospital_walk_in_discount_desc.getSafe()
                    ).apply {
                        drawable = R.drawable.ic_discount
                    }
                )
        }

        return hospitalList
    }
}