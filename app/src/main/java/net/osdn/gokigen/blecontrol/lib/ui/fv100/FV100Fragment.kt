package net.osdn.gokigen.blecontrol.lib.ui.fv100

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import net.osdn.gokigen.blecontrol.lib.ble.MyBleAdapter
import net.osdn.gokigen.blecontrol.lib.ble.MyBleDevice
import net.osdn.gokigen.blecontrol.lib.ble.R
import net.osdn.gokigen.blecontrol.lib.ui.fv100.FV100DeviceQuery.DeviceInfo

class FV100Fragment : Fragment(), DeviceInfo {
    private val TAG = toString()
    private var fv100ViewModel: FV100ViewModel? = null
    private var bondedDeviceList: MutableList<MyBleDevice>? = null
    private var selectedDevicePosition = 0
    private var bleAdapter: MyBleAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, " onCreateView : FV100")

        fv100ViewModel = ViewModelProviders.of(this).get<FV100ViewModel>(FV100ViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_fv100, container, false)
        val textView = root.findViewById<TextView>(R.id.text_device_fv100)
        bleAdapter = MyBleAdapter(requireActivity())
        bleAdapter!!.prepare()
        fv100ViewModel!!.text!!.observe(getViewLifecycleOwner(), object : Observer<String?> {
            override fun onChanged(s: String?) {
                textView.setText(s)
            }
        })
        try {
            val context = getActivity()
            if (context != null) {
                // Bonded Device List
                prepareDeviceSelection(context, root)

                // Device Query Button
                val queryButton = root.findViewById<Button?>(R.id.query_to_device)
                val deviceQuery = FV100DeviceQuery(context, this, fv100ViewModel!!)
                val propertySetting = FV100PropertySetting(context, deviceQuery)
                if (queryButton != null) {
                    queryButton.setOnClickListener(deviceQuery)
                }

                // Reload Button
                val reloadButton = root.findViewById<ImageButton?>(R.id.reload_button)
                if (reloadButton != null) {
                    reloadButton.setOnClickListener(deviceQuery)
                }

                // WiFi Connect Button
                val wifiConnectButton = root.findViewById<ImageButton?>(R.id.wifi_connect_button)
                if (wifiConnectButton != null) {
                    wifiConnectButton.setOnClickListener(deviceQuery)
                }

                // Change Image Size Button
                val imageSizeButton = root.findViewById<ImageButton?>(R.id.change_image_size_button)
                if (imageSizeButton != null) {
                    imageSizeButton.setOnClickListener(propertySetting)
                }

                // Change Video size Button
                val videoSizeButton = root.findViewById<ImageButton?>(R.id.change_video_size_button)
                if (videoSizeButton != null) {
                    videoSizeButton.setOnClickListener(propertySetting)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (root)
    }

    /**
     * 通信先デバイスの設定 （選択できるようにする）
     * 
     * @param context  context
     * @param root     view root
     */
    private fun prepareDeviceSelection(context: FragmentActivity, root: View) {
        try {
            val selection_device = root.findViewById<Spinner>(R.id.spinner_selection_device)
            val adapter = ArrayAdapter<String?>(context, android.R.layout.simple_spinner_item)
            bondedDeviceList = bleAdapter!!.getBondedDeviceList() as MutableList<MyBleDevice>?
            for (device in bondedDeviceList!!) {
                adapter.add(device.name)
            }
            selection_device.setAdapter(adapter)
            selection_device.setSelection(selectedDevicePosition)
            selection_device.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    Log.v(TAG, "onItemSelected : " + position + " (" + id + ")")
                    try {
                        selectedDevicePosition = position
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Log.v(TAG, "onNothingSelected")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getQueryDeviceName(): String {
        var deviceName = ""
        try {
            deviceName = bondedDeviceList!!.get(selectedDevicePosition).name
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (deviceName)
    }
}
