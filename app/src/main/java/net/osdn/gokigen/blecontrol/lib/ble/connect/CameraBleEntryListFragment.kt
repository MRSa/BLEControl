package net.osdn.gokigen.blecontrol.lib.ble.connect

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.fragment.app.ListFragment
import net.osdn.gokigen.blecontrol.lib.ble.R
import java.util.Locale

class CameraBleEntryListFragment : ListFragment() {
    private val TAG = toString()
    private var dialogDismiss: ICameraSetDialogDismiss? = null

    /**/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView()")
        return (inflater.inflate(R.layout.list_camera_properties, container, false))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.v(TAG, "onActivityCreated()")
        super.onActivityCreated(savedInstanceState)

        val listItems: MutableList<CameraBleSetArrayItem> = ArrayList()

        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        for (index in 1..ICameraBleProperty.MAX_STORE_PROPERTIES) {
            val idHeader = String.format(Locale.ENGLISH, "%03d", index)
            val prefDate: String =
                preferences.getString(idHeader + ICameraBleProperty.DATE_KEY, "")!!
            if (prefDate.isEmpty()) {
                listItems.add(CameraBleSetArrayItem(idHeader, "", "", ""))
                break // 最後の１個は空白で出す
                //continue;  // 全部出す
            }
            val btName: String = preferences.getString(idHeader + ICameraBleProperty.NAME_KEY, "")!!
            val btCode: String = preferences.getString(idHeader + ICameraBleProperty.CODE_KEY, "")!!
            listItems.add(CameraBleSetArrayItem(idHeader, btName, btCode, prefDate))
        }
        val adapter = CameraBleSetArrayAdapter(
            requireActivity(),
            R.layout.column_save_bt,
            listItems,
            dialogDismiss
        )
        setListAdapter(adapter)
    }

    override fun onDestroyView() {
        Log.v(TAG, "onDestroyView()")
        super.onDestroyView()
    }

    companion object {
        fun newInstance(dismiss: ICameraSetDialogDismiss?): CameraBleEntryListFragment {
            val instance = CameraBleEntryListFragment()
            instance.dialogDismiss = dismiss

            return (instance)
        }
    }
}
