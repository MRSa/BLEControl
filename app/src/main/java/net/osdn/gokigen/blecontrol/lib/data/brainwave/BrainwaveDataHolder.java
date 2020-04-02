package net.osdn.gokigen.blecontrol.lib.data.brainwave;

import androidx.annotation.NonNull;

import net.osdn.gokigen.blecontrol.lib.ui.brainwave.IBrainwaveDataDrawer;

public class BrainwaveDataHolder implements IBrainwaveDataReceiver
{
    private final IBrainwaveDataDrawer dataDrawer;

    public BrainwaveDataHolder(@NonNull IBrainwaveDataDrawer dataDrawer)
    {
        this.dataDrawer = dataDrawer;
    }

    @Override
    public void receivedRawData(byte value)
    {


        dataDrawer.drawGraph();
    }
}
