package net.osdn.gokigen.blecontrol.lib.ble.connect.fv100

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.fragment.app.FragmentActivity
import net.osdn.gokigen.blecontrol.lib.ble.R
import net.osdn.gokigen.blecontrol.lib.ble.connect.ITextDataUpdater
import net.osdn.gokigen.blecontrol.lib.ble.connect.fv100.FV100ObjectParser.ReceivedDataNotify
import net.osdn.gokigen.blecontrol.lib.ble.connect.fv100.FV100SendMessageProvider.MessageSequenceNotify
import net.osdn.gokigen.blecontrol.lib.wifi.WifiConnector
import net.osdn.gokigen.blecontrol.lib.wifi.WifiConnector.WifiConnectNotify
import java.io.ByteArrayOutputStream
import java.util.UUID


@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
internal class FV100Communicator(
    private val context: FragmentActivity,
    private val dataUpdater: ITextDataUpdater
) : BluetoothGattCallback(), ReceivedDataNotify, WifiConnectNotify, MessageSequenceNotify {
    private val TAG: String = toString()

    //private boolean mtuSizeIsExpanded = false;
    private var startQuery = false
    private var onConnected = false
    private var receiveBuffer: ByteArrayOutputStream?
    private val objectParser: FV100ObjectParser
    private val sendMessageProvider: FV100SendMessageProvider
    private val wifiConnector: WifiConnector
    private var btGatt: BluetoothGatt? = null
    private var wifiSsId: String? = null
    private var wifiKey: String? = null

    private var setPropertyMessage: MutableList<ByteArray?>? = null
    private var setPropertyMessageIndex = -1

    init {
        this.receiveBuffer = ByteArrayOutputStream()
        this.objectParser = FV100ObjectParser(this)
        this.sendMessageProvider = FV100SendMessageProvider(this)
        this.wifiConnector = WifiConnector(context, dataUpdater)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun startCommunicate(device: BluetoothDevice?) {
        val deviceName = if (device != null) device.getName() else ""
        dataUpdater.setText(" FOUND : " + deviceName)
        try {
            val thread = Thread(object : Runnable {
                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun run() {
                    try {
                        if (device != null) {
                            communicateMain(device)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
            thread.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun data_reload() {
        Log.v(TAG, " RELOAD ")
        context.runOnUiThread(object : Runnable {
            override fun run() {
                if (btGatt != null) {
                    // 画面をクリアする
                    dataUpdater.setText(" ")

                    // 最初から情報を取り直す。
                    sendMessageProvider.resetSequence()
                    startQuery = false
                    try {
                        val thread = Thread(object : Runnable {
                            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                            override fun run() {
                                queryDeviceProperty(btGatt!!)
                            }
                        })
                        thread.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    fun connect_wifi() {
        try {
            if ((wifiSsId != null) && (wifiKey != null)) {
                wifiConnector.connectToWifi(wifiSsId!!, wifiKey!!, this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setProperty(propertyName: String, propertyValue: String) {
        val message = " " + propertyName + " : " + propertyValue
        Log.v(TAG, message)
        if (btGatt == null) {
            // BluetoothGatt が設定されていない場合...
            context.runOnUiThread(object : Runnable {
                override fun run() {
                    dataUpdater.showSnackBar(context.getString(R.string.ble_not_connected))
                }
            })
            return
        }
        if ((setPropertyMessage != null) || (sendMessageProvider.isMessageSending)) {
            // ただいま通信中なので何もしないで終わる
            context.runOnUiThread(object : Runnable {
                override fun run() {
                    dataUpdater.showSnackBar(context.getString(R.string.now_ble_communicating))
                }
            })
            return
        }
        addTextInformation(message)
        setPropertyMessage =
            sendMessageProvider.provideSetPropertyMessage(propertyName, propertyValue)
        setPropertyMessageIndex = 0
        try {
            val sendMessage = setPropertyMessage!!.get(setPropertyMessageIndex)
            val service =
                btGatt!!.getService(UUID.fromString("0000a108-0000-1000-8000-00805f9b34fb"))
            val characteristicWrite =
                service.getCharacteristic(UUID.fromString("0000a155-0000-1000-8000-00805f9b34fb"))
            characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            characteristicWrite.setValue(sendMessage)
            btGatt!!.writeCharacteristic(characteristicWrite)
            setPropertyMessageIndex++
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun communicateMain(device: BluetoothDevice) {
        device.connectGatt(context, false, this)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        Log.v(TAG, " onConnectionStateChange() : [" + status + " -> " + newState + "]")
        try {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> if (!onConnected) {
                    onConnected = true
                    Log.v(TAG, "  STATE_CONNECTED : discoverServices()")
                    gatt.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.v(TAG, "  STATE_DISCONNECTED : disconnect() ")
                    gatt.disconnect()
                    onConnected = false
                }

                else -> Log.v(TAG, " STATE_????? ")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        Log.v(TAG, " onServicesDiscovered()  [" + status + "]")
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.v(TAG, " ----- GATT_SUCCESS -----")
            try {
                setCharacteristicNotification(gatt)
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
                btGatt = gatt
                //expandMtu(gatt,512);
                queryDeviceProperty(gatt) // expandMtu を使う場合にはここを呼ばない

                //gatt.close();
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun queryDeviceProperty(gatt: BluetoothGatt) {
        if (startQuery) {
            Log.v(TAG, " QUERY IS ALREADY STARTED.")
            return
        }
        startQuery = true
        try {
            sendMessageProvider.resetSequence()
            val sendMessage = sendMessageProvider.provideMessage()
            val service = gatt.getService(UUID.fromString("0000a108-0000-1000-8000-00805f9b34fb"))
            val characteristicWrite =
                service.getCharacteristic(UUID.fromString("0000a155-0000-1000-8000-00805f9b34fb"))
            characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            characteristicWrite.setValue(sendMessage)
            gatt.writeCharacteristic(characteristicWrite)
            //Log.v(TAG, " >> SEND [" + sendMessage.length + "] " + ret + " " + ret2 + " " + new String(sendMessage));
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // startQuery = false;
    }

    private fun addTextInformation(message: String?) {
        try {
            context.runOnUiThread(object : Runnable {
                override fun run() {
                    dataUpdater.addText("\n" + message)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun setCharacteristicNotification(gatt: BluetoothGatt) {
        try {
            val service = gatt.getService(UUID.fromString("0000a108-0000-1000-8000-00805f9b34fb"))
            val characteristic =
                service.getCharacteristic(UUID.fromString("0000a156-0000-1000-8000-00805f9b34fb"))
            if (gatt.setCharacteristicNotification(characteristic, true)) {
                Log.v(
                    TAG,
                    " setCharacteristicNotification is success. : " + characteristic.getUuid() + " (" + true + ") "
                )
            } else {
                Log.v(
                    TAG,
                    " setCharacteristicNotification is FAILURE. : " + characteristic.getUuid()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            val value = characteristic.getStringValue(0)
            Log.v(
                TAG,
                " W: BluetoothGatt.GATT_SUCCESS " + characteristic.getUuid() + "  (" + value + ") "
            )

            if (sendMessageProvider.isMessageSending) {
                try {
                    val sendMessage = sendMessageProvider.provideMessage()
                    val service =
                        gatt.getService(UUID.fromString("0000a108-0000-1000-8000-00805f9b34fb"))
                    val characteristicWrite =
                        service.getCharacteristic(UUID.fromString("0000a155-0000-1000-8000-00805f9b34fb"))
                    characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                    characteristicWrite.setValue(sendMessage)
                    gatt.writeCharacteristic(characteristicWrite)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if ((setPropertyMessageIndex > 0) && (setPropertyMessage != null)) {
                try {
                    val sendMessage = setPropertyMessage!!.get(setPropertyMessageIndex)
                    val service =
                        gatt.getService(UUID.fromString("0000a108-0000-1000-8000-00805f9b34fb"))
                    val characteristicWrite =
                        service.getCharacteristic(UUID.fromString("0000a155-0000-1000-8000-00805f9b34fb"))
                    characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                    characteristicWrite.setValue(sendMessage)
                    gatt.writeCharacteristic(characteristicWrite)

                    setPropertyMessageIndex++
                    if (setPropertyMessageIndex >= setPropertyMessage!!.size) {
                        // メッセージ送信終了
                        setPropertyMessageIndex = -1
                        setPropertyMessage = null
                        System.gc()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            Log.v(TAG, " W: " + status + " " + characteristic.getUuid())
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.v(TAG, " R:BluetoothGatt.GATT_SUCCESS " + characteristic.getUuid())
        } else {
            Log.v(TAG, " R: " + status + " " + characteristic.getUuid())
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic
    ) {
        try {
            val receivedValue = characteristic.getValue()
            val messageAttribute = receivedValue[0]
            receiveBuffer!!.write(receivedValue, 1, (receivedValue.size - 1))
            if (messageAttribute == 0x03.toByte()) {
                val message = objectParser.parseData(receiveBuffer.toString())
                Log.v(
                    TAG,
                    " onCharacteristicChanged() : " + characteristic.getUuid() + "  " + message
                )
                addTextInformation(message)

                receiveBuffer!!.flush()
                receiveBuffer!!.reset()
                receiveBuffer = null
                receiveBuffer = ByteArrayOutputStream()
            }
            //else
            //{
            //    Log.v(TAG, " onCharacteristicChanged() : " + characteristic.getUuid() + " [" + messageAttribute + "]" + receivedValue.length);
            //}
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onCharacteristicChanged(gatt, characteristic)
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        Log.v(TAG, " onDescriptorWrite() : " + descriptor.getUuid() + " status : " + status)
        super.onDescriptorWrite(gatt, descriptor, status)
    }

    override fun setTokenId(id: Int) {
        sendMessageProvider.setTokenId(id)
    }

    override fun detectWifiKey(ssId: String?, key: String?) {
        Log.v(TAG, " WIFI KEY : " + ssId + " " + key)
        wifiSsId = ssId
        wifiKey = key
    }

    override fun onWifiConnected(isConnect: Boolean) {
        Log.v(TAG, " onWifiConnected : " + isConnect)
    }

    override fun messageFinished(isFinished: Boolean) {
        dataUpdater.enableOperation(isFinished)
    } /*
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
