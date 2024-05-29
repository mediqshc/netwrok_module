package com.homemedics.app.utils

import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.homemedics.app.R

class PermissionUtils {
    private var mActivity: AppCompatActivity? = null
    private var mFragment: Fragment? = null
    var onPermissionResult: OnPermissionResult? = null
    private lateinit var permissionsResultLauncher: ActivityResultLauncher<Array<String>>
    var mMessageOnPermanentDenial = ""

    fun setMessageOnPermanentDenial(msg: String): PermissionUtils{
        mMessageOnPermanentDenial = msg
        return this
    }

    constructor(activity: AppCompatActivity){
        mActivity = activity

        permissionsResultLauncher =
            mActivity?.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { success ->
                if (success.isEmpty().not()) {
                    if (success.containsValue(false)) {
                        displayNeverAskAgainDialog()
                        onPermissionResult?.onPermissionDenied()
                    } else {
                        onPermissionResult?.onPermissionGranted()
                    }
                }
            } as ActivityResultLauncher<Array<String>>
    }

    constructor(fragment: Fragment){
        mFragment = fragment

        permissionsResultLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { success ->
            if (success.isEmpty().not()) {
                if (success.containsValue(false)) {
                    displayNeverAskAgainDialog()
                    onPermissionResult?.onPermissionDenied()
                } else {
                    onPermissionResult?.onPermissionGranted()
                }
            }
        }
    }

    fun hasPermissions(permission: Array<String>): Boolean{
        permission.forEach { perm ->
            mActivity?.let {
                if(ContextCompat.checkSelfPermission(
                        it, perm
                    ) != PackageManager.PERMISSION_GRANTED)
                    return false
            } ?: kotlin.run {
                mFragment?.let {
                    if(ContextCompat.checkSelfPermission(
                            it.requireContext(), perm
                        ) != PackageManager.PERMISSION_GRANTED)
                        return false
                }
            }
        }

        return true
    }

    fun requestPermissions(permissions: Array<String>): PermissionUtils{
        permissionsResultLauncher.launch(permissions)
        return this
    }

    private fun displayNeverAskAgainDialog() {
        mActivity?.let {
            DialogUtils(it).showDoubleButtonsAlertDialog(
                message = mMessageOnPermanentDenial,
                positiveButtonText = R.string.permit_manual,
                negativeButtonText = R.string.close,
                buttonCallback = {
                    it.let { gotoAppSettings(it) }
                },
                negativeButtonCallback = {

                },
                cancellable = false
            )
        } ?: kotlin.run {
            mFragment?.let {
                DialogUtils(it.requireActivity()).showDoubleButtonsAlertDialog(
                    message = mMessageOnPermanentDenial,
                    positiveButtonText = R.string.permit_manual,
                    negativeButtonText = R.string.close,
                    buttonCallback = {
                        it.let { gotoAppSettings(it.requireContext()) }
                    },
                    negativeButtonCallback = {

                    },
                    cancellable = false
                )
            }
        }

    }

    interface OnPermissionResult {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }
}