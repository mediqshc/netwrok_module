package com.homemedics.app.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.meta.MetaDataResponse
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.R
import com.homemedics.app.ui.activity.AuthActivity
import com.homemedics.app.utils.showNetworkError

abstract class ComposeBaseFragment : Fragment() {
    protected lateinit var className: String
    protected lateinit var tinydb: TinyDB

    val metaData: MetaDataResponse by lazy {
        TinyDB.instance.getObject(
            Enums.TinyDBKeys.META.key,
            MetaDataResponse::class.java
        ) as MetaDataResponse
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        className = javaClass.name
        tinydb = TinyDB.instance

        getViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ComposeView()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLanguageData()
        init()
        setListeners()
    }

    abstract fun setLanguageData()
    abstract fun init()
    @Composable
    abstract fun ComposeView()
    abstract fun getViewModel()
    abstract fun setListeners()

    //////////////////////////////////////////////////////

    fun showLoader() {
        val activity = activity
        if (activity is BaseActivity) (activity).showLoader()
    }

    fun hideLoader() {
        val activity = activity
        if (activity is BaseActivity) (activity).hideLoader()
    }

    fun closeKeypad() {
        val activity = activity
        if (activity is BaseActivity) (activity).closeKeypad()
    }
    fun showKeypad() {
        val activity = activity
        if (activity is BaseActivity) (activity).showKeypad()
    }

    fun authenticationError() {
        clearTinyDb()
        activity?.let { fragmentActivity ->
            val intent = Intent(fragmentActivity, AuthActivity::class.java)
            fragmentActivity.startActivity(intent)
            fragmentActivity.finish()
        }
    }


    private fun clearTinyDb() {

        TinyDB.instance.remove(Enums.TinyDBKeys.TOKEN_USER.key)
        TinyDB.instance.remove(Enums.TinyDBKeys.USER.key)
        TinyDB.instance.remove(Enums.TinyDBKeys.NAME.key)

    }

    fun showApiResponseErrors(it: ResponseGeneral<*>) {
        hideLoader()
        if (it.message.isNullOrBlank().not()) {
            showNetworkError(listOf(it.message))
        } else if (it.errors.isNullOrEmpty().not()) {
            showNetworkError(it.errors)
        } else {
            showNetworkError(listOf(getString(R.string.something_went_wrong)))
        }

    }

}