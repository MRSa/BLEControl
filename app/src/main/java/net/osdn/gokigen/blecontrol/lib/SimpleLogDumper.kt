package net.osdn.gokigen.blecontrol.lib

import android.app.Activity
import android.os.Environment
import android.util.Log
import net.osdn.gokigen.blecontrol.lib.ble.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SimpleLogDumper
{
    companion object {
        private val TAG: String = SimpleLogDumper::class.java.getSimpleName()

        /**
         * デバッグ用：ログにバイト列を出力する
         */
        fun dumpBytes(header: String?, data: ByteArray?) {
            if (data == null) {
                Log.v(TAG, "DATA IS NULL")
                return
            }
            if (data.size > 8192) {
                Log.v(TAG, " --- DUMP DATA IS TOO LONG... " + data.size + " bytes.")
                return
            }

            var index = 0
            var message: StringBuilder?
            message = StringBuilder()
            for (item in data) {
                index++
                message!!.append(String.format("%02x ", item))
                if (index >= 16) {
                    Log.v(TAG, "$header $message")
                    index = 0
                    message = StringBuilder()
                }
            }
            if (index != 0) {
                Log.v(TAG, "$header $message")
            }
            System.gc()
        }

        fun binaryOutputToFile(activity: Activity, fileNamePrefix: String?, rx_body: ByteArray) {
            try {
                val calendar: Calendar = Calendar.getInstance()
                val extendName: String? =
                    SimpleDateFormat(
                        "yyyyMMdd-HHmmss",
                        Locale.getDefault()
                    ).format(calendar.getTime())
                val directoryPath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        .path + "/" + activity.getString(R.string.app_name2) + "/"
                val outputFileName = fileNamePrefix + "_" + extendName + ".bin"
                val filepath = File(
                    directoryPath.lowercase(Locale.getDefault()),
                    outputFileName.lowercase(Locale.getDefault())
                ).path
                val outputStream = FileOutputStream(filepath)
                outputStream.write(rx_body, 0, rx_body.size)
                outputStream.flush()
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
