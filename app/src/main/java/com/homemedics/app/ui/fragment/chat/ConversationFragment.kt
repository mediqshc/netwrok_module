package com.homemedics.app.ui.fragment.chat

import android.content.Intent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.ConversationFragmentBinding
import com.homemedics.app.ui.activity.HomeActivity
import com.homemedics.app.ui.adapter.TabsPagerAdapter
import com.homemedics.app.utils.Enums
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.gone
import com.homemedics.app.viewmodel.ChatViewModel

class ConversationFragment  : BaseFragment(), View.OnClickListener {

    private val chatViewModel: ChatViewModel by activityViewModels()
    private lateinit var mBinding: ConversationFragmentBinding
    val langData=ApplicationClass.mGlobalData
    override fun setLanguageData() {
mBinding.actionbar.title=ApplicationClass.mGlobalData?.globalString?.Messages.getSafe()
    }

    override fun getFragmentLayout(): Int = R.layout.conversation_fragment

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as ConversationFragmentBinding
    }

    override fun setListeners() {
        handleBackPress()
       mBinding. actionbar.onAction1Click = {
//           requireActivity(). finish()
           val chatIntent = Intent(requireActivity(), HomeActivity::class.java)
           requireActivity().apply {
               startActivity(chatIntent)
               finish()
           }
        }

    }

    private fun handleBackPress(){
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val chatIntent = Intent(requireActivity(), HomeActivity::class.java)
                requireActivity().apply {
                    startActivity(chatIntent)
                    finish()
                }
            }
        })
    }

    override fun init() {
        initTabs()
    }


    private fun initTabs() {
        mBinding.apply {
            TabsPagerAdapter.fragments = ArrayList<Fragment>().apply {
                add(ChatTabFragment())
                add(ChatTabFragment())
            }

            viewPager.adapter = TabsPagerAdapter(childFragmentManager, lifecycle)
            TabLayoutMediator(
                tabLayout, viewPager
            ) { tab, position ->
                tab.text = when (position) {
                    0 -> langData?.chatScreen?.chat
                    1 -> langData?.globalString?.history
                    else -> {
                        ""
                    }
                }
            }.attach()

            viewPager.isUserInputEnabled = false
            viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    var type = ""
                    when (position) {
                        0 -> {
                            type =langData?.chatScreen?.chat.getSafe()
                            chatViewModel.selectedTab = Enums.ConversationType.CHATS
                            chatViewModel.conversationList.value= arrayListOf()
                        }
                        1 -> {
                            type =langData?.globalString?.history.getSafe()
                            chatViewModel.selectedTab = Enums.ConversationType.HISTORY
                            chatViewModel.conversationList.value= arrayListOf()
                        }
                        else -> {
                            type = ""
                            chatViewModel.selectedTab = Enums.ConversationType.CHATS
                            chatViewModel.conversationList.value= arrayListOf()
                        }
                    }
                    chatViewModel.conversationRequest.type = type.lowercase()

                }
            })
            if (tinydb.getBoolean(Enums.PlannerMode.PLANNER_MODE.key).not()) {
                viewPager.currentItem = 0
                tabLayout.gone()
            }
        }
    }

    override fun onClick(view: View?) {

    }

}