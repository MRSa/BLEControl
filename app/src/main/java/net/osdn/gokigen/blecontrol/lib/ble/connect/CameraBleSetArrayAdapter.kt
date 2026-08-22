package net.osdn.gokigen.blecontrol.lib.ble.connect

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import net.osdn.gokigen.blecontrol.lib.ble.R

class CameraBleSetArrayAdapter internal constructor(
    private val context: FragmentActivity,
    private val textViewResourceId: Int,
    private val listItems: MutableList<CameraBleSetArrayItem>,
    private val dialogDismiss: ICameraSetDialogDismiss?
) : ArrayAdapter<CameraBleSetArrayItem?>(
    context,
    textViewResourceId,
    listItems
) {
    private val TAG = toString()
    private val inflater: LayoutInflater

    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    /**
     * 
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?
        if (convertView != null) {
            view = convertView
        } else {
            view = inflater.inflate(textViewResourceId, null)
        }

        try {
            val item = listItems.get(position)

            val btNameEdit = view.findViewWithTag<EditText>("bt_name")
            btNameEdit.setText(item.getBtName())

            val passCodeEdit = view.findViewWithTag<EditText>("bt_passcode")
            passCodeEdit.setText(item.getBtPassCode())

            val infoView = view.findViewWithTag<TextView>("info")
            infoView.setText(item.getInformation())

            val button = view.findViewWithTag<Button>("button")
            button.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    val idHeader = item.getDataId()
                    val btName = btNameEdit.getText().toString()
                    val btCode = passCodeEdit.getText().toString()
                    val itemInfo = item.getInformation()

                    Log.v(
                        TAG,
                        "CLICKED : " + idHeader + " " + btName + " [" + btCode + "] (" + item.getBtName() + " " + itemInfo + ")"
                    )
                    if (dialogDismiss != null) {
                        dialogDismiss.setOlyCameraSet(idHeader, btName, btCode, itemInfo)
                    }
                    Log.v(TAG, "REGISTERD CAMERA : " + idHeader + " " + btName)

                    // Toastで保管したことを通知する
                    val restoredMessage = context.getString(R.string.saved_my_camera) + btName
                    Toast.makeText(context, restoredMessage, Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return (view)
    }
}
