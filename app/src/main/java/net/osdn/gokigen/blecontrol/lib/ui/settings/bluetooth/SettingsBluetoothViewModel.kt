package net.osdn.gokigen.blecontrol.lib.ui.settings.bluetooth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsBluetoothViewModel : ViewModel() {
    private val mText: MutableLiveData<String?>?

    init {
        mText = MutableLiveData<String?>()
        mText.setValue("Bluetooth Connection Test")
    }

    val text: LiveData<String?>?
        get() = mText
}