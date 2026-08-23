package net.osdn.gokigen.blecontrol.lib.ble.connect

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import net.osdn.gokigen.blecontrol.lib.ble.R
import net.osdn.gokigen.blecontrol.lib.ui.SnackBarMessage

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleDeviceFinder(
    private val context: FragmentActivity,
    private val dataUpdater: ITextDataUpdater,
    private val scanResult: BleScanResult
) : LeScanCallback {
    private val TAG = toString()
    private var targetDeviceName: String? = null
    private val messageToShow: SnackBarMessage
    private var foundBleDevice = false

    init {
        this.messageToShow = SnackBarMessage(context, false)
    }

    fun reset() {
        foundBleDevice = false
    }

    fun startScan(targetDeviceName: String) {
        try {
            this.targetDeviceName = targetDeviceName
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            if (!btAdapter.isEnabled()) {
                // Bluetoothの設定がOFFだった
                messageToShow.showMessage(R.string.ble_setting_is_off)
            }
            val btMgr: BluetoothManager?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // BLE のサービスを取得
                btMgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
                if (btMgr != null) {
                    // BLEデバイスをスキャンする
                    scanBleDevice(btMgr)
                } else {
                    // Bluetooth LEのサポートがない場合は、何もしない
                    messageToShow.showMessage(R.string.not_support_ble)
                }
            } else {
                // Androidのバージョンが低かった
                messageToShow.showMessage(R.string.not_support_android_version)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scanBleDevice(btMgr: BluetoothManager) {
        try {
            // スキャン開始
            foundBleDevice = false

            val adapter = btMgr.getAdapter()
            if (!adapter.startLeScan(this)) {
                // Bluetooth LEのスキャンが開始できなかった場合...
                Log.v(TAG, "Bluetooth LE SCAN START fail...")
                messageToShow.showMessage(R.string.ble_scan_start_failure)
                return
            }
            Log.v(TAG, " ----- BT SCAN STARTED ----- ")
            var passed = 0
            while (passed < BLE_SCAN_TIMEOUT_MILLIS) {
                if (foundBleDevice) {
                    // デバイス発見
                    Log.v(TAG, "FOUND DEVICE")
                    break
                }

                // BLEのスキャンが終わるまで待つ
                Thread.sleep(BLE_WAIT_DURATION.toLong())
                passed = passed + BLE_WAIT_DURATION
            }
            // スキャンを止める(500ms後に)
            Thread.sleep(500)
            adapter.stopLeScan(this)
            Log.v(TAG, " ----- BT SCAN STOPPED ----- ")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.v(TAG, "Bluetooth LE SCAN EXCEPTION...")
            messageToShow.showMessage(R.string.scan_fail_via_ble)
        }
        Log.v(TAG, "Bluetooth LE SCAN STOPPED")
        context.runOnUiThread(object : Runnable {
            override fun run() {
                dataUpdater.setText(context.getString(R.string.ble_scan_finished))
            }
        })
    }

    override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray?) {
        try {
            val btDeviceName = device.getName()
            if ((btDeviceName != null) && (btDeviceName.matches(targetDeviceName!!.toRegex()))) {
                // device発見！
                foundBleDevice = true
                scanResult.foundBleDevice(device)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface BleScanResult {
        fun foundBleDevice(device: BluetoothDevice?)
    }

    companion object {
        private val BLE_SCAN_TIMEOUT_MILLIS = 15 * 1000 // 15秒間
        private const val BLE_WAIT_DURATION = 100 // 100ms間隔
    }
}
