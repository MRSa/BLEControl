package net.osdn.gokigen.blecontrol.lib.ui.fv100

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FV100ViewModel : ViewModel() {
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

    val text: LiveData<String?>?
        get() = (mText)
}
