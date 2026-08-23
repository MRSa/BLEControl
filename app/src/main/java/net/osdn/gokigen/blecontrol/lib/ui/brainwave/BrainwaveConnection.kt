package net.osdn.gokigen.blecontrol.lib.ui.brainwave

import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Switch
import androidx.fragment.app.FragmentActivity
import net.osdn.gokigen.blecontrol.lib.ble.R
import net.osdn.gokigen.blecontrol.lib.ble.connect.ITextDataUpdater
import net.osdn.gokigen.blecontrol.lib.ble.connect.eeg.MindWaveCommunication
import net.osdn.gokigen.blecontrol.lib.data.brainwave.IBrainwaveDataReceiver
import net.osdn.gokigen.blecontrol.lib.ui.SnackBarMessage

class BrainwaveConnection internal constructor(
    private val context: FragmentActivity,
    private val deviceSelection: SelectDevice,
    private val viewModel: BrainwaveMobileViewModel,
    dataReceiver: IBrainwaveDataReceiver,
    loggingSwitch: Switch?
) : View.OnClickListener, ITextDataUpdater {
    private val TAG = toString()
    private val communicator: MindWaveCommunication
    private val messageToShow: SnackBarMessage
    private val loggingSwitch: Switch?

    init {
        this.communicator = MindWaveCommunication(context, this, dataReceiver)
        this.messageToShow = SnackBarMessage(context, false)
        this.loggingSwitch = loggingSwitch
    }

    override fun onClick(v: View) {
        val id = v.getId()
        when (id) {
            R.id.connect_to_eeg -> connectToEEG(deviceSelection.getSelectedDeviceName())
            else -> Log.v(TAG, " onClick : " + id)
        }
    }

    private fun connectToEEG(selectedDevice: String?) {
        var logging = false
        if (selectedDevice == null) {
            Log.v(TAG, " DEVICE is NULL.")
            return
        }
        if (loggingSwitch != null) {
            logging = loggingSwitch.isChecked()
        }
        try {
            val loggingFlag = logging
            Log.v(TAG, " CONNECT TO EEG. : " + selectedDevice)
            val thread = Thread(object : Runnable {
                override fun run() {
                    communicator.connect(selectedDevice, loggingFlag)
                }
            })
            thread.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setText(data: String?) {
        context.runOnUiThread(object : Runnable {
            override fun run() {
                viewModel.setText(data ?:"")
            }
        })
    }

    override fun addText(data: String?) {
        context.runOnUiThread(object : Runnable {
            override fun run() {
                viewModel.addText(data?:"")
            }
        })
    }

    override fun showSnackBar(message: String?) {
        messageToShow.showMessage(message?:"")
    }

    override fun showSnackBar(rscId: Int) {
        messageToShow.showMessage(rscId)
    }

    override fun enableOperation(isEnable: Boolean) {
        try {
            context.runOnUiThread(object : Runnable {
                override fun run() {
                    val dummyButton = context.findViewById<ImageButton?>(R.id.dummy_button1)
                    if (dummyButton != null) {
                        dummyButton.setEnabled(isEnable)
                        dummyButton.setVisibility((if (isEnable) View.INVISIBLE else View.INVISIBLE))
                    }
                    //Log.v(TAG, " >> ITextDataUpdater::enableOperation() : " + isEnable);
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal interface SelectDevice {
        fun getSelectedDeviceName(): String
    }
}
