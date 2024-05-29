package com.homemedics.app.ui.fragment.faq

import android.graphics.Bitmap
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentFaqBinding
import com.homemedics.app.ui.adapter.LanguageAdapter
import com.homemedics.app.utils.DialogUtils
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.isOnline


class FaqFragment : BaseFragment(), View.OnClickListener {

    val language = ArrayList<MultipleViewItem>()
    private lateinit var mBinding: FragmentFaqBinding

    private lateinit var languageAdapter: LanguageAdapter
    val langData = ApplicationClass.mGlobalData

    override fun setLanguageData() {
        mBinding.apply {
            actionbar.title = langData?.tabString?.faq.getSafe()
        }
    }

    override fun init() {
        mBinding.webView.apply {
            settings.loadsImagesAutomatically = true
            settings.javaScriptEnabled = true
            scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            webViewClient = MyBrowser()

            if(isOnline(requireActivity())){
                metaData?.faqUrl?.let { url ->
                    loadUrl(url)
                    showLoader()
                }
            }
            else {
                DialogUtils(requireActivity())
                    .showSingleButtonAlertDialog(
                        title = langData?.errorMessages?.internetError.getSafe(),
                        message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                        buttonCallback = {
                            findNavController().popBackStack()
                                         },
                    )
            }
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_faq

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentFaqBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                onAction1Click = {
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }

    inner class MyBrowser : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            hideLoader()
        }
    }
}