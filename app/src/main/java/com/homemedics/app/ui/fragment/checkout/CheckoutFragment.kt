package com.homemedics.app.ui.fragment.checkout

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.compose.ui.text.toLowerCase
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.appointments.AppointmentDetailReq
import com.fatron.network_module.models.request.checkout.LinkedCredit
import com.fatron.network_module.models.request.checkout.SplitAmount
import com.fatron.network_module.models.request.checkout.paymob.PaymobPaymentRequest
import com.fatron.network_module.models.request.checkout.paymob.PaymobPaymentStatusRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.checkout.CheckoutDetailResponse
import com.fatron.network_module.models.response.checkout.Promotions
import com.fatron.network_module.models.response.checkout.paymob.PaymobOrderResponse
import com.fatron.network_module.models.response.checkout.paymob.PaymobPaymentResponse
import com.fatron.network_module.models.response.checkout.paymob.PaymobTokenResponse
import com.fatron.network_module.models.response.labtest.LabTestResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.meta.PartnerService
import com.fatron.network_module.models.response.pharmacy.Product
import com.fatron.network_module.models.response.user.UserResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogAddPromoBinding
import com.homemedics.app.databinding.DialogLinkedCreditBinding
import com.homemedics.app.databinding.DialogPaymentFailBinding
import com.homemedics.app.databinding.FragmentCheckoutBinding
import com.homemedics.app.interfaces.NegativeAmountInterface
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.adapter.CheckoutLinkedCreditAdapter
import com.homemedics.app.ui.adapter.CheckoutPaymentMethodsAdapter
import com.homemedics.app.ui.adapter.CheckoutSplitAmountAdapter
import com.homemedics.app.ui.adapter.PromotionsAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.CheckoutViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CheckoutFragment : BaseFragment(), View.OnClickListener {

    private var selectedPayment: GenericItem? = null
    private var discount: String? = "0"
    private var promotionId: Int? = 0
    private var packageId: Int? = 0
    private var isPromoError = false
    private val checkoutViewModel: CheckoutViewModel by activityViewModels()
    private lateinit var mBinding: FragmentCheckoutBinding
    private lateinit var builder: AlertDialog
    private var services: PartnerService? = null
    private var fromChat = false
    private var isPaymentSelected = false
    var total="0.00"
    private var locale: String? = null
    private var promoPackageList: ArrayList<Promotions>? = null
    private lateinit var promoAdapter: PromotionsAdapter
    private var appliedPackageAmount = 0.00
    private var confirmButtonEnabledAmount = 0.00

    companion object {
        var WebViewCheck = MutableLiveData<String>()
    }

    private var currency = ""
    private var fees = ""
    private var paymentIntegrationId = 0
    private var paymentMethodId = 0
    private val start = "\u2066"
    private val end = "\u2069"

    private val user: UserResponse? by lazy {
        DataCenter.getUser()
    }

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.checkoutScreen?.checkout.getSafe()
        }
    }

    override fun init() {
        val gridSpacing = resources.getDimensionPixelSize(R.dimen.dp10)
        mBinding.rvLinkedCredit.addItemDecoration(GridItemDecoration(gridSpacing, 3, false))
        locale =
            TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        val fromBDC = requireActivity().intent?.hasExtra("fromBDC").getSafe()
        fromChat = requireActivity().intent?.hasExtra("fromChat").getSafe()
        val bookingId = requireActivity().intent?.getIntExtra(Constants.BOOKING_ID, 0).getSafe()
        if (bookingId != 0) {
            checkoutViewModel.bookConsultationRequest.bookingId = bookingId
        }
        if (fromBDC == true) {
            mBinding.deliverycharges.gone()
        }
        val request =
            AppointmentDetailReq(checkoutViewModel.bookConsultationRequest.bookingId.toString())
        if (fromChat.getSafe())

            request.renewConsultation = 1
        initWebView()
        setObserver()
        checkoutDetails(request)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("booking_id", checkoutViewModel.bookConsultationRequest.bookingId.getSafe())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            checkoutViewModel.bookConsultationRequest.bookingId =
                savedInstanceState.getInt("booking_id", 0)
            val request =
                AppointmentDetailReq(checkoutViewModel.bookConsultationRequest.bookingId.toString())
            checkoutDetails(request)
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_checkout

    override fun getViewModel() {

    }

    fun extractStatusFromUrl(url: String): String? {
        val statusParam = url.substringAfterLast("status=")
        return statusParam.substringBefore("&")
    }

    private fun setObserver() {
        WebViewCheck.observe(this) {
            it ?: return@observe
            val status = extractStatusFromUrl(it)
            when (status?.toLowerCase()) {
                "success" -> {
                    mBinding.clCheckout.removeView(mBinding.webView)
                    mBinding.webView.removeAllViews()
                    mBinding.webView.destroy()
                    mBinding.webView.gone()
                    if (fees.equals(mBinding.lang?.globalString?.free.getSafe(), true))
                        fees = "0"

                    WebViewCheck.value = null

                    callOnlyStatusUpdateApi()
                }
                "failure" -> {
                    lifecycleScope.launch {
                        delay(500)
                        mBinding.webView.gone()
                        initWebView()
                        paymentMethodId = 0
                        paymentIntegrationId = 0
                        isPaymentSelected = false
                        selectedPayment?.isChecked = false
                        WebViewCheck.value = null
                        val request =
                            AppointmentDetailReq(checkoutViewModel.bookConsultationRequest.bookingId.toString())
                        checkoutDetails(request)
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.dialogsStrings?.paymentDeclined.getSafe(),
                                message = mBinding.lang?.dialogsStrings?.paymentDeclinedDesc.getSafe()
                            )
                    }
                }
                else -> {

                }
            }
            /*
            it ?: return@observe
            if (it.isNotEmpty()) {
                var successStatus = 0
                val splitUrl = it.split("&")
                val splitData = splitUrl.find { it.startsWith("success") }
                if (splitData != null) {
                    val successData = splitData.split("=")
                    if (successData.size.getSafe() > 0)
                        successStatus = if (successData[1] == "true") 1 else 0

                    if (successStatus == 0) {
                        lifecycleScope.launch {
                            delay(500)
                            mBinding.webView.gone()
                            initWebView()
                            paymentMethodId = 0
                            paymentIntegrationId = 0
                            isPaymentSelected = false
                            selectedPayment?.isChecked = false
                            WebViewCheck.value = null
                            val request =
                                AppointmentDetailReq(checkoutViewModel.bookConsultationRequest.bookingId.toString())
                            checkoutDetails(request)
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    title = mBinding.lang?.dialogsStrings?.paymentDeclined.getSafe(),
                                    message = mBinding.lang?.dialogsStrings?.paymentDeclinedDesc.getSafe()
                                )
                        }
                    } else {
                        mBinding.clCheckout.removeView(mBinding.webView)
                        mBinding.webView.removeAllViews()
                        mBinding.webView.destroy()
                        mBinding.webView.gone()
                        if (fees.equals(mBinding.lang?.globalString?.free.getSafe(), true))
                            fees = "0"

                        WebViewCheck.value = null

                        callOnlyStatusUpdateApi()
                    }
                }

            }
            */
        }
    }

    override fun getViewBinding() {
        mBinding = binding as FragmentCheckoutBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                requireActivity().onBackPressed()
                promoAdapter.deleteClickPackage = {

                    DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                        title = mBinding.lang?.dialogsStrings?.confirmDelete.getSafe(),
                        message = mBinding.lang?.dialogsStrings?.deleteDesc.getSafe(),
                        positiveButtonStringText = mBinding.lang?.globalString?.yes.getSafe(),
                        negativeButtonStringText = mBinding.lang?.globalString?.no.getSafe(),
                        buttonCallback = {
                            val request = AppointmentDetailReq(
                                bookingId = checkoutViewModel.bookConsultationRequest.bookingId.toString(),
                                promotionId = it.promotionId
                            )
                            removePackageApi(request)
                        }
                    )
                }


            }
            tvAddPromo.setOnClickListener {
                showAddPromoDialog()
            }
            bConfirmPay.setOnClickListener {
                when (paymentMethodId) {
                    Enums.PaymentMethod.JAZZ_CASH.id, Enums.PaymentMethod.DEBIT_CREDIT_CARD.id -> {
                        if (fees.equals(
                                mBinding.lang?.globalString?.free.getSafe(),
                                true
                            ) || fees.isEmpty()
                        )
                            callOnlyStatusUpdateApi()
                        else
                            getPaymobToken()
                    }
                    else -> {
                        callOnlyStatusUpdateApi()
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {

    }

    override fun onDetach() {
        super.onDetach()
        checkoutViewModel.flushData()
        isPaymentSelected = false
        WebViewCheck.value = null
    }

    private fun callOnlyStatusUpdateApi() {
        val fee = if (fees.equals(
                mBinding.lang?.globalString?.free.getSafe(),
                true
            ) || fees.isEmpty() || fees == "0"
        ) 0 else (fees.parseDouble() * 100)

        val request = PaymobPaymentStatusRequest(
            response = null,
            amountCents = fee.toInt(),
            currency = currency,
            bookingId = if (fromChat) checkoutViewModel.checkoutDetailResponse.id else checkoutViewModel.bookConsultationRequest.bookingId.getSafe(),
            integrationId = paymentIntegrationId,
            status = 1,
            paymentMethodId = paymentMethodId,
            currencyId = checkoutViewModel.checkoutDetailResponse.currencyId,
            promotionId = if (promotionId != 0) promotionId else null,
            discount = discount
        )
        paymobPaymentStatus(request)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setDataInViews() {
        total = checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.total.getSafe()

        if (total.getCommaRemoved().toDouble() != 0.00 && checkoutViewModel.checkoutDetailResponse.availablePackages?.isNotEmpty() == true)
            try {

                // Using toDouble() - may throw NumberFormatException
                var amountPayable = (total.getCommaRemoved().toDouble())
                println("Converted value using toDouble(): $amountPayable")

                total =
                    (amountPayable + checkoutViewModel.checkoutDetailResponse.promotions?.discountValue.getCommaRemoved()
                        .parseDouble()).round(2).toString()
            } catch (e: NumberFormatException) {
                println("Conversion to double failed: ${e.message}")
            }
        else if (checkoutViewModel.checkoutDetailResponse.availablePackages?.isNotEmpty() == true || checkoutViewModel.checkoutDetailResponse.packages?.isNotEmpty()==true) {
            val subTotal =
                checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.subTotal.getCommaRemoved()
                    .toDouble()
            checkoutViewModel.checkoutDetailResponse.packages?.forEach {
                appliedPackageAmount = it.discountValue.getCommaRemoved().parseDouble()
            }
            total = (subTotal - appliedPackageAmount).round(2).toString()
        } else {
            total = total.getCommaRemoved().parseDouble().round(2).toString()
        }

        //adding user delivery charges for min amount .....
        val isPharmacyOrder =
            checkoutViewModel.bookConsultationRequest.serviceId == CustomServiceTypeView.ServiceType.PharmacyService.id
        val isLabOrder =
            checkoutViewModel.bookConsultationRequest.serviceId == CustomServiceTypeView.ServiceType.LaboratoryService.id

        if ( isPharmacyOrder) {
            if (checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.subTotal.getCommaRemoved()
                    .parseDouble() < user?.deliveryCharges?.first()?.min_amount.getSafe()
            ) {
                // add discount in total amount
                total =
                    (total.getCommaRemoved().toDouble() + user?.deliveryCharges?.first()?.delivery_charges.getSafe()).toString()
                //set discount amount
                mBinding.tvDeliveryAmount.text =
                    user?.deliveryCharges?.first()?.delivery_charges.getSafe().toString()
            } else {
                mBinding.tvDeliveryAmount.text = "0.00"
            }
        } else {
            mBinding.tvDeliveryAmount.text = "0.00"
        }

        val locale =
            TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        mBinding.apply {
            fees = total.getCommaRemoved()

            val isHHC =
                checkoutViewModel.bookConsultationRequest.serviceId == CustomServiceTypeView.ServiceType.HealthCare.id
            val isPharmacy =
                checkoutViewModel.bookConsultationRequest.serviceId == CustomServiceTypeView.ServiceType.PharmacyService.id
            val isLab =
                checkoutViewModel.bookConsultationRequest.serviceId == CustomServiceTypeView.ServiceType.LaboratoryService.id

            val service = if (isHHC)
                metaData?.specialties?.medicalStaffSpecialties?.find {
                    it.genericItemId == checkoutViewModel.checkoutDetailResponse.specialityId
                }?.genericItemName
            else if (isPharmacy || isLab)
                metaData?.partnerServiceType?.find {
                    it.id == checkoutViewModel.bookConsultationRequest.serviceId
                }?.shortName
            else
                "${
                    metaData?.partnerServiceType?.find {
                        it.id == checkoutViewModel.bookConsultationRequest.serviceId
                    }?.name
                }"

            tvPatient.text =
                "$start${mBinding.lang?.checkoutScreen?.forString.getSafe()} ${checkoutViewModel.checkoutDetailResponse.bookedForUser?.fullName.getSafe()}$end"
            tvService.text = service

            currency =
                DataCenter.getMeta()?.currencies?.find { it.itemId == checkoutViewModel.checkoutDetailResponse.currencyId.toString() }?.genericItemName.getSafe()
            val breakDown = checkoutViewModel.checkoutDetailResponse.paymentBreakdown
            val subtotal = "${checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.subTotal}"
            var orderDetails = arrayListOf<SplitAmount>()

            if (checkoutViewModel.bookConsultationRequest.serviceId == CustomServiceTypeView.ServiceType.HealthCare.id) { //home healthcare
                orderDetails =
                    checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.duties.getSafe()
                orderDetails.map { splitAmount ->
                    splitAmount.specialityName =
                        "${splitAmount.specialityName} x ${splitAmount.daysQuantity}"
                    splitAmount
                }
            } else {
                val serviceCharges = "$service ${lang?.globalString?.charges.getSafe()}"
                orderDetails = arrayListOf(
                    SplitAmount(specialityName = serviceCharges, fee = subtotal),
                )
            }

            val splitAmountAdapter = CheckoutSplitAmountAdapter()
            splitAmountAdapter.listItems = orderDetails
            rvSplitAmount.adapter = splitAmountAdapter
            promoAdapter = PromotionsAdapter()
            populatePromotions(checkoutViewModel.checkoutDetailResponse)
            populatePackages(checkoutViewModel.checkoutDetailResponse)

            tvPayableAmountStrip.text = subtotal

            tvPayableAmount.text =
                if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${total.getCommaRemoved().toDouble().round(2)} $currency" else "$currency ${total.getCommaRemoved().toDouble().round(2)}"
            confirmButtonEnabledAmount =
                tvPayableAmount.text.toString().getCommaRemoved().replace(Regex("[^\\d.]"), "")
                    .toDouble()
            bConfirmPay.text =
                if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${mBinding.lang?.checkoutScreen?.confirmAndPay.getSafe()} ${total.getCommaRemoved().toDouble().round(2)} $currency" else "${mBinding.lang?.checkoutScreen?.confirmAndPay.getSafe()} $currency ${total.getCommaRemoved().toDouble().round(2)}"
            if (total.getCommaRemoved().parseDouble().getSafe() > 0) {
                bConfirmPay.isEnabled = true
                bConfirmPay.isEnabled =
                    checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.total == "0" || checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.total == "0.00" || checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.payableAmount.isNullOrEmpty() || checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.payableAmount == "0" || checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.payableAmount == "0.00" || checkoutViewModel.checkoutDetailResponse.currencyId == 0
            } else {
                bConfirmPay.isEnabled = false
            }

            val linkedCredits = arrayListOf<LinkedCredit>()
            val availablePromos =
                checkoutViewModel.checkoutDetailResponse.availablePromotions.getSafe()
            val availablePackages =
                checkoutViewModel.checkoutDetailResponse.availablePackages.getSafe()
            // i comment this code in case if we dont have to show discount packages in android in checkout
            if (total.getMinusRemoved().getCommaRemoved().parseDouble() > 0.0) {
                availablePromos.forEach { promos ->
                    linkedCredits.apply {
                        if (promos.discountValue.getMinusRemoved().getCommaRemoved()
                                .parseDouble() > 0.0
                        )
                            add(
                                LinkedCredit(
                                    id = promos.promotionId,
                                    amount = "$currency ${promos.discountValue}",
                                    name = promos.promotionName?.trimIndent(),
                                    promoCode = promos.promotionPromocode,
                                    iconUrl = promos.bannerImage,
                                    isPromo = promos.isPromotion
                                )
                            )
                    }
                }
            }

            if (total.getMinusRemoved().getCommaRemoved().parseDouble() > 0.0) {
                availablePackages.forEach { promos ->
                    linkedCredits.apply {
                        if (promos.discountValue.getMinusRemoved().getCommaRemoved()
                                .parseDouble() > 0.0
                        )
                            add(
                                LinkedCredit(
                                    id = promos.promotionId,
                                    amount = "$currency ${promos.discountValue}",
                                    name = promos.promotionName,
                                    promoCode = promos.promotionPromocode,
                                    iconUrl = promos.bannerImage,
                                    isPromo = promos.isPromotion,
                                    discount = promos.discountValue
                                )
                            )
                    }
                }
            }

            val linkedCreditAdapter = CheckoutLinkedCreditAdapter()
            linkedCreditAdapter.itemClickListener = { item, _ ->
                if (promoPackageList?.isNotEmpty().getSafe()) {
                    run breaking@{
                        promoPackageList?.forEach { promoListItem ->
                            if (item.id == promoListItem.promotionId && item.iconUrl.isNullOrEmpty()) {
                                DialogUtils(requireActivity())
                                    .showSingleButtonAlertDialog(
                                        title = mBinding.lang?.globalString?.information.getSafe(),
                                        message = mBinding.lang?.dialogsStrings?.promoAdded.getSafe(),
                                        buttonCallback = {}
                                    )
                                return@breaking
                            } else if (item.isPromo.getSafe()) {
                                promotionId = item.id
                                val id = item.id.toString()
                                val request = AppointmentDetailReq(
                                    bookingId = checkoutViewModel.bookConsultationRequest.bookingId.toString(),
                                    promoCode = if (item.promoCode != null) item.promoCode else id
                                )
                                checkoutDetails(request)
                                return@breaking
                            } else {
                                packageId = item.id
                                showAddLinkedCreditDialog(item)
                                return@breaking
                            }
                        }
                    }
                } else {
                    if (item.isPromo.getSafe()) {
                        promotionId = item.id
                        val id = item.id.toString()
                        val request = AppointmentDetailReq(
                            bookingId = checkoutViewModel.bookConsultationRequest.bookingId.toString(),
                            promoCode = if (item.promoCode != null) item.promoCode else id
                        )
                        checkoutDetails(request)
                    } else {
                        packageId = item.id
                        showAddLinkedCreditDialog(item)
                    }
                }

            }

            linkedCreditAdapter.listItems = linkedCredits
            rvLinkedCredit.adapter = linkedCreditAdapter


            val serviceId = checkoutViewModel.bookConsultationRequest.serviceId
            val paymentMethods = arrayListOf<GenericItem>()
            metaData?.paymentMethods?.forEach {


                it.paymentMethod?.let { it1 ->

                    if ((serviceId == CustomServiceTypeView.ServiceType.VideoCall.id || serviceId == CustomServiceTypeView.ServiceType.Message.id) && it.paymentMethodId == Enums.PaymentMethod.COD.id) //COD shouldn't be in video and message
                        return@forEach
                    paymentMethods.add(it1)
                }
            }
            val paymentMethodAdapter = CheckoutPaymentMethodsAdapter()
            paymentMethodAdapter.itemClickListener = { item, pos ->
                paymentMethodId = item.genericItemId.getSafe()

                val tempIntegrationId = item.integrationId
                if (tempIntegrationId.isNullOrEmpty().not())
                    paymentIntegrationId = tempIntegrationId.getSafe().toInt()
                if (paymentMethodId == Enums.PaymentMethod.COD.id)
                    paymentIntegrationId = 0

                paymentMethodAdapter.listItems.map {
                    it.isChecked = false
                    it
                }
                selectedPayment = paymentMethodAdapter.listItems[pos]
                paymentMethodAdapter.listItems[pos].isChecked = true
                paymentMethodAdapter.notifyDataSetChanged()
                isPaymentSelected = true
                if (total.getCommaRemoved().parseDouble().getSafe() < 0) {
                    bConfirmPay.isEnabled = false
                } else {
                    bConfirmPay.isEnabled = true
                    bConfirmPay.isEnabled = isPaymentSelected
                }
            }


            paymentMethodAdapter.listItems = paymentMethods.getSafe()
            rvPaymentOptions.adapter = paymentMethodAdapter

            val freeOrZero =
                checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.total == "0" || checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.total == "0.00" || checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.payableAmount == "0" || checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.payableAmount == "0.00" || checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.payableAmount?.contains(
                    "Free",
                    true
                ).getSafe()
            if (freeOrZero) {
                mBinding.apply {
                    vDivider.gone()
                    tvPaymentHeading.gone()
                    rvPaymentOptions.gone()
                    if (total.getCommaRemoved().parseDouble().getSafe() < 0) {
                        bConfirmPay.isEnabled = false
                    } else if (total.parseDouble() > 0 && total.parseDouble() < 1) {
                        rvPaymentOptions.gone()
                        // tvPayableAmount.text =total.parseDouble().minus(total.parseDouble()).toString()
                        bConfirmPay.text = "Confirm"
                        bConfirmPay.isEnabled = true

                    } else {

                        if (total.parseDouble() == 0.00) {
                            bConfirmPay.isEnabled = true
                            bConfirmPay.text = "Confirm"
                        } else {
                            bConfirmPay.isEnabled = false
                            rvPaymentOptions.visible()
                            //bConfirmPay.text = mBinding.lang?.globalString?.confirm.getSafe()

                            bConfirmPay.text = "Confirm and pay ${total.getCommaRemoved().toDouble().round(2)}"
                            //bConfirmPay.isEnabled = freeOrZero
                        }
                    }
                }
            } else {
                vDivider.visible()
                tvPaymentHeading.visible()
                rvPaymentOptions.visible()
            }

            // Labs test payment break
            services =
                metaData?.partnerServiceType?.find { it.id == checkoutViewModel.checkoutDetailResponse.partnerServiceId }

            if (services?.id == CustomServiceTypeView.ServiceType.LaboratoryService.id) {
                fees =
                    checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.payableAmount.getCommaRemoved()
                val sampleCharges =
                    "${checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.sampleCharges}"

                val labList = arrayListOf<LabTestResponse>()
                breakDown?.labs?.map { lab ->
                    lab.labTest?.genericItemName = lab.labTest?.genericItemName
                    lab
                }
                breakDown?.labs?.let { labList.addAll(it) }

                val splitAmount = arrayListOf<SplitAmount>()
                labList.forEach { lab ->
                    if (lab.totalFee.isNullOrEmpty()) lab.totalFee = "0"

                    splitAmount.add(
                        SplitAmount(
                            specialityName = lab.labTest?.genericItemName,
                            fee = lab.totalFee?.toString()
                        )
                    )
                }

                splitAmount.apply {
                    add(
                        SplitAmount(
                            specialityName = lang?.checkoutScreen?.homeCollectionCharges.getSafe(),
                            fee = sampleCharges
                        )
                    )

                    if (breakDown?.corporateDiscount != null)
                        add(
                            SplitAmount(
                                specialityName = lang?.globalString?.corporateDiscount.getSafe(),
                                fee = "$currency ${breakDown.corporateDiscount.toString()}"
                            )
                        )

                    if (breakDown?.promoDiscount != null)
                        add(
                            SplitAmount(
                                specialityName = lang?.globalString?.promoDiscount.getSafe(),
                                fee = "$currency ${breakDown.promoDiscount.toString()}"
                            )
                        )

                    if (breakDown?.companyCredit != null)
                        add(
                            SplitAmount(
                                specialityName = lang?.globalString?.companyDiscount.getSafe(),
                                fee = "$currency ${breakDown.companyCredit.toString()}"
                            )
                        )
                }

//                val netPayment = breakDown?.subTotal?.toInt().getSafe() +
//                        breakDown?.corporateDiscount.getSafe() +
//                        breakDown?.promoDiscount.getSafe() +
//                        breakDown?.companyCredit.getSafe()

                splitAmountAdapter.listItems = splitAmount
                mBinding.apply {
                    val lab = checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.labs
                    iLab.apply {
                        root.visible()
                        if (lab.isNullOrEmpty().not()) {
                            ivIcon.loadImage(lab?.get(0)?.bookingLaboratory?.lab?.iconUrl)
                            tvTitle.text = lab?.get(0)?.bookingLaboratory?.lab?.name.getSafe()
                        }
                    }
                    rvSplitAmount.apply {
                        visible()
                        adapter = splitAmountAdapter
                    }
                    tvPayableAmountStrip.text =
                        "${checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.subTotal}"
                    val discontvalue =
                        checkoutViewModel.checkoutDetailResponse.promotions?.discountValue.getSafe()
                    tvPayableAmount.text =
                        if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${total.getCommaRemoved().toDouble().round(2)} $currency" else "$currency ${total.getCommaRemoved().toDouble().round(2)}"
                }
            }

            // Pharmacy payment breakdown
            if (services?.id == CustomServiceTypeView.ServiceType.PharmacyService.id) {
                val capitalMultiplicationSign = "\u2A2F"
                val pharmacyList = arrayListOf<Product>()
                breakDown?.products?.map { product ->
                    product.product?.displayName =
                        "${product.product?.displayName.getSafe()} $capitalMultiplicationSign ${product.quantity.getSafe()} ${product.product?.dosage?.genericItemName.getSafe()}"
                    product
                }
                breakDown?.products?.let { pharmacyList.addAll(it) }

                val splitAmount = arrayListOf<SplitAmount>()
                pharmacyList.forEach { prod ->
                    splitAmount.add(
                        SplitAmount(
                            specialityName = prod.product?.displayName,
                            fee = prod.subtotal?.toString(),
                            isAvailable = prod.isAvailable,
                            isSubstitute = prod.isSubstitute
                        )
                    )
                }

                splitAmount.apply {
                    if (breakDown?.corporateDiscount != null)
                        add(
                            SplitAmount(
                                specialityName = lang?.globalString?.corporateDiscount.getSafe(),
                                fee = "$currency ${breakDown.corporateDiscount.toString()}"
                            )
                        )

                    if (breakDown?.promoDiscount != null)
                        add(
                            SplitAmount(
                                specialityName = lang?.globalString?.promoDiscount.getSafe(),
                                fee = "$currency ${breakDown.promoDiscount.toString()}"
                            )
                        )

                    if (breakDown?.companyCredit != null)
                        add(
                            SplitAmount(
                                specialityName = lang?.globalString?.companyDiscount.getSafe(),
                                fee = "$currency ${breakDown.companyCredit.toString()}"
                            )
                        )
                }

                //                val netPayment = breakDown?.subTotal?.toInt().getSafe() +
                //                        breakDown?.corporateDiscount.getSafe() +
                //                        breakDown?.promoDiscount.getSafe() +
                //                        breakDown?.companyCredit.getSafe()

                splitAmountAdapter.listItems = splitAmount
                mBinding.apply {
                    rvSplitAmount.apply {
                        visible()
                        adapter = splitAmountAdapter
                    }
                    tvPayableAmountStrip.text =
                        "${checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.subTotal}"
                    tvPayableAmount.text =
                        if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${total.getCommaRemoved().toDouble().round(2)} $currency" else "$currency ${total.getCommaRemoved().toDouble().round(2)}"
                }
            }

        }
    }

    fun calculatePayabaleAmount() {
        val subTotal =
            checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.subTotal.getCommaRemoved()
                .toDouble()
        val totalAmount = (subTotal - checkoutViewModel.payAbleAmount).toString()
    }

    private fun populatePromotions(checkout: CheckoutDetailResponse?) {
        promoPackageList = arrayListOf<Promotions>().apply {
            checkout?.promotions?.let { promo ->
                add(promo)
            }
        }

        promoAdapter.listItems = promoPackageList.getSafe()
        mBinding.rvPromotions.apply {
            setVisible(promoPackageList?.isNotEmpty().getSafe())
            adapter = promoAdapter
        }
        promoAdapter.deleteClick = { promo ->
            DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                title = mBinding.lang?.dialogsStrings?.confirmDelete.getSafe(),
                message = mBinding.lang?.dialogsStrings?.deleteDesc.getSafe(),
                positiveButtonStringText = mBinding.lang?.globalString?.yes.getSafe(),
                negativeButtonStringText = mBinding.lang?.globalString?.no.getSafe(),
                buttonCallback = {
                    val request = AppointmentDetailReq(
                        bookingId = checkoutViewModel.bookConsultationRequest.bookingId.toString(),
                        promoCode = if (promo.promotionPromocode != null) "" else "-"
                    )
                    checkoutDetails(request, true)
                }
            )
        }
        if (isPromoError && checkout?.promotions == null) {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.lang?.globalString?.warning.getSafe(),
                    message = mBinding.lang?.dialogsStrings?.invalidPromoCode.getSafe(),
                    buttonCallback = {
                        isPromoError = false
                    },
                )
        }
        promotionId = checkout?.promotions?.promotionId.getSafe()
        discount = checkout?.promotions?.discountValue.getSafe()
    }

    private fun populatePackages(checkout: CheckoutDetailResponse?) {
        promoPackageList?.addAll(checkout?.packages.getSafe())
        promoPackageList?.forEach {
            if (it.discountTypeId != null) {
                checkoutViewModel.discountOnPackageAmount =
                    it.discountValue.getCommaRemoved().parseDouble()
            }
        }
        promoPackageList?.map {
            if (it.discountTypeId == null) {
                it.discountValue = (it.discountValue.getCommaRemoved().parseDouble()
                    .getSafe() - checkoutViewModel.discountOnPackageAmount).toString()
                checkoutViewModel.payAbleAmount = it.discountValue.getCommaRemoved().parseDouble()
                    .getSafe() + checkoutViewModel.discountOnPackageAmount


            }
        }
        promoAdapter.listItems = promoPackageList.getSafe()
        mBinding.rvPromotions.apply {
            setVisible(promoPackageList?.isNotEmpty().getSafe())
            adapter = promoAdapter
        }
        promoAdapter.deleteClickPackage = {
            DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                title = mBinding.lang?.dialogsStrings?.confirmDelete.getSafe(),
                message = mBinding.lang?.dialogsStrings?.deleteDesc.getSafe(),
                positiveButtonStringText = mBinding.lang?.globalString?.yes.getSafe(),
                negativeButtonStringText = mBinding.lang?.globalString?.no.getSafe(),
                buttonCallback = {
                    val request = AppointmentDetailReq(
                        bookingId = checkoutViewModel.bookConsultationRequest.bookingId.toString(),
                        promotionId = it.promotionId
                    )
                    removePackageApi(request)
                }
            )
        }
    }

    private lateinit var dialogAddPromoBinding: DialogAddPromoBinding
    private lateinit var dialogSaveButton: Button
    private fun showAddPromoDialog() {
        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogAddPromoBinding = DialogAddPromoBinding.inflate(layoutInflater)
            dialogAddPromoBinding.etPromoCode.hint =
                mBinding.lang?.globalString?.promoCode.getSafe()
            setView(dialogAddPromoBinding.root)
            setTitle(mBinding.lang?.dialogsStrings?.addPromotion.getSafe())
            setPositiveButton(mBinding.lang?.globalString?.add.getSafe()) { _, _ ->

            }
            setNegativeButton(mBinding.lang?.globalString?.cancel.getSafe(), null)

            dialogAddPromoBinding.apply {
                etPromoCode.mBinding.editText.doAfterTextChanged {
                    dialogSaveButton.isEnabled = isValid(etPromoCode.text)
                }
            }
        }.create()

        builder.setOnShowListener {
            dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                val promo = dialogAddPromoBinding.etPromoCode.text
                isPromoError = true
                val request = AppointmentDetailReq(
                    bookingId = checkoutViewModel.bookConsultationRequest.bookingId.toString(),
                    promoCode = promo
                )
                checkoutDetails(request)
                builder.dismiss()
            }
            dialogSaveButton.isEnabled = false

        }
        builder.show()
    }

    private lateinit var dialogAddLinkedBinding: DialogLinkedCreditBinding
    private fun showAddLinkedCreditDialog(item: LinkedCredit) {
        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogAddLinkedBinding = DialogLinkedCreditBinding.inflate(layoutInflater)

            setView(dialogAddLinkedBinding.root)
            setTitle("${item.name} ${mBinding.lang?.checkoutScreen?.credit.getSafe()}") //getString(R.string._credit, item.name)
            dialogAddLinkedBinding.tvDesc.text =
                mBinding.lang?.dialogsStrings?.creditDescription.getSafe()
                    .replace("[0]", item.amount.toString()).replace(
                        "[1]",
                        item.name.toString()
                    ) //getString(R.string.m_credit_, item.amount, item.name)
            setPositiveButton(mBinding.lang?.globalString?.add.getSafe()) { _, _ ->
                builder.dismiss()
            }
            setNegativeButton(mBinding.lang?.globalString?.cancel.getSafe(), null)

            dialogAddLinkedBinding.apply {
                val pay = total.getCommaRemoved().toDouble()
                    //checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.total.getCommaRemoved()
                      //  .getSafe()
                etAmount.mBinding.editText.doAfterTextChanged { amount ->
                    if (amount?.isNotEmpty().getSafe() && amount?.get(0) != '.') {
                        val isValid = amount?.toString()?.toDouble().getSafe() == 0.0
                        dialogSaveButton.isEnabled = isValid.not() && amount.toString().toDouble()
                            .getSafe() <= pay.toDouble() && amount.toString().toDouble()
                            .getSafe() <= item.discount.getCommaRemoved().toDouble().getSafe()
                        if ((isValid.not() && amount.toString().toDouble()
                                .getSafe() <= pay.toDouble() && amount.toString().toDouble()
                                .getSafe() <= item.discount.getCommaRemoved().toDouble()
                                .getSafe()).not()
                        ) {
                            etAmount.errorText =
                                mBinding.lang?.dialogsStrings?.packageValidation.getSafe()

                            return@doAfterTextChanged
                        }
                    } else dialogSaveButton.isEnabled = false
                }
            }
        }.create()

        builder.setOnShowListener {
            dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                val request = AppointmentDetailReq(
                    promotionId = packageId,
                    bookingId = checkoutViewModel.bookConsultationRequest.bookingId.toString(),
                    amount = dialogAddLinkedBinding.etAmount.text.toString()
                )
                applyPackageApi(request)
                builder.dismiss()
            }
            dialogSaveButton.isEnabled = false

        }
        builder.show()
    }

    private fun showPaymentFailDialog() {
        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            val dialogFailBinding = DialogPaymentFailBinding.inflate(layoutInflater)

            setView(dialogFailBinding.root)
            setTitle(mBinding.lang?.dialogsStrings?.paymentDeclined.getSafe())
            setPositiveButton(mBinding.lang?.globalString?.ok.getSafe()) { _, _ ->
                builder.dismiss()
                findNavController().safeNavigate(CheckoutFragmentDirections.actionCheckoutFragmentToCheckoutConfirmationFragment())

            }
        }.create()
        builder.show()
    }

    private fun checkoutDetails(request: AppointmentDetailReq, deletePromo: Boolean? = null) {
        if (isOnline(requireActivity())) {
            checkoutViewModel.checkoutDetails(request).observe(viewLifecycleOwner) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<CheckoutDetailResponse>
                        response.data?.let { checkoutDetail ->
                            checkoutViewModel.checkoutDetailResponse = checkoutDetail
                            if (checkoutViewModel.bookConsultationRequest.patientId == null) { // coming from order detail else bdc se data coming
                                checkoutViewModel.bookConsultationRequest.apply {
                                    patientId = checkoutDetail.userId
                                    bookingDate = getLongDateFromString(
                                        checkoutDetail.bookingDate.getSafe(),
                                        "yyyy-MM-dd hh:mm:ss"
                                    ).toString()
                                    startTime = checkoutDetail.startTime
                                    endTime = checkoutDetail.endTime
                                    userLocationId = checkoutDetail.userLocationId
                                    fee = checkoutDetail.fee
                                    instructions = checkoutDetail.instructions
                                    serviceId = checkoutDetail.partnerServiceId
                                }
                            }
                            setDataInViews()

                            if (deletePromo.getSafe())
                                promotionId = 0
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
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
                    else -> {
                        hideLoader()
                    }
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

    private fun initWebView() {
        mBinding.apply {
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }

            val webViewClient = CheckoutWebViewClient(
                onPageLoadStarted = {
                    showLoader()
                },
                onPageLoadFinish = {
                    hideLoader()
                }
            )
            webView.webViewClient = webViewClient
        }
    }

    private fun getPaymobToken() {
        if (isOnline(requireActivity())) {
            checkoutViewModel.getPaymobToken().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<PaymobTokenResponse>
                        response.data?.let {
                            if (it.token.isNullOrEmpty().not()) {

                                val fee =
                                    if (fees == mBinding.lang?.globalString?.free.getSafe() || fees.isEmpty()) "0" else (fees.parseDouble() * 100).toString()

                                val request = PaymobPaymentRequest(
                                    authToken = it.token,
                                    amountCents = fee
                                )
                                paymobOrders(request)
                            }
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
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
                        showLoader()
                    }
                    else -> {
                        hideLoader()
                    }
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

    private fun paymobOrders(request: PaymobPaymentRequest) {
        if (isOnline(requireActivity())) {
            checkoutViewModel.paymobOrders(request).observe(this) { it ->
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<PaymobOrderResponse>
                        if (((checkoutViewModel.partnerSlotsResponse.fee == "Free").not() || (checkoutViewModel.partnerSlotsResponse.currencyId == 0).not()) && paymentIntegrationId != 0) {
                            response.data?.let { paymentOrder ->
                                if (paymentOrder.id != null) {
                                    request.apply {
                                        orderId = paymentOrder.id
                                        integrationId = paymentIntegrationId
                                    }
                                    paymobPayment(request)
                                }
                            }
                        } else {
                            findNavController().safeNavigate(CheckoutFragmentDirections.actionCheckoutFragmentToCheckoutConfirmationFragment())
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
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
                        showLoader()
                    }
                    else -> {
                        hideLoader()
                    }
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

    private fun paymobPayment(request: PaymobPaymentRequest) {
        if (isOnline(requireActivity())) {
            checkoutViewModel.paymobPayment(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<PaymobPaymentResponse>
                        response.data?.let {
                            it.iframe?.let { url ->
                                mBinding.webView.apply {
                                    loadUrl(url)
                                    visible()
                                }
                            }
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
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
                        showLoader()
                    }
                    else -> {
                        hideLoader()
                    }
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

    private fun paymobPaymentStatus(request: PaymobPaymentStatusRequest) {
        if (isOnline(requireActivity())) {
            checkoutViewModel.paymobPaymentStatus(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        if (request.status == 0) {
                            showPaymentFailDialog()
                        } else {
                            ApplicationClass.localeManager.updateLocaleData(
                                requireContext(),
                                TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
                            )

//                        val response = it.data as ResponseGeneral<PaymobPaymentStatusRequest>
                            findNavController().safeNavigate(CheckoutFragmentDirections.actionCheckoutFragmentToCheckoutConfirmationFragment())
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
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
                    else -> {
                        hideLoader()
                    }
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

    private fun applyPackageApi(request: AppointmentDetailReq) {
        if (isOnline(requireActivity())) {
            checkoutViewModel.applyPackage(request).observe(viewLifecycleOwner) {
                when (it) {
                    is ResponseResult.Success -> {
                        checkoutDetails(
                            AppointmentDetailReq(
                                bookingId = checkoutViewModel.bookConsultationRequest.bookingId.toString(),
                                promoCode = if (promotionId != 0) promotionId.toString() else null
                            )
                        )
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.warning.getSafe(),
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
                    else -> {
                        hideLoader()
                    }
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

    private fun removePackageApi(request: AppointmentDetailReq) {
        if (isOnline(requireActivity())) {
            checkoutViewModel.removePackage(request).observe(viewLifecycleOwner) {
                when (it) {
                    is ResponseResult.Success -> {
                        checkoutDetails(
                            AppointmentDetailReq(
                                bookingId = checkoutViewModel.bookConsultationRequest.bookingId.toString(),
                                promoCode = if (promotionId != 0) promotionId.toString() else null
                            )
                        )
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
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
                    else -> {
                        hideLoader()
                    }
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