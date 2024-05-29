package com.homemedics.app.ui.fragment.home

import android.content.Intent
import android.os.Handler
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.fatron.network_module.models.request.linkaccount.CompanyRequest
import com.fatron.network_module.models.request.notification.NotificationReadRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.linkaccount.CompanyResponse
import com.fatron.network_module.models.response.linkaccount.LinkedAccountsResponse
import com.fatron.network_module.models.response.meta.HomeMenuItem
import com.fatron.network_module.models.response.notification.NotificationCountResponse
import com.fatron.network_module.models.response.user.UserResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentHomeBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.activity.AuthActivity
import com.homemedics.app.ui.activity.EMRActivity
import com.homemedics.app.ui.activity.HomeActivity
import com.homemedics.app.ui.adapter.HomeOptionsAdapter
import com.homemedics.app.ui.adapter.HomeSliderPagerAdapter
import com.homemedics.app.ui.adapter.LinkedAccountChipViewAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.DoctorConsultationViewModel
import com.homemedics.app.viewmodel.HomeViewModel
import com.homemedics.app.viewmodel.ProfileViewModel
import kotlin.concurrent.timer

class HomeFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val doctorConsultationViewModel: DoctorConsultationViewModel by activityViewModels()
    private var user: UserResponse? = null
    lateinit var view_Pager: ViewPager2
    lateinit var handler: Handler
    lateinit var autoSlideRunnable: Runnable

    //  var image_banner_list= arrayListOf(R.drawable.certified_doctors,R.drawable.lab_sample,R.drawable.medicines_delivery)


    // var currentPage=0
    // val delay : Long =6000


    private val linkedAccountsAdapter = LinkedAccountChipViewAdapter()
    private var langData: RemoteConfigLanguage? = null

    override fun setLanguageData() {
        hideLoader()
        langData = ApplicationClass.mGlobalData
        mBinding.apply {
            tvGreetMsg1.text = langData?.homeScreen?.helloGreet
            tvMyConnections.text = langData?.tabString?.myConnections
            tvEMR.text = langData?.homeScreen?.emr
            tvMyOrders.text = langData?.myOrdersScreens?.myOrders
            tvMyClaims.text = langData?.claimScreen?.myClaims
            view_Pager = viewPager


        }
    }

    override fun init() {
        user = DataCenter.getUser()
        Handler()
        // setUpViewPager()
        if (tinydb.getBoolean(Enums.PlannerMode.PLANNER_MODE.key)) {
            showDoctorView()
        } else {
            patientView()
        }
        homeViewModel.isDoctorViewEnabled.observe(viewLifecycleOwner) {
            setupview(it)


        }
        val spacing = resources.getDimensionPixelSize(R.dimen.dp4)
        mBinding.rvLinkedAccounts.addItemDecoration(
            RecyclerViewItemDecorator(
                spacing,
                RecyclerViewItemDecorator.HORIZONTAL
            )
        )

        val optionsAdapter = HomeOptionsAdapter()
        val menuActiveList = metaData?.homeMenuItems?.filter { it.isActive.getSafe() }
        if (menuActiveList?.size?.mod(2) != 0) {//odd
            //grid ui fixing item
            (menuActiveList as ArrayList).add(HomeMenuItem(id = -1))
        }

        optionsAdapter.listItems = menuActiveList.map {
            when (it.id) {
                1 -> {
                    it.icon = R.drawable.opt_home_doctor
                }
                2 -> {
                    it.icon = R.drawable.opt_home_pharmacy
                }
                3 -> {
                    it.icon = R.drawable.opt_home_lab
                }
                4 -> {
                    it.icon = R.drawable.opt_home_healthcare
                }
                5 -> {
                    it.icon = R.drawable.opt_home_packages
                    it.isEnabled = isUserLoggedIn() //temporary disable
                }
                6 -> {
                    it.icon = R.drawable.opt_home_claim
                }
                -1 -> { /* grid ui fixing item, ignore it */
                }
                else -> {
                    //  it.icon = R.drawable.ic_opt_home_packages
                }
            }
            it
        }.getSafe()

        optionsAdapter.itemClickListener = { item, _ ->
            when (item.id) {
                1 -> findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToDoctorConsultationNavigation())
                2 -> findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToPharmacyNavigation())
                3 -> findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToLabTestNavigation())
                4 -> findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToHomeServiceNavigation())
                5 -> {
                    if (isUserLoggedIn())
                        findNavController().safeNavigate(
                            R.id.action_home_to_packagesFragment_navigation,
                            bundleOf(
                                "navigateFrom" to "Home"
                            )
                        )
                }
                6 -> {
                    if (isUserLoggedIn() && homeViewModel.linkedAccounts.isEmpty()) {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = langData?.globalString?.information.getSafe(),
                                message = langData?.dialogsStrings?.notLinkedToCorporate.getSafe()
                            )
                    } else {
                        findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToClaimNavigation())
                    }
                }
            }

        }

        val imageSliderAdapter = HomeSliderPagerAdapter()
        imageSliderAdapter.listItems = DataCenter.getHomeSliderImages()

        mBinding.apply {
            setGreetingText()

            rvOptions.adapter = optionsAdapter
            val spacing = resources.getDimensionPixelSize(R.dimen.dp1)
            rvOptions.addItemDecoration(
                RecyclerViewItemDecorator(
                    spacing,
                    RecyclerViewItemDecorator.GRID
                )
            )
            viewPager.apply {
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                adapter = imageSliderAdapter
            }
            dotsIndicator.attachTo(viewPager)

            var currentPage = 0
            timer(initialDelay = 14000L, period = 14000L) {
                if (currentPage < mBinding.viewPager.adapter?.itemCount.getSafe() - 1)
                    currentPage++
                else currentPage = 0

                try {
                    mBinding.viewPager.setCurrentItem(currentPage, true)
                } catch (e: Exception) {
                    currentPage = 0
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_home

    override fun getViewModel() {
        observe()

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentHomeBinding

    }

    public fun setupview(isChecked: Boolean) {
        if (isChecked) {
            mBinding.llBottomBar?.visibility = View.GONE
            showDoctorView()
        } else {
            mBinding.llBottomBar?.visibility = View.VISIBLE
            patientView()
        }
    }

    override fun onResume() {
        super.onResume()
        hideLoader()
        profileViewModel.notificationCountLiveData.observe(this) { count ->
            count?.let {
                mBinding.tvFilterBadge.apply {
                    text = count.toString()
                    setVisible(count > 0 && isUserLoggedIn())
                }
            }
        }
        if (requireActivity() is HomeActivity) {
            setGreetingText()
            (requireActivity() as HomeActivity).enableDrawer(true)
            (requireActivity() as HomeActivity).setUserDetails()
        }
        setLinkedAccountsView(homeViewModel.linkedAccounts)
        getLinkedAccounts()
        getNotificationCount()
        profileViewModel.isTermsConditionsClick = false
        profileViewModel.isDiscountClick = false
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as HomeActivity).enableDrawer(false)
    }

    override fun setListeners() {
        mBinding.apply {
            ivNav.setOnClickListener {
                if (requireActivity() is HomeActivity)
                    (requireActivity() as HomeActivity).openDrawer()
            }
            ivNotifications.setOnClickListener {
                if (isUserLoggedIn()) {
                    findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToNotificationFragment())
                } else {
                    val intent = Intent(requireActivity(), AuthActivity::class.java)
                    startActivity(intent)
                }
            }
            ivProfile.setOnClickListener {
                if (isUserLoggedIn().not())
                    return@setOnClickListener

                profileViewModel.addresses.clear()
                profileViewModel.contacts.clear()
                profileViewModel._userProfile.value =
                    tinydb.getObject(
                        com.fatron.network_module.utils.Enums.TinyDBKeys.USER.key,
                        UserResponse::class.java
                    ) as UserResponse
                findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToPersonalProfileFragment())
            }
            tvMyOrders.setOnClickListener {
                findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToOrdersNavigation())
            }
            tvEMR.setOnClickListener {
                val emrIntent = Intent(requireActivity(), EMRActivity::class.java)
                startActivity(emrIntent)
                requireActivity().finish()
            }
            tvMyClaims.setOnClickListener {
                findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToClaimListNavigation())
            }
            linkedAccountsAdapter.itemClickListener = { _, _ ->
                findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToLinkedAccountNavigation())
            }

            llLoginCard?.setOnClickListener {
                if (requireActivity() is HomeActivity) {
                    (requireActivity() as HomeActivity).gotoLogin()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
//            R.id.tvGreetMsg -> {
//                findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToClaimOrdersDetailFragment())
//            }
            R.id.tvName -> {
                findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToClaimOrdersDetailFragment())
            }

        }
    }

    private fun observe() {
        homeViewModel.homeActivityIntent.observe(this) {
            it?.let {
                val user = DataCenter.getUser()
                user?.let {
                    setGreetingText()
                }
            }
        }
    }

