package com.homemedics.app.ui.fragment.linkedaccounts

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.fatron.network_module.models.response.meta.GenericItem
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentCategoriesBinding
import com.homemedics.app.ui.adapter.HomeServiceAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.setVisible
import com.homemedics.app.viewmodel.ProfileViewModel

class CategoriesFragment : BaseFragment(), View.OnClickListener {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var mBinding: FragmentCategoriesBinding
    private lateinit var categoriesAdapter: HomeServiceAdapter

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            etSearch.hint = lang?.globalString?.search.getSafe()
        }
    }

    override fun init() {
        val excludedCategories: ArrayList<GenericItem> = arrayListOf()
        if (
            CustomServiceTypeView.ServiceType.VideoCall.id == profileViewModel.companyServices?.partnerServiceId ||
            CustomServiceTypeView.ServiceType.Message.id == profileViewModel.companyServices?.partnerServiceId ||
            CustomServiceTypeView.ServiceType.HomeVisit.id == profileViewModel.companyServices?.partnerServiceId ||
            CustomServiceTypeView.ServiceType.Clinic.id == profileViewModel.companyServices?.partnerServiceId ||
            CustomServiceTypeView.ServiceType.VideoCall.id == profileViewModel.companyDiscount?.partnerServiceId ||
            CustomServiceTypeView.ServiceType.Message.id == profileViewModel.companyDiscount?.partnerServiceId ||
            CustomServiceTypeView.ServiceType.HomeVisit.id == profileViewModel.companyDiscount?.partnerServiceId
        ) {
            profileViewModel.specialitiesExclusion.forEach { categories ->
                excludedCategories.add(
                    GenericItem(genericItemName = categories)
                )
            }
        } else {
            profileViewModel.excludedCategories.forEach { categories ->
                excludedCategories.add(
                    GenericItem(genericItemName = categories)
                )
            }
        }
        categoriesAdapter = HomeServiceAdapter().apply {
            invisibleArrow = true
            listItems = excludedCategories
            originalList = excludedCategories
        }
        mBinding.rvCategory.adapter = categoriesAdapter
        mBinding.tvNoData.setVisible(categoriesAdapter.listItems.isEmpty())
        mBinding.etSearch.addTextChangedListener {
            categoriesAdapter.filter.filter(it)
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_categories

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding =  binding as FragmentCategoriesBinding
    }

    override fun setListeners() {

    }

    override fun onClick(v: View?) {

    }
}