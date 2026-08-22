package net.osdn.gokigen.blecontrol.lib.ui.fv100

import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.FragmentActivity
import net.osdn.gokigen.blecontrol.lib.ble.R
import net.osdn.gokigen.blecontrol.lib.ble.connect.ITextDataUpdater
import net.osdn.gokigen.blecontrol.lib.ble.connect.fv100.FV100BleDeviceConnector
import net.osdn.gokigen.blecontrol.lib.ui.SnackBarMessage
import net.osdn.gokigen.blecontrol.lib.ui.fv100.FV100PropertySetting.PropertySetter

class FV100DeviceQuery internal constructor(
    private val context: FragmentActivity,
    private val deviceInfo: DeviceInfo,
    private val viewModel: FV100ViewModel
) : View.OnClickListener, ITextDataUpdater, PropertySetter {
    private val TAG = toString()
    private val deviceConnector: FV100BleDeviceConnector = FV100BleDeviceConnector(context, this)
    private val messageToShow: SnackBarMessage

    init {
        this.messageToShow = SnackBarMessage(context, false)
    }

    private fun deviceQuery() {
        try {
            val deviceName = deviceInfo.getQueryDeviceName()
            viewModel.setText(context.getString(R.string.start_query) + " '" + deviceName + "'")
            if (deviceName.length > 1) {
                val thread = Thread(object : Runnable {
                    override fun run() {
                        deviceConnector.query_to_device(deviceName)
                    }
                })
                thread.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dataReload() {
        try {
            val thread = Thread(object : Runnable {
                override fun run() {
                    deviceConnector.reload_device_information()
                }
            })
            thread.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun connectToCamera() {
        try {
            val thread = Thread(object : Runnable {
                override fun run() {
                    deviceConnector.connect_to_camera_via_wifi()
                }
            })
            thread.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View) {
        val id = v.getId()
        when (id) {
            R.id.query_to_device -> deviceQuery()
            R.id.reload_button -> dataReload()
            R.id.wifi_connect_button -> connectToCamera()
            else -> Log.v(TAG, " onClick : " + id)
        }
    }

    override fun setText(data: String?) {
        viewModel.setText(data ?:"")
    }

    override fun addText(data: String?) {
        viewModel.addText(data ?:"")
    }

    override fun showSnackBar(message: String?) {
        messageToShow.showMessage(message ?:"")
    }

    override fun showSnackBar(rscId: Int) {
        messageToShow.showMessage(rscId)
    }

    override fun enableOperation(isEnable: Boolean) {
        try {
            context.runOnUiThread(object : Runnable {
                override fun run() {
                    val wifiConnectButton =
                        context.findViewById<ImageButton?>(R.id.wifi_connect_button)
                    if (wifiConnectButton != null) {
                        wifiConnectButton.setEnabled(isEnable)
                        wifiConnectButton.setVisibility((if (isEnable) View.INVISIBLE else View.INVISIBLE))
                    }

                    val imageSizeButton =
                        context.findViewById<ImageButton?>(R.id.change_image_size_button)
                    if (imageSizeButton != null) {
                        imageSizeButton.setEnabled(isEnable)
                        imageSizeButton.setVisibility((if (isEnable) View.VISIBLE else View.INVISIBLE))
                    }

                    val videoSizeButton =
                        context.findViewById<ImageButton?>(R.id.change_video_size_button)
                    if (videoSizeButton != null) {
                        videoSizeButton.setEnabled(isEnable)
                        videoSizeButton.setVisibility((if (isEnable) View.VISIBLE else View.INVISIBLE))
                    }
                    //Log.v(TAG, " >> ITextDataUpdater::enableOperation() : " + isEnable);
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setProperty(propertyName: String, propertyValue: String) {
        try {
            val thread = Thread @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT){
                try { deviceConnector.setProperty(propertyName, propertyValue) } catch (_: Exception) { }
            }
            thread.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal interface DeviceInfo {
        fun getQueryDeviceName(): String
    }
}
