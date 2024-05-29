package com.homemedics.app.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.fatron.network_module.models.response.meta.MetaDataResponse
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.homemedics.app.R

abstract class BaseBottomSheetFragment : BottomSheetDialogFragment(){
    protected lateinit var className: String
    protected lateinit var binding: ViewDataBinding

    val metaData: MetaDataResponse by lazy {
        TinyDB.instance.getObject(
            Enums.TinyDBKeys.META.key,
            MetaDataResponse::class.java
        ) as MetaDataResponse
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        className = javaClass.name

        getViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            getFragmentLayout(),
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getViewBinding()
        setLanguageData()
        init()
        setListeners()
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }
    fun showLoader() {
        val activity = activity
        if (activity is BaseActivity) (activity).showLoader()
    }

    fun hideLoader() {
        val activity = activity
        if (activity is BaseActivity) (activity).hideLoader()
    }


    abstract fun setLanguageData()
    abstract fun init()
    abstract fun getFragmentLayout(): Int
    abstract fun getViewModel()
    abstract fun getViewBinding()
    abstract fun setListeners()

}