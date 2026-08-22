package net.osdn.gokigen.blecontrol.lib.ble.connect

/**
 * Bluetooth のプロパティにアクセスするインタフェース
 * 
 */
interface ICameraBleProperty {
    companion object {
        const val MAX_STORE_PROPERTIES: Int = 12 // Olympus Airは、最大12個登録可能

        const val CAMERA_BLUETOOTH_SETTINGS: String = "camera_bluetooth_settings"
        const val CAMERA_BLUETOOTH_POWER_ON: String = "ble_power_on"

        const val NAME_KEY: String = "AirBtName"
        const val CODE_KEY: String = "AirBtCode"
        const val DATE_KEY: String = "AirBtId"
    }
}