//---------------------------
/*private fun setUpViewPager() {

    view_Pager.adapter = imageViewPagerAdapter(image_banner_list)

    //set the orientation of the viewpager using ViewPager2.orientation
    view_Pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

    //select any page you want as your starting page
    val currentPageIndex = 1
    view_Pager.currentItem = currentPageIndex

    // registering for page change callback
    view_Pager.registerOnPageChangeCallback(
        object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                //update the image number textview

            }
        }


    )
    handler.postDelayed(autoSlideRunnable,delay)
}
    private  fun Handler() {
         handler = Handler(Looper.getMainLooper())
          autoSlideRunnable = object : Runnable {
            override fun run() {
                if (currentPage === image_banner_list.size) {
                    currentPage = 0
                }
                view_Pager.setCurrentItem(currentPage, true)
                currentPage++
                handler.postDelayed(this, delay)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()

        // unregistering the onPageChangedCallback
        view_Pager.unregisterOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {}
        )
    }*/
    // ----------------------------------------------

    private fun patientView() {
        user = DataCenter.getUser()

        val spacing = resources.getDimensionPixelSize(R.dimen.dp0)
        mBinding.rvLinkedAccounts.addItemDecoration(
            RecyclerViewItemDecorator(
                spacing,
                RecyclerViewItemDecorator.HORIZONTAL
            )
        )

        val optionsAdapter = HomeOptionsAdapter()
        val menuActiveList = metaData?.homeMenuItems?.filter { it.isActive.getSafe() }
        if (menuActiveList?.size?.mod(2) != 0) {//odd
            //grid ui fixing item
            (menuActiveList as ArrayList).add(HomeMenuItem(id = -1))
        }

        optionsAdapter.listItems = menuActiveList.map {
            when (it.id) {
                1 -> {
                    it.icon = R.drawable.opt_home_doctor
                }
                2 -> {
                    it.icon = R.drawable.opt_home_pharmacy
                }
                3 -> {
                    it.icon = R.drawable.opt_home_lab
                }
                4 -> {
                    it.icon = R.drawable.opt_home_healthcare
                }
                5 -> {
                    it.icon = R.drawable.opt_home_packages
                    it.isEnabled = isUserLoggedIn()
                }

                6 -> {
                    it.icon = R.drawable.opt_home_claim
                }
                -1 -> { /* grid ui fixing item, ignore it */
                }
                else -> {
                    // it.icon = R.drawable.ic_opt_home_packages
                }
            }
            it
        }
            .getSafe()

        optionsAdapter.itemClickListener = { item, _ ->
            when (item.id) {
                1 -> findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToDoctorConsultationNavigation())
                2 -> findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToPharmacyNavigation())
                3 -> findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToLabTestNavigation())
                4 -> findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToHomeServiceNavigation())
                5 -> {
                    findNavController().safeNavigate(HomeFragmentDirections.actionHomeToPackagesFragmentNavigation())
                }
                6 -> {
                    if (isUserLoggedIn() && homeViewModel.linkedAccounts.isEmpty()) {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = langData?.globalString?.information.getSafe(),
                                message = langData?.dialogsStrings?.notLinkedToCorporate.getSafe()
                            )
                    } else {
                        findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToClaimNavigation())
                    }
                }
            }

        }

        val imageSliderAdapter = HomeSliderPagerAdapter()
        imageSliderAdapter.listItems = DataCenter.getHomeSliderImages()

        mBinding.apply {
            setGreetingText()

            rvOptions.adapter = optionsAdapter
            val spacing = resources.getDimensionPixelSize(R.dimen.dp0)
            rvOptions.addItemDecoration(
                RecyclerViewItemDecorator(
                    spacing,
                    RecyclerViewItemDecorator.GRID
                )
            )
            viewPager.apply {
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                adapter = imageSliderAdapter
            }
            dotsIndicator.attachTo(viewPager)

            var currentPage = 0
            timer(initialDelay = 4000L, period = 8000L) {
                if (currentPage < mBinding.viewPager.adapter?.itemCount.getSafe() - 1)
                    currentPage++
                else currentPage = 0

                try {
                    mBinding.viewPager.setCurrentItem(currentPage, true)
                } catch (e: Exception) {
                    currentPage = 0
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showDoctorView() {
        user = DataCenter.getUser()

        val spacing = resources.getDimensionPixelSize(R.dimen.dp0)
        mBinding.rvLinkedAccounts.addItemDecoration(
            RecyclerViewItemDecorator(
                spacing,
                RecyclerViewItemDecorator.HORIZONTAL
            )
        )

        val optionsAdapter = HomeOptionsAdapter()
        // val menuActiveList = metaData?.homeMenuItems?.filter { it.isActive.getSafe() }
        val menuActiveList: MutableList<HomeMenuItem> = arrayListOf()
        menuActiveList.add(
            HomeMenuItem(
                1,
                "My appointment",
                "View consultation & visits appointments",
                true,
                R.drawable.ic_appointments, true, //temporary disable
            )
        )

        menuActiveList.add(
            HomeMenuItem(
                2,
                "My Planner",
                "Set consultation & clinic timings",
                true,
                R.drawable.ic_planner, true, //temporary disable
            )
        )

        menuActiveList.add(
            HomeMenuItem(
                3,
                "Patient messages",
                "View patient messages & respond",
                true,
                R.drawable.ic_patient, true, //temporary disable
            )
        )

        menuActiveList.add(
            HomeMenuItem(
                4,
                "My Associations",
                "View & set associations with organizations",
                true,
                R.drawable.ic_associations, true, //temporary disable
            )
        )

        optionsAdapter.listItems = menuActiveList.toList() as ArrayList<HomeMenuItem>


        optionsAdapter.itemClickListener = { item, _ ->
            when (item.id) {
                1 -> {
                    doctorConsultationViewModel.bdcFilterRequest.serviceId = 1
                    if (requireActivity() is HomeActivity) {
                        (requireActivity() as HomeActivity).gotoMyAppointments()
                    }
                    // findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToDoctorConsultationNavigation())

                }
                2 -> {
                    doctorConsultationViewModel.bdcFilterRequest.serviceId = 3
                    if (requireActivity() is HomeActivity) {
                        (requireActivity() as HomeActivity).gotoMyPlanner()
                    }
                    //  findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToDoctorConsultationNavigation())
                }
                3 -> {
                    doctorConsultationViewModel.bdcFilterRequest.serviceId = 2
                    if (requireActivity() is HomeActivity) {
                        (requireActivity() as HomeActivity).gotoPatientMessages()
                    }
                    // findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToDoctorConsultationNavigation())
                }
                4 -> {
                    doctorConsultationViewModel.bdcFilterRequest.serviceId = 4
                    if (requireActivity() is HomeActivity) {
                        (requireActivity() as HomeActivity).gotoMyAssociations()
                    }
                    //     findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToDoctorConsultationNavigation())
                }
                5 -> {}
                6 -> {
                    if (isUserLoggedIn() && homeViewModel.linkedAccounts.isEmpty()) {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = langData?.globalString?.information.getSafe(),
                                message = langData?.dialogsStrings?.notLinkedToCorporate.getSafe()
                            )
                    } else {
                        findNavController().safeNavigate(HomeFragmentDirections.actionHomeFragmentToClaimNavigation())
                    }
                }
            }

        }
        val imageSliderAdapter = HomeSliderPagerAdapter()
        imageSliderAdapter.listItems = DataCenter.getHomeSliderImages()

        mBinding.apply {
            setGreetingText()

            rvOptions.adapter = optionsAdapter
            val spacing = resources.getDimensionPixelSize(R.dimen.dp0)
            rvOptions.addItemDecoration(
                RecyclerViewItemDecorator(
                    spacing,
                    RecyclerViewItemDecorator.GRID
                )
            )
            viewPager.apply {
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                adapter = imageSliderAdapter
            }
            dotsIndicator.attachTo(viewPager)
        }
    }


    private fun setGreetingText() {
        val user = DataCenter.getUser()
        val name = if (user == null) "" else user.fullName
        //val greetingText = "${ApplicationClass.mGlobalData?.homeScreen?.helloUser1}"
        //  mBinding.tvGreetMsg.text = greetingText.replace("[0]", name.getSafe())
        mBinding.apply {
            tvName?.text = name.getSafe()
        }
    }

    private fun setGreetingTextForNonCorporate() {
        val user = DataCenter.getUser()
        val name = if (user == null) "" else user.fullName
        val greetingText = "${ApplicationClass.mGlobalData?.homeScreen?.helloUser1}"
        mBinding.tvGreetMsg.text = greetingText.replace("[0]", name.getSafe())
        mBinding.tvGreetMsg.setVisible(true)
        mBinding.tvGreetMsg1.setVisible(true)

    }

    private fun setLinkedAccountsView(list: ArrayList<CompanyResponse>) {
        mBinding.apply {
            rvLinkedAccounts.adapter = linkedAccountsAdapter
            rvLinkedAccounts.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            llConnections.setVisible(list.isNotEmpty())
            llHealthCard?.setVisible(list.isNotEmpty())
            if (requireActivity() is HomeActivity) {
                mBinding.llLoginCard?.setVisible(!(requireActivity() as HomeActivity).isUserLoggedIn())
            }
            if (!list.isNotEmpty()) {
                //set greeting message
                mBinding.apply {
                    imageView4.setImageDrawable(resources.getDrawable(R.drawable.bottom_oval_primary_non_corporate))
                }


                setGreetingTextForNonCorporate()
            } else {
                mBinding.tvGreetMsg.setVisible(false)
                mBinding.tvGreetMsg1.setVisible(false)
                mBinding.imageView4.setImageDrawable(resources.getDrawable(R.drawable.bottom_oval_primary))

            }
        }
    }

    private fun getLinkedAccounts() {
        val request = CompanyRequest(homePage = 0)
        if (isOnline(requireActivity()) && isUserLoggedIn()) {
            profileViewModel.getLinkedAccounts(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<LinkedAccountsResponse>

                        val linkedAccounts = arrayListOf<CompanyResponse>()
                        linkedAccounts.addAll(response.data?.companies.getSafe())
                        linkedAccounts.addAll(response.data?.insurances.getSafe())
                        linkedAccounts.addAll(response.data?.healthcares.getSafe())

                        homeViewModel.linkedAccounts = linkedAccounts
                        linkedAccountsAdapter.listItems = linkedAccounts
                        setLinkedAccountsView(linkedAccounts)

                        //set corporate balance on home screen card
                        response.data?.companies?.firstOrNull()?.let {
                            mBinding.tvBalance?.text =
                                "PKR " + it?.amount?.toInt().getSafe().toString()
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

                    }
                    is ResponseResult.Pending -> {
//                        showLoader()
                    }
                    is ResponseResult.Complete -> {
//                        hideLoader()
                    }
                    else -> {
                        hideLoader()
                    }
                }
            }
        }
    }

    private fun getNotificationCount() {
        val request = NotificationReadRequest(
            userId = DataCenter.getUser()?.id
        )
        if (isOnline(requireActivity()) && isUserLoggedIn()) {
            profileViewModel.getNotificationsCount(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val data = it.data as ResponseGeneral<*>
                        val response = data.data as NotificationCountResponse
                        profileViewModel.notificationCountLiveData.postValue(
                            response.unreadNotifications.getSafe()
                        )
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {}
                    is ResponseResult.Pending -> {}
                    is ResponseResult.Complete -> {}
                    else -> {
                        hideLoader()
                    }
                }
            }
        }
    }
}
