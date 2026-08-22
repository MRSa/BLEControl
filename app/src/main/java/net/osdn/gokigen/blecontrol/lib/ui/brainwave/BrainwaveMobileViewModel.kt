package net.osdn.gokigen.blecontrol.lib.ui.brainwave;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BrainwaveMobileViewModel extends ViewModel
{

    private MutableLiveData<String> mText;

    public BrainwaveMobileViewModel()
    {
        mText = new MutableLiveData<>();
        mText.setValue(" ");
    }

    void setText(@NonNull String data)
    {
        mText.setValue(data);
    }

    void addText(@NonNull String data)
    {
        mText.setValue(mText.getValue() + data);
    }

    public LiveData<String> getText() {
        return mText;
    }
}
