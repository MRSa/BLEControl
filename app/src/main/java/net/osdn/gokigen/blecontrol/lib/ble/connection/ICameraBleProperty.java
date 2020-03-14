package net.osdn.gokigen.blecontrol.lib.ble.connection;

/**
 *  Bluetooth のプロパティにアクセスするインタフェース
 *
 */
public interface ICameraBleProperty
{
    int MAX_STORE_PROPERTIES = 12;  // Olympus Airは、最大12個登録可能

    String CAMERA_BLUETOOTH_SETTINGS = "camera_bluetooth_settings";
    String CAMERA_BLUETOOTH_POWER_ON = "ble_power_on";

    String NAME_KEY = "AirBtName";
    String CODE_KEY = "AirBtCode";
    String DATE_KEY = "AirBtId";
}
