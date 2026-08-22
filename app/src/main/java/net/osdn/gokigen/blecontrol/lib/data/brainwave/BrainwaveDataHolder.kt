package net.osdn.gokigen.blecontrol.lib.data.brainwave

import android.util.Log
import net.osdn.gokigen.blecontrol.lib.ui.brainwave.IBrainwaveDataDrawer
import java.util.Arrays

class BrainwaveDataHolder(
    private val dataDrawer: IBrainwaveDataDrawer,
    private val maxBufferSize: Int
) : IBrainwaveDataReceiver {
    private val TAG = toString()

    private val valueBuffer: IntArray
    private val currentSummaryData: BrainwaveSummaryData
    private var currentPosition: Int
    private var bufferIsFull = false

    init {
        valueBuffer = IntArray(maxBufferSize)
        currentPosition = 0

        currentSummaryData = BrainwaveSummaryData()
    }

    override fun receivedRawData(value: Int) {
        //Log.v(TAG, " receivedRawData() : " + value);
        try {
            valueBuffer[currentPosition] = value
            currentPosition++
            if (currentPosition == maxBufferSize) {
                currentPosition = 0
                bufferIsFull = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        dataDrawer.drawGraph()
    }

    override fun receivedSummaryData(data: ByteArray?) {
        if (data == null)
        {
            Log.v(TAG, " FAIL : PARSE DATA IS NULL.")
            return
        }
        if (!currentSummaryData.update(data)) {
            // parse failure...
            Log.v(TAG, " FAIL : PARSE EEG SUMMARY DATA (" + data.size + ")")
        }
    }

    val summaryData: BrainwaveSummaryData
        get() = (currentSummaryData)

    fun getValues(size: Int): IntArray? {
        var replyData: IntArray? = null
        try {
            var endPosition = currentPosition - 1
            if (currentPosition > size) {
                return (Arrays.copyOfRange(valueBuffer, (endPosition - size), endPosition))
            }
            if (!bufferIsFull) {
                return (Arrays.copyOfRange(valueBuffer, 0, endPosition))
            }
            if (currentPosition == 0) {
                endPosition = (maxBufferSize - 1)
                return (Arrays.copyOfRange(valueBuffer, (endPosition - size), endPosition))
            }

            val remainSize = size - (currentPosition - 1)
            val size0 = Arrays.copyOfRange(valueBuffer, 0, (currentPosition - 1))
            val size1 = Arrays.copyOfRange(
                valueBuffer,
                ((maxBufferSize - 1) - remainSize),
                (maxBufferSize - 1)
            )

            replyData = IntArray(size)

            System.arraycopy(size1, 0, replyData, 0, size1.size)
            System.arraycopy(size0, 0, replyData, size1.size, size0.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (replyData)
    }
}
