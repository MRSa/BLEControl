package net.osdn.gokigen.blecontrol.lib.ui.settings.tool

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ToolSettingsViewModel : ViewModel() {
    private val mText: MutableLiveData<String?>?

    init {
        mText = MutableLiveData<String?>()
        mText.setValue("This is tool settings fragment")
    }

    val text: LiveData<String?>?
        get() = mText
}