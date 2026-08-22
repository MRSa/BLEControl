package net.osdn.gokigen.blecontrol.lib.ui.settings.tool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import net.osdn.gokigen.blecontrol.lib.ble.R

class ToolSettingsFragment : Fragment() {
    private var toolSettingsViewModel: ToolSettingsViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        toolSettingsViewModel =
            ViewModelProviders.of(this)
                .get<ToolSettingsViewModel>(ToolSettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_setting_tools, container, false)
        val textView = root.findViewById<TextView>(R.id.text_tools)
        toolSettingsViewModel!!.text!!.observe(getViewLifecycleOwner(), object : Observer<String?> {
            override fun onChanged(s: String?) {
                textView.setText(s)
            }
        })
        return root
    }
}
