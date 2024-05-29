package com.homemedics.app.ui.fragment.becomepartner

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.partnerprofile.SpecialitiesRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentSpecialitiesBinding
import com.homemedics.app.ui.adapter.AddSpecialitiesAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ProfileViewModel

class SpecialitiesFragment : BaseFragment(), View.OnClickListener {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var mBinding: FragmentSpecialitiesBinding
    private lateinit var specialitiesAdapter: AddSpecialitiesAdapter

    var listItems: ArrayList<GenericItem> = ArrayList()
        set(value) {
            val diffUtilsCallback = AddSpecialitiesDiffUtilsCallback(field, value)
            val diffResult = DiffUtil.calculateDiff(diffUtilsCallback)
            field.clear()
            field.addAll(value)
            mBinding.rvSpecialities.adapter?.let { diffResult.dispatchUpdatesTo(it) }
        }

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title = langData?.globalString?.selectSpeciality.getSafe()
            etSearch.hint = langData?.globalString?.search.getSafe()
        }
    }

    override fun init() {
        profileViewModel. isFromMenu=0
        setupRecyclerview()

    }

    override fun getFragmentLayout() = R.layout.fragment_specialities

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSpecialitiesBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            etSearch.addTextChangedListener {
                specialitiesAdapter.filter.filter(it)
            }
            bSave.setOnClickListener {
                profileViewModel.selectedSpecialties = specialitiesAdapter.originalList.filter { it.isSelected.getSafe() } as ArrayList<MultipleViewItem>
                profileViewModel.selectedSpecialitiesInt = profileViewModel.selectedSpecialties.map { it.itemId?.toInt().getSafe() }
                if (profileViewModel.selectedSpecialties.isNotEmpty()) {
                    val request = SpecialitiesRequest(
                        profileViewModel.selectedSpecialitiesInt
                    )
                    storeSpecialities(request)
                }
            }
        }
    }

    override fun onClick(v: View?) {

    }

    override fun onDetach() {
        super.onDetach()
        profileViewModel.selectedSpecialties = specialitiesAdapter.listItems.filter { it.isSelected.getSafe() } as ArrayList<MultipleViewItem>

    }

    private fun setupRecyclerview() {
        listItems = profileViewModel.specialties
        specialitiesAdapter = AddSpecialitiesAdapter()
        specialitiesAdapter.listItems = listItems
        specialitiesAdapter.originalList =  profileViewModel.specialties
        mBinding.rvSpecialities.adapter = specialitiesAdapter
        specialitiesAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkEmpty()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                checkEmpty()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                checkEmpty()
            }

            fun checkEmpty() {
               mBinding.tvNoData.setVisible((specialitiesAdapter.itemCount == 0))
               mBinding.rvSpecialities.setVisible((specialitiesAdapter.itemCount != 0))
            }
        })
    }

    private fun storeSpecialities(specialitiesRequest: SpecialitiesRequest) {
        profileViewModel.storeSpecialities(specialitiesRequest).observe(this) {
            if (isOnline(activity)) {
                when (it) {
                    is ResponseResult.Success -> {
                        hideLoader()
                        try {
                            val response = it.data as ResponseGeneral<*>
                            showToast(getErrorMessage(response.message.toString()))
                            findNavController().popBackStack()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    is ResponseResult.Pending -> {
                        showLoader()
                    }
                    is ResponseResult.Failure -> {
                        hideLoader()
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {
                        hideLoader()
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.Complete -> {
                        hideLoader()
                    }
                    else -> {
                        hideLoader()
                    }
                }
            } else {
                DialogUtils(requireActivity())
                    .showSingleButtonAlertDialog(
                        title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                        message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                        buttonCallback = {},
                    )
            }
        }
    }
}