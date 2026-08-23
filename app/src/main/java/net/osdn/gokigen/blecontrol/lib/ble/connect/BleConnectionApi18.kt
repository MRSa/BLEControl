package net.osdn.gokigen.blecontrol.lib.ble.connect

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.UUID

/**
 * 
 * 
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
class BleConnectionApi18 internal constructor() : BluetoothGattCallback() {
    private val TAG = toString()

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        Log.v(TAG, " onConnectionStateChange() : [" + status + " -> " + newState + "]")
        try {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.v(TAG, "  STATE_CONNECTED : discoverServices()")
                    gatt.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.v(TAG, "  STATE_DISCONNECTED : disconnect() ")
                    gatt.disconnect()
                }

                else -> {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        Log.v(TAG, " onServicesDiscovered()  [" + status + "]")
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.v(TAG, " ----- GATT_SUCCESS -----")
            try {
                val services = gatt.getServices()
                for (service in services) {
                    Log.v(TAG, " SERVICE [" + service.getUuid() + "] " + service.getType())
                    val characteristics = service.getCharacteristics()
                    for (characteristic in characteristics) {
                        Log.v(
                            TAG,
                            "    BluetoothGattCharacteristic() [" + characteristic.getUuid() + "] " + characteristic.getPermissions() + " " + characteristic.getProperties()
                        )
                        val descripters = characteristic.getDescriptors()
                        for (descriptor in descripters) {
                            Log.v(
                                TAG,
                                "        BluetoothGattDescriptor() [" + descriptor.getUuid() + "] " + descriptor.getPermissions() + " "
                            )
                        }
                    }
                }

                Log.v(TAG, " ===== TRIAL START  =====")
                tryOpenWifi(gatt.getService(UUID.fromString("0000de00-3dd4-4255-8d62-6dc7b9bd5561")))
                Log.v(TAG, " ===== TRIAL FINISH =====")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.v(TAG, " ------------------------")
        }
    }

    private fun tryOpenWifi(service: BluetoothGattService) {
        val requestMessage1 = byteArrayOf(
            0x01.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),  // ← この 8バイトになに入れればよいのか。。。
            0xf3.toByte(),
            0x4b.toByte(),
            0x3c.toByte(),
            0xdf.toByte(),
            0xc6.toByte(),
            0x78.toByte(),
            0x68.toByte(),
            0x20.toByte(),  //
        )
        val requestMessage3 = byteArrayOf(
            0x03.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),  // ← この 8バイトになに入れればよいのか。。。
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),  // ← ここもうーん、わからん。
        )

        try {
            val characteristic =
                service.getCharacteristic(UUID.fromString("00002000-3dd4-4255-8d62-6dc7b9bd5561"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
