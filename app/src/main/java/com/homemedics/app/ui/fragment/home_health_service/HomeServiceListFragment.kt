package com.homemedics.app.ui.fragment.home_health_service

import android.app.AlertDialog
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.request.homeservice.HomeServiceDetailRequest
import com.fatron.network_module.models.request.homeservice.HomeServiceListRequest
import com.fatron.network_module.models.request.homeservice.HomeServiceStoreRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.homeservice.HomeServiceListResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogSelectCityBinding
import com.homemedics.app.databinding.FragmentHomeServiceListBinding
import com.homemedics.app.ui.activity.AuthActivity
import com.homemedics.app.ui.activity.SplashActivity
import com.homemedics.app.ui.adapter.HomeServiceAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.DoctorConsultationViewModel

class HomeServiceListFragment : BaseFragment(), View.OnClickListener {
    private lateinit var mBinding: FragmentHomeServiceListBinding
    private lateinit var homeServiceAdapter: HomeServiceAdapter
    private lateinit var dialogViewBinding: DialogSelectCityBinding
    private val homeServiceViewModel: DoctorConsultationViewModel by activityViewModels()

    override fun init() {
        setView()
        setActionbarCity()
        getServiceListCall()
        setObserver()

    }

    override fun setLanguageData() {
        mBinding.apply {
            etSearch.hint =  ApplicationClass.mGlobalData?.bookingScreen?.searchHomeService
            actionbar.title =  ApplicationClass.mGlobalData?.bookingScreen?.homeService.getSafe()
            tvNoData.text =  ApplicationClass.mGlobalData?.bookingScreen?.noHomeServiceFound.getSafe()

        }
     }

    override fun getFragmentLayout() = R.layout.fragment_home_service_list

    override fun getViewModel() {

    }

    private fun setView() {
        mBinding.apply {
            actionbar.action2Res = R.drawable.ic_expand
            homeServiceAdapter = HomeServiceAdapter()
            rvSpeciality.adapter = homeServiceAdapter
        }
    }


    override fun getViewBinding() {
        mBinding = binding as FragmentHomeServiceListBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }

