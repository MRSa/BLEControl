package net.osdn.gokigen.blecontrol.lib.ui

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import net.osdn.gokigen.blecontrol.lib.ble.R

class SnackBarMessage
    (private val context: FragmentActivity, private val isToast: Boolean) {
    private val TAG = toString()

    fun showMessage(message: String) {
        try {
            Log.v(TAG, message)
            context.runOnUiThread(object : Runnable {
                override fun run() {
                    try {
                        if (!isToast) {
                            // Snackbarでメッセージを通知する
                            Snackbar.make(
                                context.findViewById<View?>(R.id.drawer_layout),
                                message,
                                Snackbar.LENGTH_LONG
                            ).show()
                        } else {
                            // Toastでメッセージを通知する
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showMessage(stringId: Int) {
        try {
            showMessage(context.getString(stringId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
