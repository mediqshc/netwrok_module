package com.homemedics.app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseActivity
import com.homemedics.app.databinding.ActivityChatBinding
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ChatViewModel


class ChatActivity : BaseActivity() {
    private lateinit var mBinding: ActivityChatBinding
    private val chatViewModel: ChatViewModel by viewModels()

    override fun getActivityLayout(): Int = R.layout.activity_chat

    override fun getViewBinding() {
        mBinding = binding as ActivityChatBinding
        ApplicationClass.localeManager.updateLocaleData(this, TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key))

        init()
    }



    override fun setClickListeners() {

    }

    override fun onClick(v: View?) {

    }

    private fun init() {
        initNavigation()
       redirectToChat(intent.getStringExtra(Constants.SID),intent.getBooleanExtra("fromOrder",false))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        redirectToChat(
            intent?.getStringExtra(Constants.SID),
            intent?.getBooleanExtra("fromOrder",false)
        )
    }
    private fun redirectToChat(intentString: String?, fromOrder: Boolean?) {
        if (intentString.isNullOrEmpty().not()) {
            findNavController(R.id.fChatNav).safeNavigate(
                R.id.action_chatFragment,
                bundleOf(Constants.SID to intentString,
                     "fromOrder"  to fromOrder)
            )
            intent.removeExtra(Constants.SID)
        }
    }
    private fun initNavigation() {
        val myNavHostFragment: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.fChatNav) as NavHostFragment
        val inflater = myNavHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.chat_navigation)

        myNavHostFragment.navController.graph = graph
    }


    override fun onResume() {
        super.onResume()
        try {
            fetchAndSetLanguage()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
        try {
            fetchAndSetLanguage()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}