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

import java.util.List;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class FV100Communicator  extends BluetoothGattCallback
{
    private String TAG = toString();
    private final FragmentActivity context;
    private final ITextDataUpdater dataUpdater;

    FV100Communicator(@NonNull FragmentActivity context, @NonNull ITextDataUpdater dataUpdater)
    {
        this.context = context;
        this.dataUpdater = dataUpdater;
    }

    void startCommunicate(@Nullable final BluetoothDevice device)
    {
        String deviceName = (device != null) ? device.getName() : "";
        dataUpdater.setText(" FOUND BLE DEVICE. : " + deviceName);
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
            addTextInformation(" -----");
            try
            {
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services)
                {
                    String serviceMessage = " S [" + service.getUuid() + "] " + service.getType();
                    Log.v(TAG, serviceMessage);
                    addTextInformation(serviceMessage);
                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics)
                    {
                        String message = "   C [" + characteristic.getUuid() + "] " + characteristic.getPermissions() + " " + characteristic.getProperties();
                        Log.v(TAG, message);
                        addTextInformation(message);
                        List<BluetoothGattDescriptor> descripters = characteristic.getDescriptors();
                        for (BluetoothGattDescriptor descriptor : descripters)
                        {
                            String descMessage = "     D [" + descriptor.getUuid() + "] " + descriptor.getPermissions() + " ";
                            Log.v(TAG, descMessage);
                            addTextInformation(descMessage);
                        }
                    }
                }
                addTextInformation(" -----");

                Log.v(TAG, " ===== QUERY PROPERTY START  =====");
                queryDeviceProperty(gatt);
                Log.v(TAG, " ===== QUERY PROPERTY FINISH =====");
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
        try
        {
            String sendMessage = createSendMessage(257, 0);
            BluetoothGattService service = gatt.getService(UUID.fromString("0000a108-0000-1000-8000-00805f9b34fb"));
            BluetoothGattCharacteristic characteristicWrite = service.getCharacteristic(UUID.fromString("0000a155-0000-1000-8000-00805f9b34fb"));
            BluetoothGattCharacteristic characteristicRead = service.getCharacteristic(UUID.fromString("0000a156-0000-1000-8000-00805f9b34fb"));

            characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            boolean ret = characteristicWrite.setValue(sendMessage);
            boolean ret2 = gatt.writeCharacteristic(characteristicWrite);
            Log.v(TAG, " >>>>> SEND : " + sendMessage + "  [" + sendMessage.length() + "] " + ret + " " + ret2);
            //gatt.readCharacteristic(characteristicRead);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String createSendMessage(int msg_id, int token)
    {
        return ("{\"msg_id\" :" + msg_id + ",\"token\" :" + token + "}");
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

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
    {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (status == BluetoothGatt.GATT_SUCCESS)
        {
            Log.v(TAG, " W: BluetoothGatt.GATT_SUCCESS " + characteristic.getUuid());
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
        super.onCharacteristicChanged(gatt, characteristic);
        Log.v(TAG, " onCharacteristicChanged()");
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status)
    {
        super.onMtuChanged(gatt, mtu, status);
        Log.v(TAG, " MTU : " + mtu);
    }
}
