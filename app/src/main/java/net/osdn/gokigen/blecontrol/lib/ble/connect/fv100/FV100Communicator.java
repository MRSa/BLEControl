package net.osdn.gokigen.blecontrol.lib.ble.connect.fv100;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import net.osdn.gokigen.blecontrol.lib.ble.R;
import net.osdn.gokigen.blecontrol.lib.ble.connect.ITextDataUpdater;
import net.osdn.gokigen.blecontrol.lib.ui.SnackBarMessage;
import net.osdn.gokigen.blecontrol.lib.wifi.WifiConnector;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class FV100Communicator  extends BluetoothGattCallback implements FV100ObjectPaser.ReceivedDataNotify, WifiConnector.WifiConnectNotify, FV100SendMessageProvider.MessageSequenceNotify
{
    private String TAG = toString();
    private final FragmentActivity context;
    private final ITextDataUpdater dataUpdater;
    private final SnackBarMessage messageToShow;
    //private boolean mtuSizeIsExpanded = false;
    private boolean startQuery = false;
    private boolean onConnected = false;
    private ByteArrayOutputStream receiveBuffer;
    private final FV100ObjectPaser objectParser;
    private final FV100SendMessageProvider sendMessageProvider;
    private final WifiConnector wifiConnector;
    private BluetoothGatt btGatt = null;
    private String wifiSsId = null;
    private String wifiKey = null;

    private List<byte[]> setPropertyMessage = null;
    private int setPropertyMessageIndex = -1;

    FV100Communicator(@NonNull FragmentActivity context, @NonNull ITextDataUpdater dataUpdater, @NonNull SnackBarMessage messageToShow)
    {
        this.context = context;
        this.dataUpdater = dataUpdater;
        this.messageToShow = messageToShow;
        this.receiveBuffer = new ByteArrayOutputStream();
        this.objectParser = new FV100ObjectPaser(this);
        this.sendMessageProvider = new FV100SendMessageProvider(this);
        this.wifiConnector = new WifiConnector(context, messageToShow);
    }

    void startCommunicate(@Nullable final BluetoothDevice device)
    {
        String deviceName = (device != null) ? device.getName() : "";
        dataUpdater.setText(" FOUND : " + deviceName);
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        if (device != null)
                        {
                            communicateMain(device);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void data_reload()
    {
        Log.v(TAG, " RELOAD ");
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (btGatt != null)
                {
                    // 画面をクリアする
                    dataUpdater.setText(" ");

                    // 最初から情報を取り直す。
                    sendMessageProvider.resetSequence();
                    startQuery = false;
                    try
                    {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                queryDeviceProperty(btGatt);
                            }
                        });
                        thread.start();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    void connect_wifi()
    {
        try
        {
            if ((wifiSsId != null)&&(wifiKey != null))
            {
                wifiConnector.connectToWifi(wifiSsId, wifiKey, this);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void setProperty(@NonNull String propertyName, @NonNull String propertyValue)
    {
        String message = " " + propertyName + " : " + propertyValue;
        Log.v(TAG, message);
        if (btGatt == null)
        {
            // BluetoothGatt が設定されていない場合...
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageToShow.showMessage(context.getString(R.string.ble_not_connected));
                }
            });
            return;
        }
        if ((setPropertyMessage != null)||(sendMessageProvider.isMessageSending()))
        {
            // ただいま通信中なので何もしないで終わる
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageToShow.showMessage(context.getString(R.string.now_ble_communicating));
                }
            });
            return;
        }
        addTextInformation(message);
        setPropertyMessage = sendMessageProvider.provideSetPropertyMessage(propertyName, propertyValue);
        setPropertyMessageIndex = 0;
        try
        {
            byte[] sendMessage = setPropertyMessage.get(setPropertyMessageIndex);
            BluetoothGattService service = btGatt.getService(UUID.fromString("0000a108-0000-1000-8000-00805f9b34fb"));
            BluetoothGattCharacteristic characteristicWrite = service.getCharacteristic(UUID.fromString("0000a155-0000-1000-8000-00805f9b34fb"));
            characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            characteristicWrite.setValue(sendMessage);
            btGatt.writeCharacteristic(characteristicWrite);
            setPropertyMessageIndex++;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void communicateMain(@NonNull BluetoothDevice device)
    {
        device.connectGatt(context, false, this);
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
                    if (!onConnected)
                    {
                        onConnected = true;
                        Log.v(TAG, "  STATE_CONNECTED : discoverServices()");
                        gatt.discoverServices();
                    }
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.v(TAG, "  STATE_DISCONNECTED : disconnect() ");
                    gatt.disconnect();
                    onConnected = false;
                    break;

                default:
                    Log.v(TAG, " STATE_????? ");
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
                setCharacteristicNotification(gatt);
/*
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services)
                {
                    String serviceMessage = " S [" + service.getUuid() + "] " + service.getType();
                    Log.v(TAG, serviceMessage);
                    //addTextInformation(serviceMessage);
                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics)
                    {
                        String message = "   C [" + characteristic.getUuid() + "] " + characteristic.getPermissions() + " " + characteristic.getProperties();
                        Log.v(TAG, message);
                        //addTextInformation(message);
                        List<BluetoothGattDescriptor> descripters = characteristic.getDescriptors();
                        for (BluetoothGattDescriptor descriptor : descripters)
                        {
                            String descMessage = "     D [" + descriptor.getUuid() + "] " + descriptor.getPermissions() + " ";
                            Log.v(TAG, descMessage);
                            //addTextInformation(descMessage);
                        }
                    }
                }
*/
                btGatt = gatt;
                //expandMtu(gatt,512);
                queryDeviceProperty(gatt);  // expandMtu を使う場合にはここを呼ばない

                //gatt.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void queryDeviceProperty(BluetoothGatt gatt)
    {
        if (startQuery)
        {
            Log.v(TAG, " QUERY IS ALREADY STARTED.");
            return;
        }
        startQuery = true;
        try
        {
            sendMessageProvider.resetSequence();
            byte[] sendMessage = sendMessageProvider.provideMessage();
            BluetoothGattService service = gatt.getService(UUID.fromString("0000a108-0000-1000-8000-00805f9b34fb"));
            BluetoothGattCharacteristic characteristicWrite = service.getCharacteristic(UUID.fromString("0000a155-0000-1000-8000-00805f9b34fb"));
            characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            characteristicWrite.setValue(sendMessage);
            gatt.writeCharacteristic(characteristicWrite);
            //Log.v(TAG, " >> SEND [" + sendMessage.length + "] " + ret + " " + ret2 + " " + new String(sendMessage));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // startQuery = false;
    }

    private void addTextInformation(final String message)
    {
        try
        {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataUpdater.addText("\n" + message);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setCharacteristicNotification(BluetoothGatt gatt)
    {
        try
        {
            BluetoothGattService service = gatt.getService(UUID.fromString("0000a108-0000-1000-8000-00805f9b34fb"));
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("0000a156-0000-1000-8000-00805f9b34fb"));
            if (gatt.setCharacteristicNotification(characteristic, true))
            {
                Log.v(TAG, " setCharacteristicNotification is success. : " + characteristic.getUuid() + " (" + true + ") ");
            }
            else
            {
                Log.v(TAG, " setCharacteristicNotification is FAILURE. : " + characteristic.getUuid());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
    {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (status == BluetoothGatt.GATT_SUCCESS)
        {
            String value = characteristic.getStringValue(0);
            Log.v(TAG, " W: BluetoothGatt.GATT_SUCCESS " + characteristic.getUuid() + "  (" + value + ") ");

            if (sendMessageProvider.isMessageSending())
            {
                try
                {
                    byte[] sendMessage = sendMessageProvider.provideMessage();
                    BluetoothGattService service = gatt.getService(UUID.fromString("0000a108-0000-1000-8000-00805f9b34fb"));
                    BluetoothGattCharacteristic characteristicWrite = service.getCharacteristic(UUID.fromString("0000a155-0000-1000-8000-00805f9b34fb"));
                    characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    characteristicWrite.setValue(sendMessage);
                    gatt.writeCharacteristic(characteristicWrite);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if ((setPropertyMessageIndex > 0)&&(setPropertyMessage != null))
            {
                try
                {
                    byte[] sendMessage = setPropertyMessage.get(setPropertyMessageIndex);
                    BluetoothGattService service = gatt.getService(UUID.fromString("0000a108-0000-1000-8000-00805f9b34fb"));
                    BluetoothGattCharacteristic characteristicWrite = service.getCharacteristic(UUID.fromString("0000a155-0000-1000-8000-00805f9b34fb"));
                    characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    characteristicWrite.setValue(sendMessage);
                    gatt.writeCharacteristic(characteristicWrite);

                    setPropertyMessageIndex++;
                    if (setPropertyMessageIndex >= setPropertyMessage.size())
                    {
                        // メッセージ送信終了
                        setPropertyMessageIndex = -1;
                        setPropertyMessage = null;
                        System.gc();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            Log.v(TAG, " W: " + status + " " + characteristic.getUuid());
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
    {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (status == BluetoothGatt.GATT_SUCCESS)
        {
            Log.v(TAG, " R:BluetoothGatt.GATT_SUCCESS " + characteristic.getUuid());
        }
        else
        {
            Log.v(TAG, " R: " + status + " " + characteristic.getUuid());
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
    {
        try
        {
            byte[] receivedValue = characteristic.getValue();
            byte messageAttribute = receivedValue[0];
            receiveBuffer.write(receivedValue, 1, (receivedValue.length - 1));
            if (messageAttribute == (byte) 0x03)
            {
                String message = objectParser.parseData(receiveBuffer.toString());
                Log.v(TAG, " onCharacteristicChanged() : " + characteristic.getUuid() + "  " + message);
                addTextInformation(message);

                receiveBuffer.flush();
                receiveBuffer.reset();
                receiveBuffer = null;
                receiveBuffer = new ByteArrayOutputStream();
            }
            //else
            //{
            //    Log.v(TAG, " onCharacteristicChanged() : " + characteristic.getUuid() + " [" + messageAttribute + "]" + receivedValue.length);
            //}
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        super.onCharacteristicChanged(gatt, characteristic);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
    {
        Log.v(TAG, " onDescriptorWrite() : " + descriptor.getUuid() + " status : " + status);
        super.onDescriptorWrite(gatt, descriptor, status);
    }

    @Override
    public void setTokenId(int id)
    {
        sendMessageProvider.setTokenId(id);
    }

    @Override
    public void detectWifiKey(String ssId, String key)
    {
        Log.v(TAG, " WIFI KEY : " + ssId + " " + key);
        wifiSsId = ssId;
        wifiKey = key;
    }

    @Override
    public void onWifiConnected(boolean isConnect)
    {
        Log.v(TAG, " onWifiConnected : " + isConnect);

    }

    @Override
    public void messageFinished(boolean isFinished)
    {
        dataUpdater.enableOperation(isFinished);
    }

/*
    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status)
    {
        Log.v(TAG, " ===== onMtuChanged  =====");
        if (status == BluetoothGatt.GATT_SUCCESS)
        {
            mtuSizeIsExpanded = true;
        }
        super.onMtuChanged(gatt, mtu, status);

        Log.v(TAG, " MTU : " + mtu + " status : " + status);
        if (!startQuery)
        {
            queryDeviceProperty(gatt);
        }
    }

    private void expandMtu(BluetoothGatt gatt, int size)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            // MTUサイズの拡張を要求
            if (!mtuSizeIsExpanded)
            {
                if (!gatt.requestMtu(size))
                {
                    Log.v(TAG, "Failed to expand MTU value.");
                }
            }
        }
    }
*/
}
