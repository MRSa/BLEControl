package net.osdn.gokigen.blecontrol.lib.ui.fv100;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import net.osdn.gokigen.blecontrol.lib.ble.R;

public class FV100DeviceQuery implements View.OnClickListener
{
    private String TAG = toString();
    private final Context context;
    private final DeviceInfo deviceInfo;
    private final FV100ViewModel viewModel;

    FV100DeviceQuery(@NonNull Context context, @NonNull DeviceInfo deviceInfo, @NonNull FV100ViewModel viewModel)
    {
        this.context = context;
        this.deviceInfo = deviceInfo;
        this.viewModel = viewModel;
    }

    @Override
    public void onClick(View v)
    {
        try
        {
            String deviceName = deviceInfo.getQueryDeviceName();
            viewModel.setText(context.getString(R.string.start_query)+ " '" + deviceName + "'");
            if (deviceName.length() > 1)
            {
                query_to_device_impl(deviceName);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void query_to_device_impl(String deviceName)
    {
        // TODO ----- QUERY TO BLUETOOTH DEVICE

    }

    interface DeviceInfo
    {
        String getQueryDeviceName();
    }
}
