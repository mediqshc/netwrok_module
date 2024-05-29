package com.homemedics.app.utils

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.homemedics.app.R
import timber.log.Timber

/**
 * add permissions
 * <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
 * <uses-permission android:name="android.permission.BLUETOOTH" />
 *
 * create instance with context
 * check if isBluetoothSupported()
 * onResume/onPause -> startListening()/stopListening()
 * receive events on onServiceActionPerformed(BluetoothConnectStatus)
 *
 * headset.connectedDevices needs runtime permission
 *
 * created by: M.Asadullah :)
 */

class BluetoothUtils(val activity: AppCompatActivity) {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothProfileListener: BluetoothProfile.ServiceListener? = null
    private lateinit var bluetoothProfile: BluetoothProfile
    var onServiceActionPerformed: ((status: BluetoothConnectStatus)->Unit)? = null

    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if(isBluetoothSupported().not()){
            onServiceActionPerformed?.invoke(BluetoothConnectStatus.STATUS_BLUETOOTH_NOT_SUPPORTED)
        }

        bluetoothProfileListener = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == BluetoothProfile.HEADSET) {
                    bluetoothProfile = proxy
                    val headset = proxy as BluetoothHeadset

                    if(hasPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT))){
                        val devices = headset.connectedDevices
                        if (devices.isNotEmpty()) {
                            // Bluetooth headset is connected
                            // You can access the connected BluetoothDevice object using devices[0]
                            onServiceActionPerformed?.invoke(BluetoothConnectStatus.STATUS_CONNECTED)
                        } else {
                            // Bluetooth headset is not connected
                            onServiceActionPerformed?.invoke(BluetoothConnectStatus.STATUS_DISCONNECTED)
                        }
                    }
                    else {
                        requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
                        onServiceActionPerformed?.invoke(BluetoothConnectStatus.STATUS_PERMISSION_REQUIRED)
                    }
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                // Bluetooth headset service is disconnected
                onServiceActionPerformed?.invoke(BluetoothConnectStatus.STATUS_DISCONNECTED)
            }
        }
    }

    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

    fun startListening(){
        if(isBluetoothSupported())
            bluetoothAdapter?.getProfileProxy(activity, bluetoothProfileListener, BluetoothProfile.HEADSET)
        else
            onServiceActionPerformed?.invoke(BluetoothConnectStatus.STATUS_BLUETOOTH_NOT_SUPPORTED)
    }

    fun stopListening(){
        try {
            if (bluetoothProfileListener != null) {
                bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothProfile)
                bluetoothProfileListener = null
            }
        }
        catch (e:Exception){e.printStackTrace()}
    }

    fun hasPermissions(context: Context, permission: Array<String>): Boolean{
        permission.forEach { perm ->
            if(ContextCompat.checkSelfPermission(
                    context, perm
                ) != PackageManager.PERMISSION_GRANTED)
                return false
        }

        return true
    }

    private fun requestPermissions(permissions: Array<String>){
        permissionsResultLauncher.launch(permissions)
    }

    private val permissionsResultLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { success ->
            if (success.isEmpty().not()) {
                if (success.containsValue(false)) {
                    displayNeverAskAgainDialog()
                    onServiceActionPerformed?.invoke(BluetoothConnectStatus.STATUS_PERMISSION_REQUIRED)
                } else {
                    val headset = bluetoothProfile as BluetoothHeadset?
                    if(headset != null && headset.connectedDevices.isNotEmpty()){
                        onServiceActionPerformed?.invoke(BluetoothConnectStatus.STATUS_CONNECTED)
                    }
                    else {
                        onServiceActionPerformed?.invoke(BluetoothConnectStatus.STATUS_DISCONNECTED)
                    }
                }
            }
        }

    private fun displayNeverAskAgainDialog() {
        DialogUtils(activity).showDoubleButtonsAlertDialog(
            message = getString(R.string.m_bluetooth_permissions),
            positiveButtonText = R.string.permit_manual,
            negativeButtonText = R.string.close,
            buttonCallback = {
                activity.let { gotoAppSettings(it) }
            },
            negativeButtonCallback = {

            },
            cancellable = false
        )

    }

    enum class BluetoothConnectStatus{
        STATUS_CONNECTED,
        STATUS_DISCONNECTED,
        STATUS_PERMISSION_REQUIRED,
        STATUS_BLUETOOTH_NOT_SUPPORTED,
    }
}