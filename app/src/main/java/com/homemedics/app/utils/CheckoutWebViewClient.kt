package com.homemedics.app.utils

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.homemedics.app.ui.fragment.checkout.CheckoutFragment
import timber.log.Timber
import java.net.URLDecoder

class CheckoutWebViewClient(
    var onPageLoadStarted: (()->Unit)? = null,
    var onPageLoadFinish: (()->Unit)? = null
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return false
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onPageLoadStarted?.invoke()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageLoadFinish?.invoke()
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        super.onPageCommitVisible(view, url)
       // if (url?.startsWith("https://pakistan.paymob.com/api/acceptance/post_pay") == true) {
         //   val urlData = URLDecoder.decode(
           //     url.substring("https://pakistan.paymob.com/api/acceptance/post_pay".length),
             //   "UTF-8"
           // )
           // Timber.e("url data $url  and $urlData")
            CheckoutFragment.WebViewCheck.value=url

       // }
    }
}