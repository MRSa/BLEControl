package net.osdn.gokigen.blecontrol.lib.ble.connect.fv100

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.fragment.app.FragmentActivity
import net.osdn.gokigen.blecontrol.lib.ble.R
import net.osdn.gokigen.blecontrol.lib.ble.connect.BleDeviceFinder
import net.osdn.gokigen.blecontrol.lib.ble.connect.BleDeviceFinder.BleScanResult
import net.osdn.gokigen.blecontrol.lib.ble.connect.ITextDataUpdater


class FV100BleDeviceConnector(
    context: FragmentActivity,
    private val dataUpdater: ITextDataUpdater
) : BleScanResult {
    private val TAG: String = toString()
    private var deviceFinder: BleDeviceFinder? = null
    private var communicator: FV100Communicator? = null


    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.deviceFinder = BleDeviceFinder(context, dataUpdater, this)
            this.communicator = FV100Communicator(context, dataUpdater)
        }
    }

    fun query_to_device(deviceName: String) {
        Log.v(TAG, " query_to_device : '" + deviceName + "'")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // BLE のサービスを取得
                if (deviceFinder != null) {
                    // BLEデバイスをスキャンする
                    deviceFinder!!.reset()
                    deviceFinder!!.startScan(deviceName)
                }
            } else {
                // Androidのバージョンが低かった
                dataUpdater.showSnackBar(R.string.not_support_android_version)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reload_device_information() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (communicator != null) {
                    communicator!!.data_reload()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun connect_to_camera_via_wifi() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // WiFi経由でカメラに接続する
                if (communicator != null) {
                    communicator!!.connect_wifi()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setProperty(propertyName: String, propertyValue: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (communicator != null) {
                    communicator!!.setProperty(propertyName, propertyValue)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun foundBleDevice(device: BluetoothDevice?) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (communicator != null) {
                    communicator!!.startCommunicate(device)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
