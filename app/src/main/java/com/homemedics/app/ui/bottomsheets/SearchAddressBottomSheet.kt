package com.homemedics.app.ui.bottomsheets

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.homemedics.app.R
import com.homemedics.app.base.BaseBottomSheetFragment
import com.homemedics.app.databinding.FragmentSearchAddressBinding
import com.homemedics.app.utils.Constants
import com.homemedics.app.utils.safeNavigate
import com.homemedics.app.viewmodel.AddressViewModel

class SearchAddressBottomSheet : BaseBottomSheetFragment() {
    private lateinit var mBinding: FragmentSearchAddressBinding
    private val viewModel: AddressViewModel by activityViewModels()

    override fun setLanguageData() {
    }

    override fun init() {
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
        isCancelable = true

        val displayMetrics = requireActivity().resources.displayMetrics
        val height = displayMetrics.heightPixels
        val maxHeight = (height * 0.70).toInt()

        val layoutParams =
            mBinding.searchLayout.layoutParams as FrameLayout.LayoutParams
        layoutParams.height = maxHeight
        mBinding.searchLayout.layoutParams = layoutParams
        mBinding.etSearch.drawable = 0
        mBinding.etSearch.onTextChangedListener = { editText ->

            if (editText.isNotEmpty() && editText.length >= Constants.placeApiHit) {

            } else if (editText.isEmpty()) {
                //submit list
            }
        }

    }

    override fun getFragmentLayout(): Int = R.layout.fragment_address_detail

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSearchAddressBinding
    }

    override fun setListeners() {
        mBinding.back.setOnClickListener {
         }

    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener {
            val bottomSheet =
                bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.skipCollapsed = true
            val dp = resources.getDimensionPixelSize(R.dimen.dp60)
            behavior.peekHeight = dp
            behavior.isDraggable = false
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return bottomSheetDialog

    }

}