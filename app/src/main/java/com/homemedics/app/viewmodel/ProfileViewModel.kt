package com.homemedics.app.viewmodel

import android.os.CountDownTimer
import android.widget.TextView
import androidx.lifecycle.*
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.email.EmailSendRequest
import com.fatron.network_module.models.request.email.EmailVerifyRequest
import com.fatron.network_module.models.request.family.FamilyConnectionActionRequest
import com.fatron.network_module.models.request.family.FamilyRequest
import com.fatron.network_module.models.request.linkaccount.CompanyRequest
import com.fatron.network_module.models.request.linkaccount.DeleteLinkRequest
import com.fatron.network_module.models.request.linkaccount.LinkCompanyRequest
import com.fatron.network_module.models.request.linkaccount.LinkInsuranceRequest
import com.fatron.network_module.models.request.notification.NotificationReadRequest
import com.fatron.network_module.models.request.partnerprofile.*
import com.fatron.network_module.models.request.user.UserLocation
import com.fatron.network_module.models.request.user.UserRequest
import com.fatron.network_module.models.response.family.FamilyResponse
import com.fatron.network_module.models.response.linkaccount.*
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.partnerprofile.Services
import com.fatron.network_module.models.response.user.UserResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.R
import com.homemedics.app.model.AddressModel
import com.homemedics.app.model.ContactItem
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.getString
import okhttp3.MultipartBody
import retrofit2.http.Part

class ProfileViewModel: ViewModel() {
    var genderId = MutableLiveData<Int>()
    var educationDocument = ArrayList<MultipleViewItem>()
    val contacts = ArrayList<ContactItem>()

    val addresses = ArrayList<AddressModel>()
    var _address = MutableLiveData<AddressModel>()
    var pickedAddressModel = AddressModel()
    val address: LiveData<AddressModel> = _address
    var _lat = MutableLiveData<Double>()
    val lat: LiveData<Double> get() = _lat
    var isFromMenu=0
    var _licenseCode = MutableLiveData<String>()
    val licenseCode: LiveData<String> get() = _licenseCode

    var _yearsOfExperience = MutableLiveData<String>()
    val yearsOfExperience: LiveData<String> get() = _yearsOfExperience
    var _cnic = MutableLiveData<String>()
    val cnic: LiveData<String> get() = _cnic
    var _overview = MutableLiveData<String>()
    val overview: LiveData<String> get() = _overview


    var _userProfile = MutableLiveData<UserResponse>()
    val userProfile: LiveData<UserResponse> get() = _userProfile
    var _lon = MutableLiveData<Double>()
    val lon: LiveData<Double> get() = _lon
    val familyConnections = MutableLiveData(FamilyResponse())

    var specialityType: Int = 0
    var selectedSpecialties = ArrayList<MultipleViewItem>()
    var educationList = ArrayList<MultipleViewItem>()
    var selectedWorkExperience = ArrayList<MultipleViewItem>()
    var selectedSpecialitiesInt: List<Int>? = null
    var specialties = ArrayList<GenericItem>()
    var serviceList = ArrayList<Services>()

    var partnerType:String=""
    var selectedInsurance: CompanyResponse? = null
    var linkAccountItem: MultipleViewItem? = null
    var companiesList = MutableLiveData<ArrayList<CompanyResponse>>(arrayListOf())
    var hospitalsList = MutableLiveData<ArrayList<CompanyResponse>>(arrayListOf())
    var insuranceCompanyList = MutableLiveData<ArrayList<CompanyResponse>>(arrayListOf())
    var insuranceDataFields = ArrayList<InsuranceFields>()
    var notificationCountLiveData = MutableLiveData<Int>()

    var moveFamilyTabsToIndex = -1

    fun flushDataOnLogout(){
        genderId = MutableLiveData<Int>()
        educationDocument = ArrayList()
        _address = MutableLiveData<AddressModel>()
        _lat = MutableLiveData<Double>()

        _licenseCode = MutableLiveData<String>()

        _yearsOfExperience = MutableLiveData<String>()
        _cnic = MutableLiveData<String>()
        _overview = MutableLiveData<String>()

        _userProfile = MutableLiveData<UserResponse>()
        _lon = MutableLiveData<Double>()

        specialityType = 0
        selectedSpecialties = ArrayList()
        educationList = ArrayList()
        selectedWorkExperience = ArrayList()
        selectedSpecialitiesInt = null
        specialties = ArrayList()
        serviceList = ArrayList()
    }

    fun flushLinkedAccountsLists(){
        selectedInsurance = null
        isDiscountClick = false
        isTermsConditionsClick = false
        companiesList.value = arrayListOf()
        hospitalsList.value = arrayListOf()
        insuranceCompanyList.value = arrayListOf()
    }

