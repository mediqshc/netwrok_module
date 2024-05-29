package com.homemedics.app.ui.fragment.profile

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.family.FamilyResponse
import com.fatron.network_module.repository.ResponseResult
import com.google.android.material.tabs.TabLayoutMediator
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentFamilyProfileBinding
import com.homemedics.app.ui.adapter.FamilyProfileTabsPagerAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class FamilyProfileFragment : BaseFragment(), View.OnClickListener {
    private lateinit var mBinding: FragmentFamilyProfileBinding
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun setLanguageData() {
mBinding.langData=ApplicationClass.mGlobalData
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_family_profile

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentFamilyProfileBinding
    }

    override fun setListeners() {
        mBinding.apply {
            caAddNew.onAddItemClick = {
                findNavController().safeNavigate(PersonalProfileFragmentDirections.actionPersonalProfileFragmentToAddConnectionFragment())
            }
        }
    }

    override fun init() {

        mBinding.apply {
            viewPager.adapter = FamilyProfileTabsPagerAdapter(childFragmentManager, lifecycle)
            TabLayoutMediator(
                tabLayout, viewPager
            ) { tab, position ->
                tab.text = when (position) {
                    0 -> langData?.personalprofileBasicScreen?.connected
                    1 -> langData?.personalprofileBasicScreen?.received
                    2 -> langData?.personalprofileBasicScreen?.sent
                    else -> {
                        ""
                    }
                }
            }.attach()
        }
        if (PersonalProfileFragment.notiType == Enums.CallPNType.TYPE_FAMILY_MEMBER_ADDED.key) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(50)
                mBinding.viewPager.currentItem = 1
                PersonalProfileFragment.notiType=0
            }
        }

        getFamilyConnections()
        arguments?.clear()
    }

    override fun onClick(view: View?) {

    }

    fun getFamilyConnections() {
        if (isOnline(requireActivity())) {
            profileViewModel.getFamilyConnectionsApiCall().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<FamilyResponse>
                        val familyConnections = response.data
                        if (profileViewModel.moveFamilyTabsToIndex != -1) {
                            mBinding.viewPager.setCurrentItem(
                                profileViewModel.moveFamilyTabsToIndex,
                                true
                            )
                            profileViewModel.moveFamilyTabsToIndex = -1
                        }
                        profileViewModel.familyConnections.postValue(familyConnections)
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
                    title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message =mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}