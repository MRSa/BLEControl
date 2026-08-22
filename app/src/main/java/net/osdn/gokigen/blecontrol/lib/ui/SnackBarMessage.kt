package net.osdn.gokigen.blecontrol.lib.ui;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.snackbar.Snackbar;

import net.osdn.gokigen.blecontrol.lib.ble.R;

public class SnackBarMessage
{
    private String TAG = toString();
    private final FragmentActivity context;
    private final boolean isToast;

    public SnackBarMessage(@NonNull FragmentActivity context, boolean isToast)
    {
        this.context = context;
        this.isToast = isToast;
    }

    public void showMessage(final String message)
    {
        try
        {
            Log.v(TAG, message);
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        if (!isToast)
                        {
                            // Snackbarでメッセージを通知する
                            Snackbar.make(context.findViewById(R.id.drawer_layout), message, Snackbar.LENGTH_LONG).show();
                        }
                        else
                        {
                            // Toastでメッセージを通知する
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void showMessage(int stringId)
    {
        try
        {
            showMessage(context.getString(stringId));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
