package net.osdn.gokigen.blecontrol.lib.ui.brainwave

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BrainwaveMobileViewModel : ViewModel() {
    private val mText: MutableLiveData<String?>

    init {
        mText = MutableLiveData<String?>()
        mText.setValue(" ")
    }

    fun setText(data: String) {
        mText.setValue(data)
    }

    fun addText(data: String) {
        mText.setValue(mText.getValue() + data)
    }

    val text: LiveData<String?>
        get() = mText
}
