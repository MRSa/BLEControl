package net.osdn.gokigen.blecontrol.lib.ble.connect.eeg;

import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import net.osdn.gokigen.blecontrol.lib.ble.R;
import net.osdn.gokigen.blecontrol.lib.ble.connect.BleDeviceFinder;
import net.osdn.gokigen.blecontrol.lib.ble.connect.ITextDataUpdater;


public class MindWaveCommunication implements BleDeviceFinder.BleScanResult
{
    private final String TAG = toString();

    private final FragmentActivity context;
    private final ITextDataUpdater dataUpdater;
    private BleDeviceFinder deviceFinder = null;

    public MindWaveCommunication(@NonNull FragmentActivity context, @NonNull ITextDataUpdater dataUpdater)
    {
        this.context = context;
        this.dataUpdater = dataUpdater;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            this.deviceFinder = new BleDeviceFinder(context, dataUpdater, this);
        }
    }

    public void connect(@NonNull String deviceName)
    {
        Log.v(TAG, " BrainWaveMobileCommunicator::connect() : " + deviceName);
        setText(context.getString(R.string.start_query) + " '" + deviceName + "' ");
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

    private void setText(@NonNull final String message)
    {
        dataUpdater.setText(message);
    }

    private void addText(@NonNull final String message)
    {
        dataUpdater.addText(message);
    }


    @Override
    public void foundBleDevice(BluetoothDevice device)
    {
        try
        {
            addText(" ");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
/*
                if (communicator != null)
                {
                    communicator.startCommunicate(device);
                }
*/
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
