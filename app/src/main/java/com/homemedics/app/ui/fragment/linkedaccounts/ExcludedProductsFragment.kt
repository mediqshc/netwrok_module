package com.homemedics.app.ui.fragment.linkedaccounts

import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.fatron.network_module.models.response.meta.GenericItem
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentExcludedProductsBinding
import com.homemedics.app.ui.adapter.ExcludedProductAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.setVisible
import com.homemedics.app.viewmodel.ProfileViewModel


class ExcludedProductsFragment : BaseFragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var mBinding: FragmentExcludedProductsBinding
    private lateinit var excludedProductAdapter: ExcludedProductAdapter

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            etSearch.hint = lang?.globalString?.search.getSafe()
        }
    }

    override fun init() {
        val excludedItems: ArrayList<GenericItem> = arrayListOf()

        if (
            CustomServiceTypeView.ServiceType.VideoCall.id == profileViewModel.companyServices?.partnerServiceId ||
            CustomServiceTypeView.ServiceType.Message.id == profileViewModel.companyServices?.partnerServiceId ||
            CustomServiceTypeView.ServiceType.HomeVisit.id == profileViewModel.companyServices?.partnerServiceId ||
            CustomServiceTypeView.ServiceType.Clinic.id == profileViewModel.companyServices?.partnerServiceId ||
            CustomServiceTypeView.ServiceType.VideoCall.id == profileViewModel.companyDiscount?.partnerServiceId ||
            CustomServiceTypeView.ServiceType.Message.id == profileViewModel.companyDiscount?.partnerServiceId ||
            CustomServiceTypeView.ServiceType.HomeVisit.id == profileViewModel.companyDiscount?.partnerServiceId
        ) {
            val doctors = profileViewModel.doctors.getSafe() as List<Map<String, Any>>
            var currentDoctor = ""
            for (item in doctors) {
                val type = item["type"] as String
                val value = item["value"]

                if (type == "doctor") {
                   value.let {value ->
                       currentDoctor = value.toString() as String
                   }
                } else if (type == "speciality") {
                    val specialities = ((value as List<String>).getSafe()).joinToString(", ", postfix = ".")
                    excludedItems.add(
                        GenericItem(genericItemName = currentDoctor, description = "$specialities ")
                    )
                }
            }
        } else {
            profileViewModel.excludedItems.forEach { items ->
                excludedItems.add(
                    GenericItem(genericItemName = items)
                )
            }
        }
        excludedProductAdapter = ExcludedProductAdapter().apply {
            listItems = excludedItems
            originalList = excludedItems
        }
        mBinding.rvProducts.adapter = excludedProductAdapter
        mBinding.tvNoData.setVisible(excludedProductAdapter.listItems.isEmpty())
        mBinding.etSearch.addTextChangedListener {
            excludedProductAdapter.filter.filter(it)
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_excluded_products

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentExcludedProductsBinding
    }

    override fun setListeners() {

    }
}