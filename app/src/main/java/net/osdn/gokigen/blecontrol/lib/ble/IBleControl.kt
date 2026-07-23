package net.osdn.gokigen.blecontrol.lib.ble

import android.bluetooth.BluetoothDevice

interface IBleControl
{
    // カメラ起動指示
    fun wakeup(target: MyBleDevice, code: String, callback: IBleControlCallback)
    fun cancelWakeup()

    // 実行終了時のコールバックのインタフェース
    interface IBleControlCallback
    {
        fun onStart(message: String)
        fun onProgress(message: String, isLineFeed: Boolean = true)
        fun wakeupExecuted(isExecute: Boolean)
        fun finishedScan(deviceList: Map<String, BluetoothDevice>)
    }
}
