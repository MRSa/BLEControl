package net.osdn.gokigen.blecontrol.lib.ui.settings.bluetooth

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import net.osdn.gokigen.blecontrol.lib.ble.R

class SettingsBluetoothFragment : Fragment() {
    private var settingsBluetoothViewModel: SettingsBluetoothViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsBluetoothViewModel = ViewModelProviders.of(this)
            .get<SettingsBluetoothViewModel>(SettingsBluetoothViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings_bluetooth, container, false)
        val textView = root.findViewById<TextView>(R.id.text_settings_bluetooth)
        settingsBluetoothViewModel!!.text!!.observe(
            getViewLifecycleOwner(),
            object : Observer<String?> {
                override fun onChanged(s: String?) {
                    textView.setText(s)
                }
            })

        val btnConnect = root.findViewById<Button>(R.id.btnConnect)
        val btConnection = ConnectViaBluetooth(this)
        btnConnect.setOnClickListener(btConnection)
        btnConnect.setOnLongClickListener(btConnection)

        val btnWifi = root.findViewById<Button>(R.id.btnWifiSet)
        btnWifi.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                try {
                    // Wifi 設定画面を表示する
                    getActivity()!!.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })

        return (root)
    }
}