            actionbar.onAction2Click = {
                showCityDialog()
            }
            homeServiceAdapter.itemClickListener = { item, _ ->
                if (isUserLoggedIn()) {
                    etSearch.text.clear()
                    homeServiceViewModel.homeServiceRequest =
                        HomeServiceDetailRequest(item.genericItemId)
                    homeServiceViewModel.fileList = arrayListOf()
                    homeServiceViewModel.homeConsultationRequest=HomeServiceStoreRequest()
                    findNavController().safeNavigate(R.id.action_homeServiceListFragment_to_homeServiceDetailFragment,
                        bundleOf(Constants.TITLE to item.genericItemName))
                } else {
                    val intent = Intent(requireActivity(), AuthActivity::class.java)
                    requireActivity().startActivity(intent)
                    requireActivity().finishAffinity()
                }

            }
            homeServiceAdapter.registerAdapterDataObserver(object :
                RecyclerView.AdapterDataObserver() {
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
                    tvNoData.setVisible((homeServiceAdapter.itemCount == 0))
                    rvSpeciality.setVisible((homeServiceAdapter.itemCount != 0))
                }
            })
        }
    }

    private fun showCityDialog() {
        val builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogViewBinding = DialogSelectCityBinding.inflate(layoutInflater)

            setView(dialogViewBinding.root)

            setPositiveButton(ApplicationClass.mGlobalData?.globalString?.select) { _, _ -> }
            setNegativeButton(ApplicationClass.mGlobalData?.globalString?.cancel, null)
            setCancelable(false)

            val indexSelection = 0
            //country
            dialogViewBinding.apply {
                 languData = ApplicationClass.mGlobalData
                cdCity.hint=languData?.globalString?.city.getSafe()
                cdCountry.hint=languData?.globalString?.country.getSafe()
                val countryList = getCountryList()
                cdCountry.data = countryList as ArrayList<String>
                if (homeServiceViewModel.countryId != 0) {
                    val index =
                        metaData?.countries?.indexOfFirst { it.id == homeServiceViewModel.countryId }
                    if (index != -1)
                        cdCountry.selectionIndex = index.getSafe()
                } else
                    homeServiceViewModel.countryId = metaData?.countries?.get(indexSelection)?.id.getSafe()

                cdCountry.onItemSelectedListener = { _, position: Int ->
                    homeServiceViewModel.countryId = metaData?.countries?.get(position)?.id.getSafe()
                    homeServiceViewModel.cityId=0
                    setCityList( homeServiceViewModel.countryId.getSafe())

                }

                setCityList( homeServiceViewModel.countryId.getSafe())
            }
        }.create()
        builder.setOnShowListener {
            val dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                val countryId=homeServiceViewModel.countryId
                val cityId=homeServiceViewModel.cityId
                if(countryId==0 || cityId==null){
                    Toast.makeText(requireContext(),ApplicationClass.mGlobalData?.dialogsStrings?.selectCity,Toast.LENGTH_SHORT).show()
                }else {
                    val city =
                        getCityList(homeServiceViewModel.countryId)?.find { city -> city.id == homeServiceViewModel.cityId }
                    val country =
                        metaData?.countries?.find { it.id == homeServiceViewModel.countryId }
                    homeServiceViewModel.countryName.value = country?.name.getSafe()
                    homeServiceViewModel.cityName.value = city?.name.getSafe()
                    mBinding.actionbar.desc = city?.name.getSafe()
                    getServiceListCall()
                    builder.dismiss()
                }
            }

        }
        builder.show()
    }

    private fun getServiceListCall() {
        if (isOnline(requireActivity())) {
            homeServiceViewModel.getHomeServiceList(HomeServiceListRequest(countryId = homeServiceViewModel.countryId,cityId = homeServiceViewModel.cityId ))
                .observe(this) { it ->
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<HomeServiceListResponse>
                            homeServiceViewModel.medSpecialities.value= response.data?.services.getSafe()
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
                                    message =   getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                    message =ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }


    private fun setCityList(countryId: Int) {
     val   citylist =getCityList(countryId = countryId)
        val cityList = citylist?.map { it.name }
        if (cityList?.size.getSafe() > 0) {
            dialogViewBinding.cdCity.data = arrayListOf<String>()
        dialogViewBinding.cdCity.data = cityList as ArrayList<String>

                val index = citylist.indexOfFirst {  it.id ==homeServiceViewModel.cityId }
                if (index != -1) {
                    if (homeServiceViewModel.cityIdGlobal != 0) {
                        dialogViewBinding.cdCity.selectionIndex = homeServiceViewModel.cityIdGlobal
                    } else {
                        dialogViewBinding.cdCity.selectionIndex = index.getSafe()
                    }
                } else
                {
                    dialogViewBinding.cdCity.selectionIndex =0
                    homeServiceViewModel.cityId = citylist[0].id.getSafe()
                }


        }else{
            homeServiceViewModel.cityId=null
            homeServiceViewModel.cityName.value=null
            dialogViewBinding.cdCity.data = arrayListOf<String>()
            dialogViewBinding.cdCity. mBinding.dropdownMenu.setText("", true)
        }

        dialogViewBinding.cdCity.onItemSelectedListener = { _, position: Int ->
            homeServiceViewModel.cityId = citylist?.get(position)?.id.getSafe()
            homeServiceViewModel.cityName.value = citylist?.get(position)?.name.getSafe()
            dialogViewBinding.cdCity.clearFocus()
            homeServiceViewModel.cityIdGlobal = position

        }
    }

    private fun setObserver() {
        homeServiceViewModel.medSpecialities.observe(this) {
            mBinding.apply {
                rvSpeciality.setVisible((it.isNullOrEmpty().not()))
                tvNoData.setVisible((it.isNullOrEmpty()))
            }
            homeServiceAdapter.listItems = it
            homeServiceAdapter.originalList = it
            mBinding.etSearch.addTextChangedListener {
                homeServiceAdapter.filter.filter(it)
            }
        }
    }

    private fun setActionbarCity(){
        //action bar city name
        if(homeServiceViewModel.cityName.value.getSafe().isEmpty()){
            val user = DataCenter.getUser()
            user?.let {
                val city = getCityList(it.countryId.getSafe())?.find { city -> city.id == it.cityId }
                mBinding.actionbar.desc = city?.name.getSafe()
                homeServiceViewModel.cityId = city?.id.getSafe()
                homeServiceViewModel.cityName.value=city?.name.getSafe()

                val countryIndex = metaData?.countries?.indexOfFirst { country->country.id == it.countryId.getSafe() }.getSafe()

                val country = metaData?.countries?.get(countryIndex)
                homeServiceViewModel.countryId = country?.id.getSafe()
                homeServiceViewModel.countryName.value = country?.name.getSafe()
            }.run {
                val city = getCityList(metaData?.defaultCountryId.getSafe())?.find { city -> city.id == metaData?.defaultCityId.getSafe() }
                mBinding.actionbar.desc = city?.name.getSafe()
            }
        }
        else mBinding.actionbar.desc = homeServiceViewModel.cityName.value.getSafe()
    }

    override fun onClick(v: View?) {

    }

}