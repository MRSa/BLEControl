package net.osdn.gokigen.blecontrol.lib.ui.slideshow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SlideshowViewModel : ViewModel() {
    private val mText: MutableLiveData<String?>?

    init {
        mText = MutableLiveData<String?>()
        mText.setValue("This is slideshow fragment")
    }

    val text: LiveData<String?>?
        get() = mText
}