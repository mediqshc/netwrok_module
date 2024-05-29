package com.homemedics.app.ui.fragment.profile

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.user.ProfilePicResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.google.android.material.tabs.TabLayoutMediator
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentPersonalProfileBinding
import com.homemedics.app.ui.adapter.TabsPagerAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.utils.Constants.MEDIA_TYPE_IMAGE
import com.homemedics.app.utils.Constants.PROFILE_PIC
import com.homemedics.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


class PersonalProfileFragment : BaseFragment(), View.OnClickListener {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var mBinding: FragmentPersonalProfileBinding
    private lateinit var fileUtils: FileUtils
    val langData=ApplicationClass.mGlobalData
companion object{
    var notiType:Int=0
}

    override fun setLanguageData() {
mBinding.actionbar.title=langData?.personalprofileBasicScreen?.personalProfile.getSafe()
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_personal_profile

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentPersonalProfileBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                closeKeypad()
                findNavController().popBackStack()
            }
            ivAddThumbnail.setOnClickListener(this@PersonalProfileFragment)
        }
    }

    override fun init() {
        val intent=arguments?.getBoolean("fromNoti")
          notiType=arguments?.getInt("NotiType").getSafe()
        mBinding.apply {
            TabsPagerAdapter.fragments = ArrayList<Fragment>().apply {
                add(BasicProfileFragment())
                add(FamilyProfileFragment())
            }
            viewPager.adapter = TabsPagerAdapter(childFragmentManager, lifecycle)
            viewPager.isUserInputEnabled = false
            TabLayoutMediator(
                tabLayout, viewPager
            ) { tab, position ->
                tab.text = when (position) {
                    0 -> ApplicationClass.mGlobalData?.personalprofileBasicScreen?.basic
                    1 -> ApplicationClass.mGlobalData?.personalprofileBasicScreen?.family
                    else -> {
                        ""
                    }
                }
            }.attach()
        }

        fileUtils = FileUtils()
        fileUtils.init(this)

        val user = DataCenter.getUser()
        if (user?.profilePicture != null) {
            mBinding.ivThumbnail.loadImage(user.profilePicture, getGenderIcon(user.genderId.toString()))
        }
        if(intent.getSafe()){
            CoroutineScope(Dispatchers.Main).launch {
                delay(100)
                mBinding.viewPager.currentItem = 1
            }

            arguments?.clear()
        }
        observe()
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.ivAddThumbnail -> {
                fileUtils.requestPermissions(requireActivity(),true){ result ->
                    uploadMultiParts(result)
                }
            }
        }
    }

    private fun observe() {
        val user = DataCenter.getUser()
        profileViewModel.genderId.observe(this) { genderId ->
            genderId.let { gender ->
                if (user?.profilePicture.isNullOrEmpty()) {
                    mBinding.ivThumbnail.setImageResource(getGenderIcon(gender.toString()))
                }
            }
        }
    }

    private fun uploadMultiParts(result: FileUtils.FileData?) {
        val profileFile: File? = result?.uri.let { uri ->
            uri?.let {
                fileUtils.copyUriToFile(
                    requireContext(),
                    it,
                    fileUtils.getFileNameFromUri(
                        requireContext(), uri
                    ),
                    MEDIA_TYPE_IMAGE
                )
            }
        }
        if (profileFile != null) {
            fileUtils.convertFileToMultiPart(
                profileFile,
                MEDIA_TYPE_IMAGE,
                PROFILE_PIC
            ).let { profilePicture ->
                profileViewModel.storeUserProfilePic(profile_pic = profilePicture).observe(this) {
                    if (isOnline(activity)) {
                        when (it) {
                            is ResponseResult.Success -> {
                                hideLoader()
                                try {
                                    val response = it.data as ResponseGeneral<ProfilePicResponse>
                                    val profilePicResponse = response.data

                                    DialogUtils(requireActivity())
                                        .showSingleButtonAlertDialog(
                                            message = langData?.messages?.personalProfilePictureUploaded.getSafe(),
                                            buttonCallback = {},
                                        )

                                    val user = DataCenter.getUser()
                                    user?.profilePicture = profilePicResponse?.profilePic
                                    if (user != null) {
                                        tinydb.putObject(Enums.TinyDBKeys.USER.key, user)
                                    }
                                    mBinding.ivThumbnail.loadImage(user?.profilePicture, getGenderIcon(user?.genderId.toString()))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            is ResponseResult.Pending -> {
                                showLoader()
                            }
                            is ResponseResult.Failure -> {
                                hideLoader()
                                DialogUtils(requireActivity())
                                    .showSingleButtonAlertDialog(
                                        message = it.error.message.getSafe(),
                                        buttonCallback = {},
                                    )
                            }
                            is ResponseResult.ApiError -> {
                                hideLoader()
                                DialogUtils(requireActivity())
                                    .showSingleButtonAlertDialog(
                                        message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                        buttonCallback = {},
                                    )
                            }
                            is ResponseResult.Complete -> {
                                hideLoader()
                            }
                            else -> {
                                hideLoader()
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


                if (isOnline(requireActivity())) {

                } else {
                    DialogUtils(requireActivity())
                        .showSingleButtonAlertDialog(
                            title =  langData?.errorMessages?.internetError.getSafe(),
                            message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                            buttonCallback = {},
                        )
                }
            }
        }
    }

}