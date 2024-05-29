package com.homemedics.app.ui.fragment.language

import android.content.Intent
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentSelectLanguageBinding
import com.homemedics.app.ui.activity.SplashActivity
import com.homemedics.app.ui.adapter.LanguageAdapter
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.safeNavigate

class SelectLanguageFragment : BaseFragment(), View.OnClickListener {

    val language = ArrayList<MultipleViewItem>()
    private lateinit var mBinding: FragmentSelectLanguageBinding

    private lateinit var languageAdapter: LanguageAdapter
    val langData = ApplicationClass.mGlobalData

    override fun setLanguageData() {
        mBinding.apply {
            actionbar.title = langData?.tabString?.selectLanguage.getSafe()
            bSelect.text = langData?.globalString?.select
        }
    }

    override fun init() {

        populateAddressList()
        getLanguageList()
    }

    override fun getFragmentLayout() = R.layout.fragment_select_language

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSelectLanguageBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                onAction1Click = {
                    findNavController().popBackStack()
                }
            }
            bSelect.setOnClickListener(this@SelectLanguageFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bSelect -> {
                if(languageAdapter.getSelectedItem()?.desc!=tinydb.getString(Enums.TinyDBKeys.LOCALE.key)) {
                    languageAdapter.getSelectedItem()?.desc?.let {
                        tinydb.putString(
                            Enums.TinyDBKeys.LOCALE.key,
                            it
                        )
                    }
                    setLocaleManager()
                    requireActivity().finish()
                    startActivity()
                }
            }
        }
    }

    private fun setLocaleManager() {
        ApplicationClass.localeManager.updateLocaleData(
            requireContext(),
            TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        )

    }

    private fun startActivity() {
        val intent = Intent(requireContext(), SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun populateAddressList() {
        languageAdapter = LanguageAdapter()
        mBinding.apply {
            rvLanguage.adapter = languageAdapter
        }
    }

    private fun getLanguageList() {
        val languageModel = metaData?.tenantLanguageItem

//        languageModel.add(
//            LanguageItem(
//                id = 1,
//                name = "English",
//            ).apply {
////                drawable = R.drawable.ic_adjust
//                desc = DEFAULT_LOCALE_LANGUAGE_EN
//            }
//        )
//        languageModel.add(
//            LanguageItem(
//                id = 2, name = "Urdu"
//            ).apply {
////                drawable = R.drawable.ic_adjust
//                desc = DEFAULT_LOCALE_LANGUAGE_UR
//            }
//        )
        languageModel?.map {
            if (isUniqueItem(it, language as ArrayList<MultipleViewItem>))
                language.add(it)

        }

        languageAdapter.listItems = (language).map {
            it.isSelected =
                it.desc.getSafe() == TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
            it
        } as ArrayList<MultipleViewItem>

    }


    private fun isUniqueItem(item: MultipleViewItem, list: ArrayList<MultipleViewItem>): Boolean {
        return list.find { it.title == item.title && it.desc == item.desc } == null
    }
}