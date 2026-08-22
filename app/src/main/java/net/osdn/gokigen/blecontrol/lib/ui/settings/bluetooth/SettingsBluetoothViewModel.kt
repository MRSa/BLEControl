package net.osdn.gokigen.blecontrol.lib.ui.settings.bluetooth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsBluetoothViewModel extends ViewModel
{
    private MutableLiveData<String> mText;

    public SettingsBluetoothViewModel()
    {
        mText = new MutableLiveData<>();
        mText.setValue("Bluetooth Connection Test");
    }

    public LiveData<String> getText() {
        return mText;
    }
}