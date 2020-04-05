package net.osdn.gokigen.blecontrol.lib.data.brainwave;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osdn.gokigen.blecontrol.lib.ui.brainwave.IBrainwaveDataDrawer;

import java.util.Arrays;

public class BrainwaveDataHolder implements IBrainwaveDataReceiver
{
    private final String TAG = toString();

    private final IBrainwaveDataDrawer dataDrawer;
    private int[] valueBuffer;
    private BrainwaveSummaryData currentSummaryData;
    private int maxBufferSize;
    private int currentPosition;
    private boolean bufferIsFull = false;

    public BrainwaveDataHolder(@NonNull IBrainwaveDataDrawer dataDrawer, int maxBufferSize)
    {
        this.dataDrawer = dataDrawer;
        this.maxBufferSize = maxBufferSize;

        valueBuffer = new int[maxBufferSize];
        currentPosition = 0;

        currentSummaryData = new BrainwaveSummaryData();
    }

    @Override
    public void receivedRawData(int value)
    {
        //Log.v(TAG, " receivedRawData() : " + value);
        try
        {
            valueBuffer[currentPosition] = value;
            currentPosition++;
            if (currentPosition == maxBufferSize)
            {
                currentPosition = 0;
                bufferIsFull = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        dataDrawer.drawGraph();
    }

    @Override
    public void receivedSummaryData(byte[] data)
    {
        if (!currentSummaryData.update(data))
        {
            // parse failure...
            Log.v(TAG, " FAIL : PARSE EEG SUMMARY DATA (" + data.length + ")");
        }
    }

    public @NonNull BrainwaveSummaryData getSummaryData()
    {
        return (currentSummaryData);
    }

    public @Nullable int[] getValues(int size)
    {
        int [] replyData = null;
        try
        {
            int endPosition = currentPosition - 1;
            if (currentPosition > size)
            {
                return (Arrays.copyOfRange(valueBuffer, (endPosition - size), endPosition));
            }
            if (!bufferIsFull)
            {
                return (Arrays.copyOfRange(valueBuffer, 0, endPosition));
            }

            int remainSize = size - (currentPosition - 1);
            int [] size0 = Arrays.copyOfRange(valueBuffer, 0, (currentPosition - 1));
            int [] size1 = Arrays.copyOfRange(valueBuffer, ((maxBufferSize - 1) - remainSize), (maxBufferSize - 1));

            replyData = new int[size];

            System.arraycopy(size0, 0, replyData, 0, size0.length);
            System.arraycopy(size1, 0, replyData, size0.length, size1.length);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (replyData);
    }
}
