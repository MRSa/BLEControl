package net.osdn.gokigen.blecontrol.lib.ble.connect

interface ICameraPowerOn {
    // カメラ起動指示
    fun wakeup(callback: PowerOnCameraCallback)

    // 実行終了時のコールバックのインタフェース
    interface PowerOnCameraCallback {
        fun wakeupExecuted(isExecute: Boolean)
    }
}
