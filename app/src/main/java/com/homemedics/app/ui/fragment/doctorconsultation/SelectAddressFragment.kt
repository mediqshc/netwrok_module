package com.homemedics.app.ui.fragment.doctorconsultation

import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentSelectAddressBinding
import com.homemedics.app.model.AddressModel
import com.homemedics.app.ui.adapter.SelectAddressAdapter
import com.homemedics.app.utils.DataCenter
import com.homemedics.app.utils.DialogUtils
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.safeNavigate
import com.homemedics.app.viewmodel.DoctorConsultationViewModel
import com.homemedics.app.viewmodel.LabTestViewModel
import com.homemedics.app.viewmodel.PharmacyViewModel
import com.homemedics.app.viewmodel.ProfileViewModel

class SelectAddressFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentSelectAddressBinding

    private lateinit var addressAdapter: SelectAddressAdapter

    private val viewModel: ProfileViewModel by activityViewModels()

    private val doctorConsultationViewModel: DoctorConsultationViewModel by activityViewModels()
    private val labTestViewModel: LabTestViewModel by activityViewModels()
    private val pharmacyViewModel: PharmacyViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.globalString?.selectAddress.getSafe()
        }
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.addresses.clear()
    }

    override fun init() {
        observe()
        populateAddressList()
        getAddresses()
        preSelectedAddress()
    }

    override fun getFragmentLayout() = R.layout.fragment_select_address

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSelectAddressBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                onAction1Click = {
                    findNavController().popBackStack()
                }
                onAction2Click = {

                    findNavController().safeNavigate(
                        R.id.action_selectAddressFragment_to_addAddressFragment,
                        bundleOf("fromBDC" to true)
                    )
                }
            }
            bSelect.setOnClickListener(this@SelectAddressFragment)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bSelect -> {
                pharmacyViewModel.selectedAddress.postValue(addressAdapter.getSelectedItem())
                labTestViewModel.selectedAddress.postValue(addressAdapter.getSelectedItem())
                doctorConsultationViewModel.selectedAddress.postValue(addressAdapter.getSelectedItem())
                if(addressAdapter.getSelectedItem() == null){
                    DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                        title = mBinding.lang?.globalString?.information.getSafe(),
                        message = mBinding.lang?.bookingScreen?.chooseAddress.getSafe()
                    )
                    return
                }
                findNavController().popBackStack()
            }
        }
    }

    private fun observe() {
        viewModel.address.observe(this) {
            addressAdapter.listItems = viewModel.addresses as ArrayList<MultipleViewItem>
        }
    }

    private fun populateAddressList() {
        addressAdapter = SelectAddressAdapter()
        mBinding.apply {
            rvAddresses.adapter = addressAdapter
        }
    }

    private fun preSelectedAddress() {
        val fromBDC = arguments?.getBoolean("fromBDC")
        val fromPharmacy = arguments?.getBoolean("fromPharmacy")
        val fromLab = arguments?.getBoolean("fromLab")

        if (addressAdapter.listItems.isNotEmpty()) {

            if (fromBDC.getSafe()) {
                addressAdapter.listItems.map {
                    it.isSelected = it.itemId == doctorConsultationViewModel.selectedAddress.value?.itemId
                    it
                }
            }

            if (fromPharmacy.getSafe()) {
                addressAdapter.listItems.map {
                    it.isSelected = it.itemId == pharmacyViewModel.selectedAddress.value?.itemId
                    it
                }
            }

            if (fromLab.getSafe()) {
                addressAdapter.listItems.map {
                    it.isSelected = it.itemId == labTestViewModel.selectedAddress.value?.itemId
                    it
                }
            }
        }
    }

    private fun getAddresses() {
        val user = DataCenter.getUser()
        if (user?.userLocations?.isNotEmpty().getSafe()) {
            user?.userLocations?.map {
                val locCategory = metaData?.locationCategories?.find {loc-> loc.genericItemId == it.category?.toInt().getSafe() }
                var categoryLoc = ""
                if (locCategory!=null)
                    categoryLoc=locCategory.genericItemName.getSafe()

                if (categoryLoc.uppercase().getSafe().contains("other".uppercase().getSafe()))
                    categoryLoc= it.other.getSafe()

                val addressModel = AddressModel()
                addressModel.apply {
                    id = it.id
                    extraInt = id
                    streetAddress = it.street.getSafe()
                    category = categoryLoc
                    categoryId = it.category?.toInt().getSafe()
                    floor = it.floorUnit.getSafe()
                    subLocality = it.address.getSafe()
                    region = it.category.getSafe()
                    latitude = it.lat?.toDouble()
                    longitude = it.long?.toDouble()
                    region=it.region.getSafe()
                    other = it.other.getSafe()
                    title = categoryLoc
                    desc = it.address.getSafe()
                    itemId = locCategory?.genericItemId.getSafe().toString()
                    drawable = R.drawable.ic_location_trans
                }

                if(isUniqueItem(addressModel, viewModel.addresses as ArrayList<MultipleViewItem>))
                    viewModel.addresses.add(addressModel)
            }
                addressAdapter.listItems = (viewModel.addresses).map {
                    it.isSelected = it.extraInt.getSafe() == doctorConsultationViewModel.selectedAddress.value?.extraInt
                    it
                } as ArrayList<MultipleViewItem>

        }
    }

    private fun isUniqueItem(item: MultipleViewItem, list: ArrayList<MultipleViewItem>): Boolean{
        return list.find { it.title == item.title && it.desc == item.desc } == null
    }
}