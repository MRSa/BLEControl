package net.osdn.gokigen.blecontrol.lib.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import net.osdn.gokigen.blecontrol.lib.ble.R

class HomeFragment : Fragment() {
    private var homeViewModel: HomeViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProviders.of(this).get<HomeViewModel>(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView = root.findViewById<TextView>(R.id.text_home)
        homeViewModel!!.text!!.observe(getViewLifecycleOwner(), object : Observer<String?> {
            override fun onChanged(s: String?) {
                textView.setText(s)
            }
        })
        return root
    }
}