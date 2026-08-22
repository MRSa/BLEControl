package net.osdn.gokigen.blecontrol.lib.data.brainwave

import android.os.Environment
import androidx.fragment.app.FragmentActivity
import net.osdn.gokigen.blecontrol.lib.SimpleLogDumper.Companion.dumpBytes
import net.osdn.gokigen.blecontrol.lib.ble.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BrainwaveFileLogger
    (context: FragmentActivity) {
    private var outputStream: FileOutputStream? = null

    init {
        try {
            val fileNamePrefix = context.getString(R.string.app_name2) + "_EEG"
            val calendar = Calendar.getInstance()
            val extendName =
                SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(calendar.getTime())
            val directoryPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .getPath() + "/"
            val outputFileName = fileNamePrefix + "_" + extendName + ".bin"
            val filepath = File(
                directoryPath.lowercase(Locale.getDefault()),
                outputFileName.lowercase(Locale.getDefault())
            ).getPath()
            outputStream = FileOutputStream(filepath)
        } catch (e: Exception) {
            e.printStackTrace()
            outputStream = null
        }
    }

    fun outputSummaryData(data: ByteArray) {
        try {
            dumpBytes("RECV [" + data.size + "] ", data)
            if ((outputStream != null) && (data.size >= 36)) {
                outputStream!!.write(data, 0, 36)
                outputStream!!.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
