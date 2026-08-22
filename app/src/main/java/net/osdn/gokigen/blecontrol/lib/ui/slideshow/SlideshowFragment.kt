package net.osdn.gokigen.blecontrol.lib.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import net.osdn.gokigen.blecontrol.lib.ble.R

class SlideshowFragment : Fragment() {
    private var slideshowViewModel: SlideshowViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        slideshowViewModel =
            ViewModelProviders.of(this).get<SlideshowViewModel>(SlideshowViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_slideshow, container, false)
        val textView = root.findViewById<TextView>(R.id.text_slideshow)
        slideshowViewModel!!.text!!.observe(this, object : Observer<String?> {
            override fun onChanged(s: String?) {
                textView.setText(s)
            }
        })
        return root
    }
}