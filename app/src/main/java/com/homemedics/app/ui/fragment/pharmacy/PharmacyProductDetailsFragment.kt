package com.homemedics.app.ui.fragment.pharmacy

import android.graphics.Paint
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.pharmacy.PharmacyProductDetailRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.pharmacy.PharmacyCartResponse
import com.fatron.network_module.models.response.pharmacy.PharmacyProduct
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentPharmacyProductDetailsBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.activity.HomeActivity
import com.homemedics.app.ui.adapter.PharmacyListAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.PharmacyViewModel

class PharmacyProductDetailsFragment : BaseFragment(), View.OnClickListener {

    private val pharmacyViewModel: PharmacyViewModel by activityViewModels()

    private lateinit var mBinding: FragmentPharmacyProductDetailsBinding

    private lateinit var adapter: PharmacyListAdapter

    private val initialIndicationIsCollapsed = true

    private var isIndicationCollapsed = initialIndicationIsCollapsed

    private val initialContradictionIsCollapsed = true

    private var isContradictionCollapsed = initialContradictionIsCollapsed

    private var currency: String? = null

    private var quantity = 1

    private var price: Double = 0.0

    private var totalAmount: Double = 0.0

    private var locale: String? = null

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.labPharmacyScreen?.pharmacyTitle.getSafe()
        }
    }

    override fun init() {
        locale = TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
        if (pharmacyViewModel.pharmaProductIdsList.isEmpty())
            pharmacyViewModel.pharmaProductIdsList.add(pharmacyViewModel.pharmaProductId)

        pharmacyViewModel.pharmaProductId = pharmacyViewModel.pharmaProductIdsList[pharmacyViewModel.pharmaProductIdsList.size -1]
        observe()
        pharmacyProductDetailsApi()
    }

    override fun getFragmentLayout() = R.layout.fragment_pharmacy_product_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentPharmacyProductDetailsBinding
    }

    private fun setBackPressedListener() {
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                pharmacyViewModel.pharmaProductIdsList.removeAt(pharmacyViewModel.pharmaProductIdsList.size-1)
                findNavController().popBackStack()
            }
        })
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                onAction1Click = {
                    pharmacyViewModel.pharmaProductIdsList.removeAt(pharmacyViewModel.pharmaProductIdsList.size-1)
                    findNavController().popBackStack()
                }
                onAction2Click = {
                    if (pharmacyViewModel.products?.isEmpty().getSafe() || pharmacyViewModel.products == null) {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = lang?.labPharmacyScreen?.emptyCart.getSafe(),
                                buttonCallback = {},
                            )
                    } else {
                        findNavController().safeNavigate(
                            PharmacyProductDetailsFragmentDirections.actionPharmacyProductDetailsFragmentToPharmacyCartDetailsFragment()
                        )
                    }
                }
            }
            iCartButtons.apply {
                ivAdd.setOnClickListener {
                    quantity = quantity.plus(1)
                    tvQuantity.text = quantity.toString()
                    totalAmount = price.times(quantity)
                    tvProductTotalAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${lang?.globalString?.total.getSafe()}   ${getCommaFormatted(totalAmount.round(2))} $currency" else "${lang?.globalString?.total.getSafe()}   $currency ${getCommaFormatted(totalAmount.round(2))}"
                }
                ivSub.setOnClickListener {
                    quantity = quantity.minus(1)

                    if (quantity == 0) {
                        quantity = 1
                        return@setOnClickListener
                    }

                    tvQuantity.text = quantity.toString()
                    totalAmount = totalAmount.minus(price)
                    tvProductTotalAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${lang?.globalString?.total.getSafe()}   ${getCommaFormatted(totalAmount.round(2))} $currency" else "${lang?.globalString?.total.getSafe()}   $currency ${getCommaFormatted(totalAmount.round(2))}"
                }
            }
            llIndications.setOnClickListener(this@PharmacyProductDetailsFragment)
            llContraindications.setOnClickListener(this@PharmacyProductDetailsFragment)
            bAddToCart.setOnClickListener(this@PharmacyProductDetailsFragment)
        }

        if (activity is HomeActivity) {
            setBackPressedListener()
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.llIndications -> {
                mBinding.apply {
                    if (isIndicationCollapsed) {
                        tvIndicationsValue.visible()
                    } else {
                        tvIndicationsValue.gone()
                    }
                    isIndicationCollapsed = !isIndicationCollapsed
                }
            }
            R.id.llContraindications -> {
                mBinding.apply {
                    if (isContradictionCollapsed) {
                        tvContraindicationsValue.visible()
                    } else {
                        tvContraindicationsValue.gone()
                    }
                    isContradictionCollapsed = !isContradictionCollapsed
                }
            }
            R.id.bAddToCart -> pharmaAddToCartApi()
        }
    }

    private var isShow = false

    override fun onPause() {
        super.onPause()
        isShow = true
    }

    override fun onDetach() {
        super.onDetach()
        isShow = false
        pharmacyViewModel.quantityLiveData?.postValue(null)
    }

    private fun observe() {
        pharmacyViewModel.quantityLiveData?.observe(this) { quantity ->
            mBinding.apply {
                iCartButtons.apply {
                    tvQuantity.text = "${quantity ?: "1"}"
                }
            }
        }
    }

    private fun setupViews(pharmacyProducts: PharmacyProduct) {
        currency = metaData?.currencies?.find { it.genericItemId == pharmacyProducts.currencyId }?.genericItemName
        pharmacyProducts.apply {
                mBinding.apply {
                    actionbar.dotText = pharmacyViewModel.products?.size.getSafe().toString()
                    ivMedicineProduct.loadImage(media?.file.getSafe())
                    tvProductHeading.text = "${displayName.getSafe()} | ${packageType?.genericItemName.getSafe()}"
                    tvProductName.text = generic?.name.getSafe()
                    tvProductDesc.text = "${packageSize.getSafe()} ${dosage?.genericItemName.getSafe()} | ${category?.genericItemName.getSafe()}"
                    tvProductAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${price.toString()} $currency" else "$currency ${price.toString()}"
                    tvProductTotalAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${lang?.globalString?.total.getSafe()}   ${price?.getCommaRemoved()?.toDouble().round(2)} $currency" else "${lang?.globalString?.total.getSafe()}   $currency ${price?.getCommaRemoved()?.toDouble().round(2)}"
                    tvProductDescription.text = if (pharmacyProducts.description == null || pharmacyProducts.description?.isEmpty().getSafe()) lang?.labPharmacyScreen?.na.getSafe() else description
                    tvProductPrescription.text = if (prescriptionRequired == 0) lang?.labPharmacyScreen?.prescriptionNotRequired.getSafe() else lang?.labPharmacyScreen?.prescriptionRequired.getSafe()
                    tvIndicationsValue.text = if (generic?.indications?.isNullOrEmpty()?.not().getSafe()) generic?.indications.getSafe() else lang?.labPharmacyScreen?.na.getSafe()
                    tvContraindicationsValue.text = if (generic?.contraIndications?.isNullOrEmpty()?.not().getSafe()) generic?.contraIndications.getSafe() else lang?.labPharmacyScreen?.na.getSafe()
                    tvProductDiscount.apply {
                        text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${oldPrice.toString()} $currency" else "$currency ${oldPrice.toString()}"
                        setTextColor(resources.getColor(R.color.call_red, requireContext().theme))
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        setVisible(oldPrice != null)
                    }
                    iCartButtons.tvQuantity.text = if (pharmacyProducts.booking_pharmacy_product != null) {
                        pharmacyProducts.booking_pharmacy_product?.quantity?.toString().getSafe()
                    } else this@PharmacyProductDetailsFragment.quantity.toString()

                    this@PharmacyProductDetailsFragment.price = price?.getCommaRemoved()?.parseDouble().getSafe()
                    if (booking_pharmacy_product != null) {
                        this@PharmacyProductDetailsFragment.quantity = booking_pharmacy_product?.quantity.getSafe()
                        tvProductTotalAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${lang?.globalString?.total.getSafe()}   ${price?.getCommaRemoved()?.toDouble()?.times(this@PharmacyProductDetailsFragment.quantity).round(2).getSafe()} $currency" else "${lang?.globalString?.total.getSafe()}   $currency ${price?.getCommaRemoved()?.toDouble()?.times(this@PharmacyProductDetailsFragment.quantity)?.round(2).getSafe()}"
                    }
                }
                populateRelatedMedicinesProductList(relatedItems.getSafe())
        }
    }

    private fun populateRelatedMedicinesProductList(relatedItems: ArrayList<PharmacyProduct>) {
        mBinding.apply {
            adapter = PharmacyListAdapter()
            adapter.isRelatedItem = true
            rvRelatedItem.adapter = adapter
            adapter.listItems = relatedItems
            adapter.itemClickListener = { item, _ ->
                closeKeypad()
                pharmacyViewModel.pharmaProductId = item.id.getSafe()
                pharmacyViewModel.pharmaProductIdsList.add(pharmacyViewModel.pharmaProductId)
                findNavController().safeNavigate(
                    PharmacyProductDetailsFragmentDirections.actionPharmacyProductDetailsFragmentSelf()
                )
            }
        }
    }

    private fun pharmacyProductDetailsApi(quan: Int = 0) {
        val cityId = if (pharmacyViewModel.cityId != 0) pharmacyViewModel.cityId else null
        val countryId = if (pharmacyViewModel.countryId != 0) pharmacyViewModel.countryId else null
        val bookingId = tinydb.getInt(Enums.TinyDBKeys.BOOKING_ID.key)
        val productBookingId = if (bookingId > 0) bookingId else null
        val request = PharmacyProductDetailRequest(
            productId = pharmacyViewModel.pharmaProductId,
            bookingId = productBookingId,
            countryId = countryId,
            cityId = cityId
        )
        if (isOnline(requireActivity())) {
            pharmacyViewModel.pharmacyProductDetails(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        (response.data as PharmacyProduct).let { pharmacyProducts ->
                            setupViews(pharmacyProducts)
                            pharmacyViewModel.addToCart.apply {
                                productId = pharmacyProducts.id
                                quantity = quan
                                price = pharmacyProducts.price.getCommaRemoved()
                            }
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
                    else -> { hideLoader() }
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun pharmaAddToCartApi() {
        val bookingId = tinydb.getInt(Enums.TinyDBKeys.BOOKING_ID.key)
        pharmacyViewModel.addToCart.bookingId = if (bookingId > 0) bookingId else null
        pharmacyViewModel.addToCart.quantity = quantity
        if (isOnline(requireActivity())) {
            pharmacyViewModel.pharmaAddToCart(pharmacyViewModel.addToCart).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        (response.data as PharmacyCartResponse).let { pharmacyCartResponse ->
                            tinydb.putInt(Enums.TinyDBKeys.BOOKING_ID.key, pharmacyCartResponse.bookingId.getSafe())
                            findNavController().safeNavigate(
                                PharmacyProductDetailsFragmentDirections.actionPharmacyProductDetailsFragmentToPharmacyCartDetailsFragment()
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
                    else -> { hideLoader() }
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }


}