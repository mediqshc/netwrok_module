package com.homemedics.app.ui.fragment.profile

import android.view.View
import androidx.fragment.app.activityViewModels
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.family.FamilyConnectionActionRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.family.FamilyResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentFamilyConnectionsBinding
import com.homemedics.app.model.ConnectionType
import com.homemedics.app.model.DemoModel
import com.homemedics.app.ui.adapter.FamilyConnectionsAdapter
import com.homemedics.app.utils.DialogUtils
import com.homemedics.app.utils.getErrorMessage
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.isOnline
import com.homemedics.app.viewmodel.ProfileViewModel


class FamilyConnectionsFragment() : BaseFragment(), View.OnClickListener {
    private lateinit var mBinding: FragmentFamilyConnectionsBinding
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var connectionType: ConnectionType

    private lateinit var adapter: FamilyConnectionsAdapter
val langData=ApplicationClass.mGlobalData
    override fun setLanguageData() {

    }

    override fun getFragmentLayout(): Int = R.layout.fragment_family_connections

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentFamilyConnectionsBinding
    }

    override fun setListeners() {
        adapter.onRequestAcceptClick = {item, position ->
            val request = FamilyConnectionActionRequest(relationId = item.id.toString())
            acceptConnectionApiCall(request, position)
        }
        adapter.onRequestRejectClick = {item, position ->
            DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                title = langData?.dialogsStrings?.confirmDelete.getSafe(),
                message = langData?.dialogsStrings?.deleteDesc.getSafe(),
                positiveButtonStringText = langData?.globalString?.yes.getSafe(),
                negativeButtonStringText =  langData?.globalString?.no.getSafe(),
                buttonCallback = {
                    val request = FamilyConnectionActionRequest(relationId = item.id.toString())
                    deleteConnectionApiCall(request, position)
                }
            )
        }
        mBinding.apply {
            swipeRefresh.setOnRefreshListener {
                swipeRefresh.isRefreshing = false
                (parentFragment as FamilyProfileFragment).getFamilyConnections()
            }
        }
    }

    override fun init() {
        connectionType = arguments?.getSerializable("type") as ConnectionType
        adapter = FamilyConnectionsAdapter(connectionType)
        mBinding.apply {
            rvConnections.adapter = adapter
        }

        observe()
    }

    override fun onClick(view: View?) {

    }

    private fun observe(){
        profileViewModel.familyConnections.observe(this){
            it?.let {
                when (connectionType){
                    ConnectionType.Connected -> adapter.listItems = it.connected as ArrayList
                    ConnectionType.Received -> adapter.listItems = it.recieved as ArrayList
                    ConnectionType.Sent -> adapter.listItems = it.sent as ArrayList
                }
            }
        }
    }

    private fun acceptConnectionApiCall(request: FamilyConnectionActionRequest, position: Int){
        if (isOnline(requireActivity())) {
            profileViewModel.connectFamilyConnection(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        (parentFragment as FamilyProfileFragment).getFamilyConnections()
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =it.error.message.getSafe(),
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
                    title = langData?.errorMessages?.internetError.getSafe(),
                    message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun deleteConnectionApiCall(request: FamilyConnectionActionRequest, position: Int){
        if (isOnline(requireActivity())) {
            profileViewModel.deleteFamilyConnectionApiCall(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        val familyConnection = response.data
                        (parentFragment as FamilyProfileFragment).getFamilyConnections()
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =it.error.message.getSafe(),
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
                    title = langData?.errorMessages?.internetError.getSafe(),
                    message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}