    class ViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel() as T
        }
    }

    fun validation(): Boolean {
            if (address.value?.streetAddress.isNullOrEmpty() || address.value?.category.isNullOrEmpty() || (address.value?.category == "Other" && address.value?.other.isNullOrEmpty())) {
            return false
        }
        return true
    }
    fun createProfileApiCall(request: UserRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.createProfile(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getNotificationsCount(request: NotificationReadRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getNotificationsCount(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getFamilyConnectionsApiCall() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getFamilyConnections()
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun storeUserProfilePic(@Part profile_pic: MultipartBody.Part?) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.storeUserProfilePio(profile_pic)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun storeEducation(educationDocument: ArrayList<MultipartBody.Part>?, educationRequest: EducationRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.storeEducation(document = educationDocument,educationRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun storePartnerCnic(@Part cnic_front: MultipartBody.Part?, @Part cnic_back: MultipartBody.Part?,update:String ) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.storePartnerCnic(cnic_front, cnic_back,update)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun storePartnerDetails(partnerDetailsRequest: PartnerDetailsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.storePartnerDetails(partnerDetailsRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getPartnerDetails() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getPartnerDetails()
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun storeSpecialities(specialitiesRequest: SpecialitiesRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.storeSpecialities(specialitiesRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun createFamilyConnectionApiCall(request: FamilyRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.createFamilyConnection(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteFamilyConnectionApiCall(request: FamilyConnectionActionRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteFamilyConnection(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun connectFamilyConnection(request: FamilyConnectionActionRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.connectFamilyConnection(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    //company
    fun getCooperateCompList() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getCooperateCompList()
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun linkCompany(request: LinkCompanyRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.linkCompany(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteCompanyLink(request: DeleteLinkRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteCompanyLink(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    //insurances
    fun getInsuranceCompList() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getInsuranceCompList()
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun linkInsuranceComp(request: LinkInsuranceRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.linkInsuranceComp(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteInsuranceComp(request: DeleteLinkRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteInsuranceComp(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    //hospitals
    fun getHospitalList() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getHospitalList()
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun linkHospital(request: LinkCompanyRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.linkHospital(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteHospital(request: DeleteLinkRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteHospital(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getLinkedAccounts(request: CompanyRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getLinkedAccounts(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun sendEmail(emailSendRequest: EmailSendRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.sendEmail(emailSendRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun verifyEmail(emailVerifyRequest: EmailVerifyRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.verifyEmail(emailVerifyRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun startTimer(view: TextView, onFinish: () -> Unit): CountDownTimer {
        return object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsInMilli: Long = 1000
                val elapsedSeconds = millisUntilFinished / secondsInMilli
                view.text = "$elapsedSeconds ${getString(R.string.sec)}"
            }

            override fun onFinish() {
                onFinish.invoke()
            }
        }.start()
    }
    fun callAddWorkExpApi(request: WorkExperience) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.callAddWorkExpApi(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun calldeletePartnerWork(request: deletePartnerWork) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.calldeletePartnerWork(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
    fun calldeleteEducation(request: deletePartnerEducation) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.calldeleteEducApi(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
    fun calldeleteSpeciality(request: deletePartnerSpecialityReq) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.calldeleteSpeciality(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
    fun storePartnerService(serviceRequest: ServiceRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.storePartnerService(serviceRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    //save address
    fun saveAddressCall(userLocation:UserLocation) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.saveAddressCall(userLocation)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getLocationCategory(
        locationCategories: List<GenericItem>?,
        category: String?,
        other: String?
    ):String{
        val locCategory = locationCategories?.find {loc-> loc.genericItemId == category?.toInt().getSafe() }
       var categoryLoc=""
        if (locCategory!=null)
            categoryLoc=locCategory.genericItemName.getSafe()

        if (categoryLoc.uppercase().getSafe().contains("other".uppercase().getSafe()))
            categoryLoc=  other.getSafe()

        return categoryLoc
    }


    var companiesServicesListLiveData = MutableLiveData<ArrayList<CompaniesService>>()
    var companiesDiscountsListLiveData = MutableLiveData<ArrayList<CompaniesDiscounts>>()
    var companyServices: CompaniesService? = null
    var companyDiscount: CompaniesDiscounts? = null
    var companyTermsAndCondition: CompanyTermsAndConditionsResponse? = null
    var excludedItems: ArrayList<String> = arrayListOf()
    var excludedCategories: ArrayList<String> = arrayListOf()
    var doctors: List<Map<String, Any>>? = null
    var specialitiesExclusion: ArrayList<String> = arrayListOf()
    var isService = false
    var isDiscountClick = false
    var isTermsConditionsClick = false

    fun getCompaniesServices(request: CompanyRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getCompaniesServices(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getCompaniesDiscounts(request: CompanyRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getCompaniesDiscounts(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getCompaniesTermsAndConditions(request: CompanyRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getCompaniesTermsAndConditions(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun verifyCompaniesEmail(request: EmailSendRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.verifyCompaniesEmail(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun companiesVerificationCode(request: EmailVerifyRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.companiesVerificationCode(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
}