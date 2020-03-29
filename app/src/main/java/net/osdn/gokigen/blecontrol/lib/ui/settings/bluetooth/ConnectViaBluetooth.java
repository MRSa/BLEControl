package net.osdn.gokigen.blecontrol.lib.ui.settings.bluetooth;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import net.osdn.gokigen.blecontrol.lib.ble.R;
import net.osdn.gokigen.blecontrol.lib.ble.connect.ICameraBleProperty;
import net.osdn.gokigen.blecontrol.lib.ble.connect.ICameraPowerOn;
import net.osdn.gokigen.blecontrol.lib.ble.connect.PowerOnCamera;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class ConnectViaBluetooth implements View.OnLongClickListener, View.OnClickListener, ICameraPowerOn.PowerOnCameraCallback
{
    private final String TAG = toString();
    private final Fragment fragment;

    ConnectViaBluetooth(@NonNull Fragment fragment)
    {
        this.fragment = fragment;

        setBleCameraSet(1, "B500_21028637", "164309", "DEFAULT");
    }

    @Override
    public boolean onLongClick(View view)
    {
        Log.v(TAG, " onLongClick()");

        return false;
    }

    @Override
    public void onClick(View view)
    {
        Log.v(TAG, " onClick()");

        Activity activity = fragment.getActivity();
        if (activity != null)
        {
            EditText deviceName = activity.findViewById(R.id.deviceName);
            EditText devicePass = activity.findViewById(R.id.devicePass);

            final PowerOnCamera connection = new PowerOnCamera(activity);
            final String device = (deviceName == null) ? "" :  deviceName.getText().toString();
            final String pass = (devicePass == null) ? "" :  devicePass.getText().toString();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        startPowerOnCamera(connection, device, pass);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            try
            {
                thread.start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void startPowerOnCamera(ICameraPowerOn connection, String deviceName, String passCode)
    {
        try
        {
            Log.v(TAG, " startPowerOnCamera()");
            Log.v(TAG, " device Name : " + deviceName + "  pass : " + passCode);

            setBleCameraSet(2, deviceName, passCode, "INFO");

            connection.wakeup(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void wakeupExecuted(boolean isExecute)
    {
        Log.v(TAG, " wakeupExecuted() : " + isExecute);
    }

    /**
     *   index : 1 ～ ICameraBleProperty.MAX_STORE_PROPERTIES
     *   name  : device name
     *   code  : passcode
     *   info  : information
     */
    private void setBleCameraSet(int index, String name, String code, String info)
    {
        String id = String.format(Locale.ENGLISH, "%03d", index);

        String namePrefKey = id + ICameraBleProperty.NAME_KEY;
        String codePrefKey = id + ICameraBleProperty.CODE_KEY;
        String infoPrefKey = id + ICameraBleProperty.DATE_KEY;

        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        String dateInfo = dateFormat.format(new Date());

        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(fragment.getActivity());
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString(namePrefKey, name);
            editor.putString(codePrefKey, code);
            editor.putString(infoPrefKey, dateInfo);

            editor.apply();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.v(TAG, "setBleCameraSet() REGISTERED : [" + id + "] " + name + " " + code + " " + dateInfo + " (" + info + ")");
    }
}
