package net.osdn.gokigen.blecontrol.lib.ble.connect.eeg;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import net.osdn.gokigen.blecontrol.lib.SimpleLogDumper;
import net.osdn.gokigen.blecontrol.lib.ble.R;
import net.osdn.gokigen.blecontrol.lib.ble.connect.BleDeviceFinder;
import net.osdn.gokigen.blecontrol.lib.ble.connect.ITextDataUpdater;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;


public class MindWaveCommunication implements BleDeviceFinder.BleScanResult
{
    private final String TAG = toString();

    private final FragmentActivity context;
    private final ITextDataUpdater dataUpdater;
    private BleDeviceFinder deviceFinder = null;
    private boolean foundDevice = false;

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
                    foundDevice = false;
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



    private void parseReceivedData(byte[] data)
    {
        // 受信データブロック１つ分
        try
        {
            if (data.length <= 3)
            {
                // ヘッダ部しか入っていない...無視する
                return;
            }
            byte length = data[2];
            if (data.length < (length + 2))
            {
                // データが最小サイズに満たない...無視する
                return;
            }

            SimpleLogDumper.dump_bytes("RECV SPP : ", data);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void serialCommunicationMain(final BluetoothSocket btSocket)
    {
        InputStream inputStream = null;
        try
        {
            btSocket.connect();
            inputStream = btSocket.getInputStream();
        }
        catch (Exception e)
        {

            Log.e(TAG, "Fail to accept.", e);
        }
        if (inputStream == null)
        {
            return;
        }

        // シリアルデータの受信メイン部分
        byte previousData = (byte) 0xff;
        ByteArrayOutputStream outputStream = null;
        while (foundDevice)
        {
            try
            {
                int data = inputStream.read();
                byte byteData = (byte) (data & 0xff);
                if ((previousData == byteData)&&(byteData == (byte) 0xaa))
                {
                    // 先頭データを見つけた。 （0xaa 0xaa がヘッダ）
                    if (outputStream != null)
                    {
                        parseReceivedData(outputStream.toByteArray());
                        outputStream = null;
                    }
                    outputStream = new ByteArrayOutputStream();
                    outputStream.write((byte) 0xaa);
                    outputStream.write((byte) 0xaa);
                }
                else
                {
                    if (outputStream != null)
                    {
                        outputStream.write(byteData);
                    }
                }
                previousData = byteData;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            btSocket.close();
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
            foundDevice = true;
            final BluetoothSocket btSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        serialCommunicationMain(btSocket);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            if (btSocket != null)
            {
                thread.start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
