package net.osdn.gokigen.blecontrol.lib.ble.connect

import android.app.Activity

/**
 * Olympus AIR の Bluetooth設定を記録する
 * 
 * 
 */
class OlyCameraPowerOnSelector

/**
 * コンストラクタ
 * 
 */(private val activity: Activity) {
    private val TAG = toString()


    fun showBleSettingDialog() {
        try {
            /*
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            // Bluetooth設定登録用ダイアログを表示する
            CameraBleEntryListDialog dialogFragment = CameraBleEntryListDialog.newInstance(activity.getString(R.string.pref_ble_settings), activity.getString(R.string.pref_summary_ble_settings));
            dialogFragment.setRetainInstance(false);
            dialogFragment.setShowsDialog(true);
            dialogFragment.show();
*/
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
