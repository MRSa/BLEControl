package net.osdn.gokigen.blecontrol.lib.ui.fv100;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FV100ViewModel extends ViewModel
{
    private MutableLiveData<String> mText;

    public FV100ViewModel()
    {
        mText = new MutableLiveData<>();
        mText.setValue(" ");
    }

    void setText(@NonNull String data)
    {
        mText.setValue(data);
    }

    public LiveData<String> getText()
    {
        return (mText);
    }
}
