package net.osdn.gokigen.blecontrol.lib.ble.connect.eeg;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import net.osdn.gokigen.blecontrol.lib.ble.R;
import net.osdn.gokigen.blecontrol.lib.ble.connect.ITextDataUpdater;

public class MindWaveCommunication
{
    private final String TAG = toString();
    private final FragmentActivity context;
    private final ITextDataUpdater dataUpdater;

    public MindWaveCommunication(@NonNull FragmentActivity context, @NonNull ITextDataUpdater dataUpdater)
    {
        this.context = context;
        this.dataUpdater = dataUpdater;
    }

    public void connect(@NonNull String deviceName)
    {
        Log.v(TAG, " BrainWaveMobileCommunicator::connect() : " + deviceName);
        setText(context.getString(R.string.start_query) + " '" + deviceName + "' ");

    }



    private void setText(@NonNull final String message)
    {
        dataUpdater.setText(message);
    }

    private void addText(@NonNull final String message)
    {
        dataUpdater.addText(message);
    }

}
