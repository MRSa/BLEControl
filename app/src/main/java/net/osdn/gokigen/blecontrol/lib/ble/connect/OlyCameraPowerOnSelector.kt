package net.osdn.gokigen.blecontrol.lib.ble.connect;

import android.app.Activity;

import androidx.annotation.NonNull;

/**
 *   Olympus AIR の Bluetooth設定を記録する
 *
 *
 */
public class OlyCameraPowerOnSelector
{
    private final String TAG = toString();
    private final Activity activity;


    /**
     *   コンストラクタ
     *
     */
    public OlyCameraPowerOnSelector(@NonNull Activity context)
    {
        this.activity = context;
    }

    public void showBleSettingDialog()
    {
        try
        {
/*
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            // Bluetooth設定登録用ダイアログを表示する
            CameraBleEntryListDialog dialogFragment = CameraBleEntryListDialog.newInstance(activity.getString(R.string.pref_ble_settings), activity.getString(R.string.pref_summary_ble_settings));
            dialogFragment.setRetainInstance(false);
            dialogFragment.setShowsDialog(true);
            dialogFragment.show();
*/
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
     }
}
