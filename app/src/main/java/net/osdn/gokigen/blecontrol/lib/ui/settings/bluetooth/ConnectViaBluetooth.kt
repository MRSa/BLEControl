package net.osdn.gokigen.blecontrol.lib.ui.settings.bluetooth

import android.app.Activity
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.EditText
import androidx.fragment.app.Fragment
import net.osdn.gokigen.blecontrol.lib.ble.R
import net.osdn.gokigen.blecontrol.lib.ble.connect.ICameraBleProperty
import net.osdn.gokigen.blecontrol.lib.ble.connect.ICameraPowerOn
import net.osdn.gokigen.blecontrol.lib.ble.connect.ICameraPowerOn.PowerOnCameraCallback
import net.osdn.gokigen.blecontrol.lib.ble.connect.PowerOnCamera
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class ConnectViaBluetooth internal constructor(private val fragment: Fragment) :
    OnLongClickListener, View.OnClickListener, PowerOnCameraCallback {
    private val TAG = toString()

    init {
        setBleCameraSet(1, "B500_21028637", "164309", "DEFAULT")
    }

    override fun onLongClick(view: View?): Boolean {
        Log.v(TAG, " onLongClick()")

        return false
    }

    override fun onClick(view: View?) {
        Log.v(TAG, " onClick()")

        val activity: Activity? = fragment.getActivity()
        if (activity != null) {
            val deviceName = activity.findViewById<EditText?>(R.id.deviceName)
            val devicePass = activity.findViewById<EditText?>(R.id.devicePass)

            val connection = PowerOnCamera(activity)
            val device = if (deviceName == null) "" else deviceName.getText().toString()
            val pass = if (devicePass == null) "" else devicePass.getText().toString()

            val thread = Thread(object : Runnable {
                override fun run() {
                    try {
                        startPowerOnCamera(connection, device, pass)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
            try {
                thread.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startPowerOnCamera(
        connection: ICameraPowerOn,
        deviceName: String?,
        passCode: String?
    ) {
        try {
            Log.v(TAG, " startPowerOnCamera()")
            Log.v(TAG, " device Name : " + deviceName + "  pass : " + passCode)

            setBleCameraSet(2, deviceName, passCode, "INFO")

            connection.wakeup(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun wakeupExecuted(isExecute: Boolean) {
        Log.v(TAG, " wakeupExecuted() : " + isExecute)
    }

    /**
     * index : 1 ～ ICameraBleProperty.MAX_STORE_PROPERTIES
     * name  : device name
     * code  : passcode
     * info  : information
     */
    private fun setBleCameraSet(index: Int, name: String?, code: String?, info: String?) {
        val id = String.format(Locale.ENGLISH, "%03d", index)

        val namePrefKey = id + ICameraBleProperty.NAME_KEY
        val codePrefKey = id + ICameraBleProperty.CODE_KEY
        val infoPrefKey = id + ICameraBleProperty.DATE_KEY

        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        val dateInfo = dateFormat.format(Date())

        try {
            val preferences = PreferenceManager.getDefaultSharedPreferences(fragment.getActivity())
            val editor = preferences.edit()

            editor.putString(namePrefKey, name)
            editor.putString(codePrefKey, code)
            editor.putString(infoPrefKey, dateInfo)

            editor.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.v(
            TAG,
            "setBleCameraSet() REGISTERED : [" + id + "] " + name + " " + code + " " + dateInfo + " (" + info + ")"
        )
    }
}
