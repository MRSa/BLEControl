package net.osdn.gokigen.blecontrol.lib.ble.connect;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.UUID;

/**
 *
 *
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class BleConnectionApi18 extends BluetoothGattCallback
{
    private final String TAG = toString();

    BleConnectionApi18()
    {

    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
    {
        Log.v(TAG, " onConnectionStateChange() : [" + status + " -> " + newState +"]" );
        try
        {
            switch (newState)
            {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.v(TAG, "  STATE_CONNECTED : discoverServices()");
                    gatt.discoverServices();
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.v(TAG, "  STATE_DISCONNECTED : disconnect() ");
                    gatt.disconnect();
                    break;

                default:
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status)
    {
        Log.v(TAG, " onServicesDiscovered()  [" + status + "]");
        if (status == BluetoothGatt.GATT_SUCCESS)
        {
            Log.v(TAG, " ----- GATT_SUCCESS -----");
            try
            {
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services)
                {
                    Log.v(TAG, " SERVICE [" + service.getUuid() + "] " + service.getType());
                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics)
                    {
                        Log.v(TAG, "    BluetoothGattCharacteristic() [" + characteristic.getUuid() + "] " + characteristic.getPermissions() + " " + characteristic.getProperties());
                        List<BluetoothGattDescriptor> descripters = characteristic.getDescriptors();
                        for (BluetoothGattDescriptor descriptor : descripters)
                        {
                            Log.v(TAG, "        BluetoothGattDescriptor() [" + descriptor.getUuid() + "] " + descriptor.getPermissions() + " ");

                        }
                    }
                }

                Log.v(TAG, " ===== TRIAL START  =====");
                tryOpenWifi(gatt.getService(UUID.fromString("0000de00-3dd4-4255-8d62-6dc7b9bd5561")));
                Log.v(TAG, " ===== TRIAL FINISH =====");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            Log.v(TAG, " ------------------------");
        }
    }

    private void tryOpenWifi(BluetoothGattService service)
    {
        byte[] requestMessage1 = new byte[] {
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,  // ← この 8バイトになに入れればよいのか。。。
                (byte) 0xf3, (byte) 0x4b, (byte) 0x3c, (byte) 0xdf, (byte) 0xc6, (byte) 0x78, (byte) 0x68, (byte) 0x20,  //
        };
        byte[] requestMessage3 = new byte[] {
                (byte) 0x03,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,  // ← この 8バイトになに入れればよいのか。。。
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,  // ← ここもうーん、わからん。
        };

        try
        {


            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("00002000-3dd4-4255-8d62-6dc7b9bd5561"));



        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
