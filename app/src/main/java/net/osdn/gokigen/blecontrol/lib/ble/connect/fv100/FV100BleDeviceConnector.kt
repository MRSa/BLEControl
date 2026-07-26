package net.osdn.gokigen.blecontrol.lib.ble.connect.fv100;

import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import net.osdn.gokigen.blecontrol.lib.ble.R;
import net.osdn.gokigen.blecontrol.lib.ble.connect.BleDeviceFinder;
import net.osdn.gokigen.blecontrol.lib.ble.connect.ITextDataUpdater;

public class FV100BleDeviceConnector implements BleDeviceFinder.BleScanResult
{
    private String TAG = toString();
    private BleDeviceFinder deviceFinder = null;
    private FV100Communicator communicator = null;
    private final ITextDataUpdater dataUpdater;


    public FV100BleDeviceConnector(@NonNull FragmentActivity context, @NonNull ITextDataUpdater dataUpdater)
    {
        this.dataUpdater = dataUpdater;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            this.deviceFinder = new BleDeviceFinder(context, dataUpdater, this);
            this.communicator = new FV100Communicator(context, dataUpdater);
        }
    }

    public void query_to_device(String deviceName)
    {
        Log.v(TAG, " query_to_device : '" + deviceName + "'");
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                // BLE のサービスを取得
                if (deviceFinder != null)
                {
                    // BLEデバイスをスキャンする
                    deviceFinder.reset();
                    deviceFinder.startScan(deviceName);
                }
            }
            else
            {
                // Androidのバージョンが低かった
                dataUpdater.showSnackBar(R.string.not_support_android_version);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void reload_device_information()
    {
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                if (communicator != null)
                {
                    communicator.data_reload();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void connect_to_camera_via_wifi()
    {
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                // WiFi経由でカメラに接続する
                if (communicator != null)
                {
                    communicator.connect_wifi();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setProperty(@NonNull String propertyName, @NonNull String propertyValue)
    {
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                if (communicator != null)
                {
                    communicator.setProperty(propertyName, propertyValue);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void foundBleDevice(BluetoothDevice device)
    {
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                if (communicator != null)
                {
                    communicator.startCommunicate(device);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
