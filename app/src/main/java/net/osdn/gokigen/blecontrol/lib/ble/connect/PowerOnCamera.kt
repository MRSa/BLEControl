package net.osdn.gokigen.blecontrol.lib.ble.connect

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import net.osdn.gokigen.blecontrol.lib.ble.R
import net.osdn.gokigen.blecontrol.lib.ble.connect.ICameraPowerOn.PowerOnCameraCallback
import java.util.Locale

/**
 * BLE経由でカメラの電源を入れるクラス
 * 
 */
class PowerOnCamera(context: Activity) : ICameraPowerOn {
    private val TAG = toString()
    private val BLE_SCAN_TIMEOUT_MILLIS = 5 * 1000 // 5秒間
    private val BLE_WAIT_DURATION = 100 // 100ms間隔
    private val context: Activity
    private var myCameraList: MutableList<CameraBleSetArrayItem>? = null
    private var myBluetoothDevice: BluetoothDevice? = null
    private var myBtDevicePassCode: String? = ""

    /**
     * 
     */
    init {
        Log.v(TAG, "PowerOnCamera()")
        this.context = context
        setupCameraList()
    }

    override fun wakeup(callback: PowerOnCameraCallback) {
        Log.v(TAG, "PowerOnCamera::wakeup()")

        try {
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            if (!btAdapter.isEnabled()) {
                // Bluetoothの設定がOFFだった
                Log.v(TAG, "Bluetooth is currently off.")
                context.runOnUiThread(object : Runnable {
                    override fun run() {
                        // Toastで カメラ起動エラーがあったことを通知する
                        Toast.makeText(
                            context,
                            context.getString(R.string.ble_setting_is_off),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
                callback.wakeupExecuted(false)
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callback.wakeupExecuted(false)
            return
        }

        val btMgr: BluetoothManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // BLE のサービスを取得
            btMgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
            if (btMgr == null) {
                // Bluetooth LEのサポートがない場合は、何もしない
                Log.v(TAG, "PowerOnCamera::wakeup() NOT SUPPORT BLE...")

                // BLEの起動はしなかった...
                callback.wakeupExecuted(false)
                return
            }
            val deviceList = myCameraList!!

            //  BLE_SCAN_TIMEOUT_MILLIS の間だけBLEのスキャンを実施する
            val thread = Thread(object : Runnable {
                override fun run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        class bleScanCallback : LeScanCallback {
                            override fun onLeScan(
                                bluetoothDevice: BluetoothDevice,
                                i: Int,
                                bytes: ByteArray?
                            ) {
                                try {
                                    val btDeviceName = bluetoothDevice.getName()
                                    // Log.v(TAG, "onLeScan() " + btDeviceName);   // BluetoothDevice::getName() でログ出力してくれるので
                                    for (device in deviceList) {
                                        val btName = device.getBtName()
                                        // Log.v(TAG, "onLeScan() [" + btName + "]");
                                        if (btName == btDeviceName) {
                                            // マイカメラ発見！
                                            // 別スレッドで起動する
                                            myBluetoothDevice = bluetoothDevice
                                            myBtDevicePassCode = device.getBtPassCode()
                                            break
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            fun reset() {
                                try {
                                    myBluetoothDevice = null
                                    myBtDevicePassCode = ""
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        val scanCallback = bleScanCallback()
                        try {
                            // スキャン開始
                            scanCallback.reset()
                            val adapter = btMgr.getAdapter()
                            if (!adapter.startLeScan(scanCallback)) {
                                // Bluetooth LEのスキャンが開始できなかった場合...
                                Log.v(TAG, "Bluetooth LE SCAN START fail...")
                                callback.wakeupExecuted(false)
                                return
                            }
                            Log.v(TAG, "BT SCAN STARTED")
                            var passed = 0
                            while (passed < BLE_SCAN_TIMEOUT_MILLIS) {
                                // BLEデバイスが見つかったときは抜ける...
                                if (myBluetoothDevice != null) {
                                    break
                                }

                                // BLEのスキャンが終わるまで待つ
                                Thread.sleep(BLE_WAIT_DURATION.toLong())
                                passed = passed + BLE_WAIT_DURATION
                            }
                            // スキャンを止める
                            adapter.stopLeScan(scanCallback)
                            Log.v(TAG, "BT SCAN STOPPED")

                            // カメラの起動
                            callback.wakeupExecuted(
                                wakeupViaBle(
                                    adapter,
                                    myBluetoothDevice,
                                    myBtDevicePassCode
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.v(TAG, "Bluetooth LE SCAN EXCEPTION...")
                            callback.wakeupExecuted(false)

                            try {
                                val btName =
                                    if (myBluetoothDevice != null) myBluetoothDevice!!.getName() else ""
                                context.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        // Toastで カメラ起動エラーがあったことを通知する
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.launch_fail_via_ble) + btName,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                })
                            } catch (ee: Exception) {
                                ee.printStackTrace()
                            }
                        }
                        Log.v(TAG, "Bluetooth LE SCAN STOPPED")
                    } // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                }
            })
            thread.start()
        }
    }

    private fun wakeupViaBle(
        adapter: BluetoothAdapter?,
        myBluetoothDevice: BluetoothDevice?,
        passCode: String?
    ): Boolean {
        if (adapter == null) {
            Log.v(TAG, " BluetoothAdapter is UNKNOWN(null).")
            return (false)
        }

        if (myBluetoothDevice == null) {
            Log.v(TAG, " Bt Device is UNKNOWN(null).")
            return (false)
        }

        Log.v(
            TAG,
            "WAKE UP CAMERA : " + myBluetoothDevice.getName() + " [" + myBluetoothDevice.getAddress() + "]"
        )
        try {
            Log.v(TAG, "PASSCODE : " + passCode)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // デバイスに接続する
                myBluetoothDevice.connectGatt(context, false, BleConnectionApi18())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (true)
    }


    /**
     * 
     * 
     */
    private fun setupCameraList() {
        myCameraList = ArrayList<CameraBleSetArrayItem>()

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        for (index in 1..ICameraBleProperty.MAX_STORE_PROPERTIES) {
            val idHeader = String.format(Locale.ENGLISH, "%03d", index)
            val prefDate: String =
                preferences.getString(idHeader + ICameraBleProperty.DATE_KEY, "")!!
            if (prefDate.length <= 0) {
                // 登録が途中までだったとき
                break
            }
            val btName: String = preferences.getString(idHeader + ICameraBleProperty.NAME_KEY, "")!!
            val btCode: String = preferences.getString(idHeader + ICameraBleProperty.CODE_KEY, "")!!
            myCameraList!!.add(CameraBleSetArrayItem(idHeader, btName, btCode, prefDate))
        }
        Log.v(TAG, "setupCameraList() : " + myCameraList!!.size)
    }
}
