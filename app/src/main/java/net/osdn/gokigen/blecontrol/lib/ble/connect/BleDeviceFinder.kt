package net.osdn.gokigen.blecontrol.lib.ble.connect;

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
import net.osdn.gokigen.blecontrol.lib.ui.SnackBarMessage;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleDeviceFinder implements BluetoothAdapter.LeScanCallback
{
    private String TAG = toString();
    private static final int BLE_SCAN_TIMEOUT_MILLIS = 15 * 1000; // 15秒間
    private static final int BLE_WAIT_DURATION  = 100;           // 100ms間隔

    private final FragmentActivity context;
    private final ITextDataUpdater dataUpdater;
    private String targetDeviceName = null;
    private final BleScanResult scanResult;
    private final SnackBarMessage messageToShow;
    private boolean foundBleDevice = false;

    public BleDeviceFinder(@NonNull FragmentActivity context, @NonNull ITextDataUpdater dataUpdater, @NonNull BleScanResult scanResult)
    {
        this.context = context;
        this.dataUpdater = dataUpdater;
        this.scanResult = scanResult;
        this.messageToShow = new SnackBarMessage(context, false);

    }

    public void reset()
    {
        foundBleDevice = false;
    }

    public void startScan(@NonNull String targetDeviceName)
    {
        try
        {
            this.targetDeviceName = targetDeviceName;
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!btAdapter.isEnabled())
            {
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
                    scanBleDevice(btMgr);
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

    private void scanBleDevice(BluetoothManager btMgr)
    {
        try
        {
            // スキャン開始
            foundBleDevice = false;

            BluetoothAdapter adapter = btMgr.getAdapter();
            if (!adapter.startLeScan(this))
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
            // スキャンを止める(500ms後に)
            Thread.sleep(500);
            adapter.stopLeScan(this);
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
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
    {
        try
        {
            final String btDeviceName = device.getName();
            if ((btDeviceName != null)&&(btDeviceName.matches(targetDeviceName)))
            {
                // device発見！
                foundBleDevice = true;
                scanResult.foundBleDevice(device);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public interface BleScanResult
    {
        void foundBleDevice(BluetoothDevice device);
    }
}
