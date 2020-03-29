package net.osdn.gokigen.blecontrol.lib.ble.connect.fv100;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class FV100Finder implements BluetoothAdapter.LeScanCallback
{
    private final String targetDeviceName;
    private final BleScanResult scanResult;

    FV100Finder(@NonNull String targetDeviceName, @NonNull BleScanResult scanResult)
    {
        this.targetDeviceName = targetDeviceName;
        this.scanResult = scanResult;

    }

    void reset()
    {

    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
    {
        try
        {
            final String btDeviceName = device.getName();
            if ((btDeviceName != null)&&(btDeviceName.matches(targetDeviceName)))
            {
                // device発見！
                scanResult.foundBleDevice(device);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    interface BleScanResult
    {
        void foundBleDevice(BluetoothDevice device);
    }
}
