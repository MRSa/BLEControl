package net.osdn.gokigen.blecontrol.lib.ui.fv100;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import net.osdn.gokigen.blecontrol.lib.ble.R;
import net.osdn.gokigen.blecontrol.lib.ble.connection.ITextDataUpdater;
import net.osdn.gokigen.blecontrol.lib.ble.connection.fv100.FV100BleDeviceConnector;

public class FV100DeviceQuery implements View.OnClickListener, ITextDataUpdater
{
    //private String TAG = toString();
    private final FragmentActivity context;
    private final DeviceInfo deviceInfo;
    private final FV100ViewModel viewModel;
    private final FV100BleDeviceConnector deviceConnector;

    FV100DeviceQuery(@NonNull FragmentActivity context, @NonNull DeviceInfo deviceInfo, @NonNull FV100ViewModel viewModel)
    {
        this.context = context;
        this.deviceInfo = deviceInfo;
        this.viewModel = viewModel;
        this.deviceConnector = new FV100BleDeviceConnector(context, this);
    }

    @Override
    public void onClick(View v)
    {
        try
        {
            final String deviceName = deviceInfo.getQueryDeviceName();
            viewModel.setText(context.getString(R.string.start_query)+ " '" + deviceName + "'");
            if (deviceName.length() > 1)
            {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        deviceConnector.query_to_device(deviceName);
                    }
                });
                thread.start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void setText(String data)
    {
        viewModel.setText(data);
    }

    @Override
    public void addText(String data)
    {
        viewModel.addText(data);
    }

    interface DeviceInfo
    {
        String getQueryDeviceName();
    }
}
