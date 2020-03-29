package net.osdn.gokigen.blecontrol.lib.ble.connect.fv100;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import net.osdn.gokigen.blecontrol.lib.ble.R;
import net.osdn.gokigen.blecontrol.lib.ble.connect.ITextDataUpdater;
import net.osdn.gokigen.blecontrol.lib.ui.SnackBarMessage;

public class FV100BleDeviceConnector implements FV100Finder.BleScanResult
{
    private String TAG = toString();
    private static final int BLE_SCAN_TIMEOUT_MILLIS = 15 * 1000; // 15秒間
    private static final int BLE_WAIT_DURATION  = 100;           // 100ms間隔
    private final FragmentActivity context;
    private final ITextDataUpdater dataUpdater;
    private final SnackBarMessage messageToShow;
    private FV100Communicator communicator = null;
    private boolean foundBleDevice = false;

    public FV100BleDeviceConnector(@NonNull FragmentActivity context, @NonNull ITextDataUpdater dataUpdater)
    {
        this.context = context;
        this.dataUpdater = dataUpdater;
        this.messageToShow = new SnackBarMessage(context, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            communicator = new FV100Communicator(context, dataUpdater, messageToShow);
        }
    }

    public void query_to_device(String deviceName)
    {
        Log.v(TAG, " query_to_device : '" + deviceName + "'");
        try {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!btAdapter.isEnabled()) {
                // Bluetoothの設定がOFFだった
                messageToShow.showMessage(R.string.ble_setting_is_off);
            }
            BluetoothManager btMgr;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                // BLE のサービスを取得
                btMgr = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                if (btMgr != null)
                {
                    // BLEデバイスをスキャンする
                    scanBleDevice(btMgr, new FV100Finder(deviceName, this));
                }
                else
                {
                    // Bluetooth LEのサポートがない場合は、何もしない
                    messageToShow.showMessage(R.string.not_support_ble);
                }
            }
            else
            {
                // Androidのバージョンが低かった
                messageToShow.showMessage(R.string.not_support_android_version);
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


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanBleDevice(BluetoothManager btMgr, FV100Finder finder)
    {
        try
        {
            // スキャン開始
            foundBleDevice = false;
            finder.reset();
            BluetoothAdapter adapter = btMgr.getAdapter();
            if (!adapter.startLeScan(finder))
            {
                // Bluetooth LEのスキャンが開始できなかった場合...
                Log.v(TAG, "Bluetooth LE SCAN START fail...");
                messageToShow.showMessage(R.string.ble_scan_start_failure);
                return;
            }
            Log.v(TAG, " ----- BT SCAN STARTED ----- ");
            int passed = 0;
            while (passed < BLE_SCAN_TIMEOUT_MILLIS)
            {
                if (foundBleDevice)
                {
                    // デバイス発見
                    Log.v(TAG, "FOUND DEVICE");
                    break;
                }

                // BLEのスキャンが終わるまで待つ
                Thread.sleep(BLE_WAIT_DURATION);
                passed = passed + BLE_WAIT_DURATION;
            }
            // スキャンを止める
            Thread.sleep(1000);
            adapter.stopLeScan(finder);
            Log.v(TAG, " ----- BT SCAN STOPPED ----- ");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.v(TAG, "Bluetooth LE SCAN EXCEPTION...");
            messageToShow.showMessage(R.string.scan_fail_via_ble);
        }
        Log.v(TAG, "Bluetooth LE SCAN STOPPED");
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dataUpdater.setText(context.getString(R.string.ble_scan_finished));
            }
        });
    }

    @Override
    public void foundBleDevice(BluetoothDevice device)
    {
        try
        {
            foundBleDevice = true;
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
