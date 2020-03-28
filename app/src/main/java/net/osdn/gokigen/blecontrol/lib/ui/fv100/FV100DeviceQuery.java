package net.osdn.gokigen.blecontrol.lib.ui.fv100;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import net.osdn.gokigen.blecontrol.lib.ble.R;
import net.osdn.gokigen.blecontrol.lib.ble.connection.ITextDataUpdater;
import net.osdn.gokigen.blecontrol.lib.ble.connection.fv100.FV100BleDeviceConnector;

public class FV100DeviceQuery implements View.OnClickListener, ITextDataUpdater, FV100PropertySetting.PropertySetter
{
    private String TAG = toString();
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

    private void deviceQuery()
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

    private void dataReload()
    {
        try
        {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    deviceConnector.reload_device_information();
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void connectToCamera()
    {
        try
        {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    deviceConnector.connect_to_camera_via_wifi();
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(@NonNull View v)
    {
        int id = v.getId();
        switch (id)
        {
            case R.id.query_to_device:
                deviceQuery();
                break;

            case R.id.reload_button:
                dataReload();
                break;

            case R.id.wifi_connect_button:
                connectToCamera();
                break;

            default:
                Log.v(TAG, " onClick : " + id);
                break;
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

    @Override
    public void enableOperation(final boolean isEnable)
    {
        try
        {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final ImageButton wifiConnectButton = context.findViewById(R.id.wifi_connect_button);
                    if (wifiConnectButton != null)
                    {
                        wifiConnectButton.setEnabled(isEnable);
                        wifiConnectButton.setVisibility((isEnable? View.INVISIBLE : View.INVISIBLE));
                    }

                    final ImageButton imageSizeButton = context.findViewById(R.id.change_image_size_button);
                    if (imageSizeButton != null)
                    {
                        imageSizeButton.setEnabled(isEnable);
                        imageSizeButton.setVisibility((isEnable? View.VISIBLE : View.INVISIBLE));
                    }

                    final ImageButton videoSizeButton = context.findViewById(R.id.change_video_size_button);
                    if (videoSizeButton != null)
                    {
                        videoSizeButton.setEnabled(isEnable);
                        videoSizeButton.setVisibility((isEnable? View.VISIBLE : View.INVISIBLE));
                    }
                    //Log.v(TAG, " >> ITextDataUpdater::enableOperation() : " + isEnable);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void setProperty(@NonNull final String propertyName, @NonNull final String propertyValue)
    {
        try
        {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    deviceConnector.setProperty(propertyName, propertyValue);
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    interface DeviceInfo
    {
        String getQueryDeviceName();
    }
}
