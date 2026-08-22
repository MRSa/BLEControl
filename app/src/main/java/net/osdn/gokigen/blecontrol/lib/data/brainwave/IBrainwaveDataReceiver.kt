package net.osdn.gokigen.blecontrol.lib.data.brainwave;

public interface IBrainwaveDataReceiver
{
    void receivedRawData(int value);
    void receivedSummaryData(byte[] data);
}
