package net.osdn.gokigen.blecontrol.lib.data.brainwave;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.blecontrol.lib.ui.brainwave.IBrainwaveDataDrawer;

public class BrainwaveDataHolder implements IBrainwaveDataReceiver
{
    private final String TAG = toString();

    private final IBrainwaveDataDrawer dataDrawer;
    private int value;

    public BrainwaveDataHolder(@NonNull IBrainwaveDataDrawer dataDrawer)
    {
        this.dataDrawer = dataDrawer;
    }

    @Override
    public void receivedRawData(int value)
    {
        Log.v(TAG, " receivedRawData() : " + value);

        this.value = value;

        dataDrawer.drawGraph();
    }

    public int getValue()
    {
        return (value);
    }
}
