package net.osdn.gokigen.blecontrol.lib.ble.connect.eeg

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.fragment.app.FragmentActivity
import net.osdn.gokigen.blecontrol.lib.ble.R
import net.osdn.gokigen.blecontrol.lib.ble.connect.BleDeviceFinder
import net.osdn.gokigen.blecontrol.lib.ble.connect.BleDeviceFinder.BleScanResult
import net.osdn.gokigen.blecontrol.lib.ble.connect.ITextDataUpdater
import net.osdn.gokigen.blecontrol.lib.data.brainwave.BrainwaveFileLogger
import net.osdn.gokigen.blecontrol.lib.data.brainwave.IBrainwaveDataReceiver
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID


class MindWaveCommunication(
    private val context: FragmentActivity,
    private val dataUpdater: ITextDataUpdater,
    private val dataReceiver: IBrainwaveDataReceiver
) : BleScanResult {
    private val TAG: String = toString()

    private var deviceFinder: BleDeviceFinder? = null
    private var fileLogger: BrainwaveFileLogger? = null
    private var foundDevice = false
    private var loggingFlag = false

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.deviceFinder = BleDeviceFinder(context, dataUpdater, this)
        }
    }

    fun connect(deviceName: String, loggingFlag: Boolean) {
        Log.v(
            TAG,
            " BrainWaveMobileCommunicator::connect() : $deviceName Logging : $loggingFlag"
        )
        setText(context.getString(R.string.start_query) + " '" + deviceName + "'")
        try {
            this.loggingFlag = loggingFlag
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // BLE のサービスを取得
                if (deviceFinder != null) {
                    // BLEデバイスをスキャンする
                    foundDevice = false
                    deviceFinder!!.reset()
                    deviceFinder!!.startScan(deviceName)
                }
            } else {
                // Androidのバージョンが低かった
                dataUpdater.showSnackBar(R.string.not_support_android_version)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setText(message: String) {
        dataUpdater.setText(message)
    }

    private fun addText(message: String) {
        dataUpdater.addText(message)
    }

    private fun parseReceivedData(data: ByteArray) {
        // 受信データブロック１つ分
        try {
            if (data.size <= 3) {
                // ヘッダ部しか入っていない...無視する
                return
            }
            val length = data[2]
            if (data.size < (length + 2)) {
                // データが最小サイズに満たない...無視する
                return
            }

            if ((data.size == 8) || (data.size == 9)) {
                var value = ((data[5].toInt() and 0xff) * 256) + (data[6].toInt() and 0xff)
                if (value > 32768) {
                    value = value - 65536
                }
                dataReceiver.receivedRawData(value)
                return
            }
            dataReceiver.receivedSummaryData(data)
            if (fileLogger != null) {
                // ファイルにサマリーデータを出力する
                fileLogger!!.outputSummaryData(data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun serialCommunicationMain(btSocket: BluetoothSocket) {
        var inputStream: InputStream? = null
        try {
            btSocket.connect()
            inputStream = btSocket.getInputStream()
        } catch (e: Exception) {
            Log.e(TAG, "Fail to accept.", e)
        }
        if (inputStream == null) {
            return
        }

        if (loggingFlag) {
            try {
                // ログ出力を指示されていた場合...ファイル出力クラスを作成しておく
                fileLogger = BrainwaveFileLogger(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // シリアルデータの受信メイン部分
        var previousData = 0xff.toByte()
        var outputStream: ByteArrayOutputStream? = null
        while (foundDevice) {
            try {
                val data = inputStream.read()
                val byteData = (data and 0xff).toByte()
                if ((previousData == byteData) && (byteData == 0xaa.toByte())) {
                    // 先頭データを見つけた。 （0xaa 0xaa がヘッダ）
                    if (outputStream != null) {
                        parseReceivedData(outputStream.toByteArray())
                        outputStream = null
                    }
                    outputStream = ByteArrayOutputStream()
                    outputStream.write(0xaa.toByte().toInt())
                    outputStream.write(0xaa.toByte().toInt())
                } else {
                    outputStream?.write(byteData.toInt())
                }
                previousData = byteData
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        try {
            btSocket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun foundBleDevice(device: BluetoothDevice) {
        try {
            if (foundDevice) {
                // すでに見つかっている
                Log.v(TAG, " ALREADY FIND.")
                return
            }
            foundDevice = true
            val btSocket =
                device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
            val thread = Thread {
                try {
                    serialCommunicationMain(btSocket!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (btSocket != null) {
                thread.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
