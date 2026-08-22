package net.osdn.gokigen.blecontrol.lib.ui.brainwave

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import net.osdn.gokigen.blecontrol.lib.ble.MyBleAdapter
import net.osdn.gokigen.blecontrol.lib.ble.MyBleDevice
import net.osdn.gokigen.blecontrol.lib.ble.R
import net.osdn.gokigen.blecontrol.lib.data.brainwave.BrainwaveDataHolder
import net.osdn.gokigen.blecontrol.lib.ui.brainwave.BrainwaveConnection.SelectDevice

class BrainwaveMobileFragment : Fragment(), SelectDevice {
    private val TAG = toString()
    private var bondedDeviceList: List<MyBleDevice>? = null
    private var dataHolder: BrainwaveDataHolder? = null
    private var selectedDevicePosition = 0
    private var bleAdapter: MyBleAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val brainwaveViewModel = ViewModelProviders.of(this)
            .get<BrainwaveMobileViewModel>(BrainwaveMobileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_brainwave, container, false)
        val cameraLiveImageView = root.findViewById<BrainwaveRawGraphView>(R.id.cameraLiveImageView)
        bleAdapter = MyBleAdapter(getActivity()!!)
        bleAdapter!!.prepare()
        dataHolder = BrainwaveDataHolder(cameraLiveImageView, 16000)
        cameraLiveImageView.setDataHolder(dataHolder)
        val textView = root.findViewById<TextView>(R.id.text_brainwave)
        brainwaveViewModel.text.observe(getViewLifecycleOwner(), object : Observer<String?> {
            override fun onChanged(s: String?) {
                textView.setText(s)
            }
        })
        try {
            val context = getActivity()
            if (context != null) {
                // Bonded Device List
                prepareDeviceSelection(context, root)

                // Connect Button
                val loggingSwitch = root.findViewById<Switch?>(R.id.switch_logging)
                val eegConnection = BrainwaveConnection(
                    context,
                    this,
                    brainwaveViewModel,
                    dataHolder!!,
                    loggingSwitch
                )
                val queryButton = root.findViewById<Button?>(R.id.connect_to_eeg)
                if (queryButton != null) {
                    queryButton.setOnClickListener(eegConnection)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return root
    }

    /**
     * 通信先デバイスの設定 （選択できるようにする）
     * 
     * @param context  context
     * @param root     view root
     */
    private fun prepareDeviceSelection(context: FragmentActivity, root: View) {
        try {
            val selection_device = root.findViewById<Spinner>(R.id.spinner_selection_eeg_device)
            val adapter = ArrayAdapter<String?>(context, android.R.layout.simple_spinner_item)
            bondedDeviceList = bleAdapter!!.getBondedDeviceList()
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

    override fun getSelectedDeviceName(): String {
        var deviceName = ""
        try {
            deviceName = (bondedDeviceList!!.get(selectedDevicePosition)).name
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (deviceName)
    }
}
