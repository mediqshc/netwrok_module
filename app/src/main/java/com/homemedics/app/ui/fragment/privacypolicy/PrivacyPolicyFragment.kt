package com.homemedics.app.ui.fragment.privacypolicy

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentBasicProfileBinding
import com.homemedics.app.databinding.FragmentPrivacyPolicyBinding

class PrivacyPolicyFragment : BaseFragment() {

    companion object {
        fun newInstance() = PrivacyPolicyFragment()
    }

   // private lateinit var mViewModel: PrivacyPolicyVM

    private lateinit var mBinding: FragmentPrivacyPolicyBinding
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_privacy_policy, container, false)
//    }

    override fun setLanguageData() {
    }

    override fun init() {

        // WebViewClient allows you to handle
        // onPageFinished and override Url loading.
        mBinding.webView.webViewClient = WebViewClient()

        // this will load the url of the website
        mBinding.webView.loadUrl("https://mediq.com.pk/privacy-policy/")

        // this will enable the javascript settings, it can also allow xss vulnerabilities
        mBinding.webView.settings.javaScriptEnabled = true

        // if you want to enable zoom feature
        mBinding.webView.settings.setSupportZoom(true)
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_privacy_policy

    override fun getViewModel() {
    }

    override fun getViewBinding() {
        mBinding = binding as FragmentPrivacyPolicyBinding
    }

    override fun setListeners() {
    }


}