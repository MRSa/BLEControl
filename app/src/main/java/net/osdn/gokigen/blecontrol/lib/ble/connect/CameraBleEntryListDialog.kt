package net.osdn.gokigen.blecontrol.lib.ble.connect

import android.app.Dialog
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import net.osdn.gokigen.blecontrol.lib.ble.R
import java.text.DateFormat
import java.util.Date

class CameraBleEntryListDialog : DialogFragment(), ICameraSetDialogDismiss {
    private val TAG = this.toString()
    private var viewCreated = false
    private var myView: View? = null
    private var message: String? = ""
    private var title: String? = ""
    private val listFragment: CameraBleEntryListFragment =
        CameraBleEntryListFragment.newInstance(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView()")

        val arguments = getArguments()
        if (arguments != null) {
            title = arguments.getString("title")
            message = arguments.getString("message")
            Log.v(TAG, "title: " + title + " message: " + message)
        }

        if ((viewCreated) && (myView != null)) {
            // Viewを再利用。。。
            Log.v(TAG, "onCreateView() : called again, so do nothing... : " + myView)
            return (myView)
        }
        val view = inflater.inflate(R.layout.dialog_my_camera_ble_entries, container, false)

        myView = view
        viewCreated = true

        // listFragmentを子フラグメントとする（Nested Fragment を使う）
        val fragmentManager = getChildFragmentManager()
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.layout_content, listFragment, "list_fragment")
        transaction.commit()

        return (view)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.v(TAG, "onCreateDialog() : " + title + " (" + message + ")")
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle(title)
        return (dialog)
    }

    /**
     * 
     * 
     */
    override fun setOlyCameraSet(id: String?, name: String?, code: String?, info: String?) {
        val namePrefKey = id + ICameraBleProperty.NAME_KEY
        val codePrefKey = id + ICameraBleProperty.CODE_KEY
        val infoPrefKey = id + ICameraBleProperty.DATE_KEY

        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        val dateInfo = dateFormat.format(Date())

        try {
            val preferences = PreferenceManager.getDefaultSharedPreferences(getActivity())
            val editor = preferences.edit()

            editor.putString(namePrefKey, name)
            editor.putString(codePrefKey, code)
            editor.putString(infoPrefKey, dateInfo)

            editor.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.v(
            TAG,
            "setOlyCameraSet() REGISTERED : [" + id + "] " + name + " " + code + " " + dateInfo
        )

        dismiss()
    }

    companion object {
        fun newInstance(title: String?, message: String?): CameraBleEntryListDialog {
            val instance = CameraBleEntryListDialog()

            // ダイアログに渡すパラメータはBundleにまとめておく
            val arguments = Bundle()
            arguments.putString("title", title)
            arguments.putString("message", message)
            instance.setArguments(arguments)

            Log.v("dialog", "title: " + title + " message: " + message)
            return (instance)
        }
    }
}
