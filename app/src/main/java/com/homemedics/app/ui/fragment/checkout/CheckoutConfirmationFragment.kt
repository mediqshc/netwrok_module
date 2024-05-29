package com.homemedics.app.ui.fragment.checkout

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.appointments.AppointmentDetailReq
import com.fatron.network_module.models.request.checkout.SplitAmount
import com.fatron.network_module.models.response.checkout.CheckoutDetailResponse
import com.fatron.network_module.models.response.checkout.Promotions
import com.fatron.network_module.models.response.labtest.LabTestResponse
import com.fatron.network_module.models.response.pharmacy.Product
import com.fatron.network_module.models.response.user.UserResponse
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentCheckoutConfirmationBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.adapter.CheckoutSplitAmountAdapter
import com.homemedics.app.ui.adapter.PromotionsAdapter
import com.homemedics.app.ui.custom.ConcaveRoundedCornerTreatment
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.CheckoutViewModel
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class CheckoutConfirmationFragment : BaseFragment(), View.OnClickListener {

    private val checkoutViewModel: CheckoutViewModel by activityViewModels()
    private lateinit var mBinding: FragmentCheckoutConfirmationBinding
    private lateinit var builder: AlertDialog
    private var appliedPackageAmount = 0.00
    private val user: UserResponse? by lazy {
        DataCenter.getUser()
    }
    private val start = "\u2066"
    private val end = "\u2069"
    private val hash = "\u0023"

    override fun setLanguageData() {
        hideLoader()
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.checkoutScreen?.orderConfirmation.getSafe()
        }
    }

    override fun init() {
        tinydb.remove(Enums.TinyDBKeys.BOOKING_ID.key)
        tinydb.remove(Enums.TinyDBKeys.LAB_TEST_BOOKING_ID.key)
        setCornersTreatment()
        setDataInViews()
    }

    override fun getFragmentLayout() = R.layout.fragment_checkout_confirmation

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentCheckoutConfirmationBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                onAction2Click = {
                    clScreenShot.background = ContextCompat.getDrawable(requireContext(), R.drawable.gradient_primary_shades)
                    val uri = screenShot(clScreenShot)
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "image/*"
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    startActivity(Intent.createChooser(shareIntent, "Choose to share"))
                }
            }

            bViewDetails.setOnClickListener(this@CheckoutConfirmationFragment)
            bSave.setOnClickListener(this@CheckoutConfirmationFragment)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bViewDetails -> {
                checkoutViewModel.fromCheckout = true
                checkoutViewModel.bookedOrderId = checkoutViewModel.bookConsultationRequest.bookingId
                findNavController().safeNavigate(CheckoutConfirmationFragmentDirections.actionCheckoutConfirmationFragmentToOrdersNavigation())
            }
            R.id.bSave -> {
                mBinding.clScreenShot.background = ContextCompat.getDrawable(requireContext(), R.drawable.gradient_primary_shades)
                screenShot(mBinding.clScreenShot)
                showToast(mBinding.lang?.checkoutScreen?.screenShot.getSafe())
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        checkoutViewModel.flushData()
    }

    @SuppressLint("SetTextI18n")
    private fun setDataInViews(){
        var total = checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.total.getSafe()
        if ( total.getCommaRemoved().toDouble() !=0.00  && checkoutViewModel.checkoutDetailResponse.availablePackages?.isNotEmpty()==true) {
            try {
                // Using toDouble() - may throw NumberFormatException
                var amountPayable = (total.getCommaRemoved()
                    .toDouble() + checkoutViewModel.checkoutDetailResponse.promotions?.discountValue.getCommaRemoved()
                    .toDouble())
                println("Converted value using toDouble(): $amountPayable")
                total = amountPayable.toString()
            } catch (e: NumberFormatException) {
                println("Conversion to double failed: ${e.message}")
            }
        }
        else if (checkoutViewModel.checkoutDetailResponse.availablePackages?. isNotEmpty() == true || checkoutViewModel.checkoutDetailResponse.packages?.isNotEmpty()==true) {
            val subTotal =
                checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.subTotal.getCommaRemoved()
                    .toDouble()
            checkoutViewModel.checkoutDetailResponse.packages?.forEach {
                appliedPackageAmount = it.discountValue.getCommaRemoved().parseDouble()
            }
            total = subTotal.toString()
            var amountEnterFromPackage=0.00
            var discountAmount=0.00
              if (checkoutViewModel.checkoutDetailResponse.promotions?.discountValue.isNullOrEmpty() ){
                  discountAmount=0.00
              }
            else {
                  discountAmount =
                      checkoutViewModel.checkoutDetailResponse.promotions?.discountValue.getCommaRemoved()
                          .toDouble()
              }
            checkoutViewModel.checkoutDetailResponse.packages?.forEach {
                amountEnterFromPackage= it.discountValue?.toDouble()!!
            }
            amountEnterFromPackage = amountEnterFromPackage.plus(discountAmount)
            total= total.getCommaRemoved().toDouble().minus(amountEnterFromPackage).toString()
        }
        else
        {
            total = (checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.total.getSafe())
        }
        val locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        val services = metaData?.partnerServiceType?.find { it.id == checkoutViewModel.checkoutDetailResponse.partnerServiceId }
        val currency = metaData?.currencies?.find { it.genericItemId == checkoutViewModel.checkoutDetailResponse.currencyId }?.genericItemName.getSafe()
        val isHHC = checkoutViewModel.checkoutDetailResponse.partnerServiceId == CustomServiceTypeView.ServiceType.HealthCare.id
        val isPharmacy = checkoutViewModel.checkoutDetailResponse.partnerServiceId == CustomServiceTypeView.ServiceType.PharmacyService.id
        val isLab = checkoutViewModel.bookConsultationRequest.serviceId == CustomServiceTypeView.ServiceType.LaboratoryService.id
        val serviceName = metaData?.partnerServiceType?.find { it.id == checkoutViewModel.checkoutDetailResponse.partnerServiceId.getSafe() }
        val hhc = metaData?.specialties?.medicalStaffSpecialties?.find {
            it.genericItemId == checkoutViewModel.checkoutDetailResponse.specialityId
        }?.genericItemName

        val timeFormat = if (DateFormat.is24HourFormat(getAppContext()))
            getString(R.string.timeFormat24) else getString(R.string.timeFormat12)
        val dateStamp: String = SimpleDateFormat("dd MMM yyyy").format(Calendar.getInstance().time)
        val timeStamp: String = SimpleDateFormat(timeFormat).format(Calendar.getInstance().time)


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


        mBinding.apply {
            tvPatient.text = "$start${lang?.checkoutScreen?.forString.getSafe()} ${checkoutViewModel.checkoutDetailResponse.bookedForUser?.fullName}$end"
            tvService.text = if (isHHC) hhc else if (isPharmacy || isLab) serviceName?.shortName else serviceName?.name
            tvDate.text = "${lang?.globalString?.date.getSafe()}: $dateStamp $start$timeStamp$end"
            tvOrderNo.text = "${lang?.checkoutScreen?.orderNo.getSafe()} $hash ${checkoutViewModel.checkoutDetailResponse.uniqueIdentificationNumber}"

            val service = metaData?.partnerServiceType?.find { it.id == checkoutViewModel.checkoutDetailResponse.partnerServiceId }?.name

            val fee = if (checkoutViewModel.checkoutDetailResponse.fee != null) checkoutViewModel.checkoutDetailResponse.fee.toString() else lang?.globalString?.free.getSafe()
            val subTotal = checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.subTotal


            val splitAmountAdapter = CheckoutSplitAmountAdapter()
            var orderDetails = arrayListOf<SplitAmount>()
            orderDetails = if (checkoutViewModel.bookConsultationRequest.serviceId == CustomServiceTypeView.ServiceType.HealthCare.id) { //home healthcare
                checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.duties.getSafe()
            } else {
                val serviceCharges = "$service ${lang?.globalString?.charges.getSafe()}"
                arrayListOf(
                    SplitAmount(specialityName = serviceCharges, fee = "$subTotal")
                )
            }

            splitAmountAdapter.listItems = orderDetails
            rvSplitAmount.adapter = splitAmountAdapter

            promoAdapter = PromotionsAdapter().apply {
                isConfirmation = true
            }
            populatePromotions(checkoutViewModel.checkoutDetailResponse)
            populatePackages(checkoutViewModel.checkoutDetailResponse)

            if (checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.subTotal != null) {
                tvPayableAmountStrip.text = "$subTotal"
                tvPayableAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${total.getCommaRemoved().toDouble().round(2)} $currency" else "$currency ${total.getCommaRemoved().toDouble().round(2)}"
            } else {
                tvPayableAmountStrip.text = "$subTotal"
                tvPayableAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${total.getCommaRemoved().toDouble().round(2)} $currency" else "$currency ${total.getCommaRemoved().toDouble().round(2)}"
            }

            // Labs test payment break
            val breakDown = checkoutViewModel.checkoutDetailResponse.paymentBreakdown
            if (services?.id == CustomServiceTypeView.ServiceType.LaboratoryService.id) {
                val sampleCharges = "${checkoutViewModel.checkoutDetailResponse.paymentBreakdown?.sampleCharges}"
                val labList = arrayListOf<LabTestResponse>()
                breakDown?.labs?.map { lab ->
                    lab.labTest?.genericItemName = lab.labTest?.genericItemName
                    lab
                }
                breakDown?.labs?.let { labList.addAll(it) }

                val splitAmount = arrayListOf<SplitAmount>()
                labList.forEach { lab ->
                    splitAmount.add(
                        SplitAmount(
                            specialityName = lab.labTest?.genericItemName,
                            fee = lab.totalFee?.toString()
                        )
                    )
                }

                splitAmount.apply {
                    add(SplitAmount(specialityName = mBinding.lang?.checkoutScreen?.homeCollectionCharges.getSafe(), fee = sampleCharges))
                    if (breakDown?.corporateDiscount != null)
                        add(
                            SplitAmount(
                                specialityName = mBinding.lang?.globalString?.corporateDiscount.getSafe(),
                                fee = "$currency ${breakDown.corporateDiscount.toString()}"
                            )
                        )

                    if (breakDown?.promoDiscount != null)
                        add(
                            SplitAmount(
                                specialityName = mBinding.lang?.globalString?.promoDiscount.getSafe(),
                                fee = "$currency ${breakDown.promoDiscount.toString()}"
                            )
                        )

                    if (breakDown?.companyCredit != null)
                        add(
                            SplitAmount(
                                specialityName = mBinding.lang?.globalString?.companyDiscount.getSafe(),
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
                    tvPayableAmountStrip.text = "$subTotal"
                    tvPayableAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${total.getCommaRemoved().toDouble().round(2)} $currency" else "$currency ${total.getCommaRemoved().toDouble().round(2)}"
                }
            }
            // Pharmacy payment break
            if (services?.id == CustomServiceTypeView.ServiceType.PharmacyService.id) {
                val pharmacyList = arrayListOf<Product>()
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
                                specialityName = mBinding.lang?.globalString?.corporateDiscount.getSafe(),
                                fee = "$currency ${breakDown.corporateDiscount.toString()}"
                            )
                        )

                    if (breakDown?.promoDiscount != null)
                        add(
                            SplitAmount(
                                specialityName = mBinding.lang?.globalString?.promoDiscount.getSafe(),
                                fee = "$currency ${breakDown.promoDiscount.toString()}"
                            )
                        )

                    if (breakDown?.companyCredit != null)
                        add(
                            SplitAmount(
                                specialityName = mBinding.lang?.globalString?.companyDiscount.getSafe(),
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
                    tvPayableAmountStrip.text = "$subTotal"
                    tvPayableAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${total.getCommaRemoved().toDouble().round(2)} $currency" else "$currency ${total.getCommaRemoved().toDouble().round(2)}"
                }
            }
        }
    }

    private lateinit var promoAdapter: PromotionsAdapter
    private var promoPackageList: ArrayList<Promotions>? = null

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
    }

    private fun populatePackages(checkout: CheckoutDetailResponse?) {
        promoPackageList?.addAll(checkout?.packages.getSafe())

        promoAdapter.listItems = promoPackageList.getSafe()
        mBinding.rvPromotions.apply {
            setVisible(promoPackageList?.isNotEmpty().getSafe())
            adapter = promoAdapter
        }
    }

    private fun setCornersTreatment(){
        mBinding.apply {
            val cutCornerRadius = resources.getDimensionPixelSize(R.dimen.dp12)
            linearLayoutTop.background = MaterialShapeDrawable(
                ShapeAppearanceModel.Builder()
                    .setAllCornerSizes(cutCornerRadius.toFloat())
                    .setBottomLeftCorner(ConcaveRoundedCornerTreatment())
                    .setBottomRightCorner(ConcaveRoundedCornerTreatment())
                    .build()
            ).apply { fillColor = ColorStateList.valueOf(resources.getColor(R.color.white, requireActivity().theme)) }

            linearLayoutBottom.background = MaterialShapeDrawable(
                ShapeAppearanceModel.Builder()
                    .setAllCornerSizes(cutCornerRadius.toFloat())
                    .setTopLeftCorner(ConcaveRoundedCornerTreatment())
                    .setTopRightCorner(ConcaveRoundedCornerTreatment())
                    .build()
            ).apply { fillColor = ColorStateList.valueOf(resources.getColor(R.color.white, requireActivity().theme)) }
        }
    }

    private fun screenShot(view: View): Uri? {
        val bitmap = Bitmap.createBitmap(
            view.width,
            view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        bitmap?.let {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "title")
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            val uri: Uri? = requireActivity().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )


            val outstream: OutputStream?
            uri?.let {
                try {
                    outstream = requireActivity().contentResolver.openOutputStream(uri)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream)
                    outstream?.close()
                } catch (e: Exception) {
                    System.err.println(e.toString())
                }
            }

            return uri
        }

        return null
    }

}