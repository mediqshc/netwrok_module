package com.homemedics.app.ui.fragment.packages

import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.auth.LoginRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentPackagesBinding
import com.homemedics.app.ui.adapter.PackagesAdapter
import  com.fatron.network_module.models.response.packages.Package
import com.fatron.network_module.models.response.user.UserResponse
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.AuthViewModel

class PackagesFragment : BaseFragment() {

    companion object {
        fun newInstance() = PackagesFragment()
    }


    private lateinit var mBinding: FragmentPackagesBinding
    private val packagesViewModel: PackagesViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            AuthViewModel.ViewModelFactory()
        )[AuthViewModel::class.java]
    }

    private val packagesAdapter = PackagesAdapter()
    private val langData = ApplicationClass.mGlobalData
    var isFromHome = false
    private val loginRequest = LoginRequest()

    override fun getFragmentLayout() = R.layout.fragment_packages

    override fun getViewBinding() {
        mBinding = binding as FragmentPackagesBinding
    }


    override fun setLanguageData() {
        mBinding.actionbar
        mBinding.actionbar.title = "Packages & Promotions"
    }

    override fun init() {
        mBinding.rvPackages.adapter = packagesAdapter
        val network = arguments?.getString("network").getSafe()
        val phone = arguments?.getString("phone").getSafe()
        isFromHome = arguments?.getString("navigateFrom").getSafe() == "Home"

        //set values to be used later on details screen
        packagesViewModel.phone.value = phone
        packagesViewModel.network.value = network
        if (isFromHome) {
            verifyPhoneNumApiCall()
        } else {
            getListApi(network, phone)
        }

    }

    override fun getViewModel() {
    }

    override fun setListeners() {
        mBinding.actionbar.onAction1Click = {
            packagesViewModel.isPackagesScreenCancelled.value = true
            findNavController().popBackStack()
        }

        packagesAdapter.packageClick = { item ->
            packagesViewModel.selectedPackage.value = item
            if (isFromHome){
                findNavController().safeNavigate(
                    R.id.action_packagesFragment_to_packageDetailsFragment2,bundleOf(
                        "isFromHome" to isFromHome
                    )
                )
            }else{
                findNavController().safeNavigate(
                    R.id.action_packagesFragment_to_packageDetailsFragment
                )
            }

        }
    }


    private fun getListApi(network: String, phone: String) {
        if (isOnline(requireActivity())) {
            packagesViewModel.getPackagesApiCall(network, phone).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<List<Package>>
                        val list = (response.data as ArrayList<Package>?)
                        list?.let { listItems ->
                            packagesAdapter.listItems = listItems
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = langData?.globalString?.information.getSafe(),
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = langData?.globalString?.information.getSafe(),
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                cancellable = false,
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
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
                    title = langData?.errorMessages?.internetError.getSafe(),
                    message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun verifyPhoneNumApiCall() {
        if (isOnline(requireActivity())) {
            loginRequest.countryCode = getUser().countryCode.getSafe()
            loginRequest.phoneNumber = getUser().phoneNumber.getSafe()
            loginRequest.type = "login"
            authViewModel.verifyPhoneNumApiCall(loginRequest).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<UserResponse>
                        if (response.data?.network == "Other") {
                            showNoPackagesDialog()
                        } else {
                            packagesViewModel.phone.value = getUser().phoneNumber.getSafe()
                            packagesViewModel.network.value = response.data?.network

                            getListApi(
                                network = response.data?.network ?: "",
                                phone = getUser().phoneNumber.getSafe()
                            )
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {
                        findNavController().popBackStack()
                    }
                    is ResponseResult.Pending -> {
                        showLoader()
                    }
                    is ResponseResult.Complete -> {
                        hideLoader()
                    }
                    else -> {}
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = langData?.errorMessages?.internetError.getSafe(),
                    message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }

    }

    private fun showNoPackagesDialog() {
        DialogUtils(requireActivity())
            .showSingleButtonAlertDialog(
                title = "No packages found",
                message = "Currently we don't have any packages available for you at this time.",
                buttonCallback = {
                    findNavController().popBackStack()
                },
            )
    }

    fun getUser(): UserResponse {
        return tinydb.getObject(
            com.fatron.network_module.utils.Enums.TinyDBKeys.USER.key,
            UserResponse::class.java
        ) as UserResponse
    }


}