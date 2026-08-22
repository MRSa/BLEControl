package net.osdn.gokigen.blecontrol.lib.data.brainwave

interface IBrainwaveDataReceiver {
    fun receivedRawData(value: Int)
    fun receivedSummaryData(data: ByteArray?)
}
