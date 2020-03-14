package net.osdn.gokigen.blecontrol.lib.ui.settings.tool;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ToolSettingsViewModel extends ViewModel
{

    private MutableLiveData<String> mText;

    public ToolSettingsViewModel()
    {
        mText = new MutableLiveData<>();
        mText.setValue("This is tool settings fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}