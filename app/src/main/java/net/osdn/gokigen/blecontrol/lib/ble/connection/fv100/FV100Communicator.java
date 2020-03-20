package net.osdn.gokigen.blecontrol.lib.ble.connection.fv100;

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

import net.osdn.gokigen.blecontrol.lib.ble.connection.ITextDataUpdater;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class FV100Communicator  extends BluetoothGattCallback
{
    private String TAG = toString();
    private final FragmentActivity context;
    private final ITextDataUpdater dataUpdater;
    //private boolean mtuSizeIsExpanded = false;
    private boolean startQuery = false;
    private boolean onConnected = false;
    private boolean secondMessageSend = false;
    private ByteArrayOutputStream receiveBuffer;
    private final FV100ObjectPaser objectParser;

    FV100Communicator(@NonNull FragmentActivity context, @NonNull ITextDataUpdater dataUpdater)
    {
        this.context = context;
        this.dataUpdater = dataUpdater;
        this.receiveBuffer = new ByteArrayOutputStream();
        this.objectParser = new FV100ObjectPaser();
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
                //expandMtu(gatt,512);
                queryDeviceProperty(gatt);

                //gatt.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            Log.v(TAG, " ------------------------");
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
            byte[] messageToSend = createSendMessage(257, 0);
            byte[] sendMessage = Arrays.copyOfRange(messageToSend, 0, 20);
            BluetoothGattService service = gatt.getService(UUID.fromString("0000a108-0000-1000-8000-00805f9b34fb"));
            BluetoothGattCharacteristic characteristicWrite = service.getCharacteristic(UUID.fromString("0000a155-0000-1000-8000-00805f9b34fb"));
            characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            boolean ret = characteristicWrite.setValue(sendMessage);
            boolean ret2 = gatt.writeCharacteristic(characteristicWrite);
            Log.v(TAG, " >> SEND [" + sendMessage.length + "] " + ret + " " + ret2 + " " + new String(sendMessage));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // startQuery = false;
    }

    private byte[] createSendMessage(int msg_id, int token)
    {
        String data = ("{\"msg_id\":" + msg_id + ",\"token\":" + token + "}");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try
        {
            byte[] header = {(byte) 0x01, (byte) 0x00, (byte) 0x18};
            output.write(header);
            output.write(data.getBytes());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (output.toByteArray());
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

/*
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

            if (!secondMessageSend)
            {
                secondMessageSend = true;
                try {
                    byte[] sendMessage = {0x03, 0x6b, 0x65, 0x6e, 0x22, 0x3a, 0x30, 0x7d};
                    BluetoothGattService service = gatt.getService(UUID.fromString("0000a108-0000-1000-8000-00805f9b34fb"));
                    BluetoothGattCharacteristic characteristicWrite = service.getCharacteristic(UUID.fromString("0000a155-0000-1000-8000-00805f9b34fb"));
                    characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    boolean ret = characteristicWrite.setValue(sendMessage);
                    boolean ret2 = gatt.writeCharacteristic(characteristicWrite);
                    Log.v(TAG, " << SEND [" + sendMessage.length + "] " + ret + " " + ret2 + " " + new String(sendMessage));
                } catch (Exception e) {
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
            else
            {
                Log.v(TAG, " onCharacteristicChanged() : " + characteristic.getUuid() + " [" + messageAttribute + "]" + receivedValue.length);
            }
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
*/
}
