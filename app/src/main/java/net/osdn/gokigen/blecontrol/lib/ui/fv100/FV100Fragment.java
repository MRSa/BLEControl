package net.osdn.gokigen.blecontrol.lib.ui.fv100;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import net.osdn.gokigen.blecontrol.lib.ble.MyBleAdapter;
import net.osdn.gokigen.blecontrol.lib.ble.R;

import java.util.List;

public class FV100Fragment extends Fragment implements FV100DeviceQuery.DeviceInfo
{
    private final String TAG = toString();
    private FV100ViewModel fv100ViewModel;
    private List<String> bondedDeviceList;
    private int selectedDevicePosition = 0;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.v(TAG, " onCreateView : FV100");

        fv100ViewModel = ViewModelProviders.of(this).get(FV100ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_fv100, container, false);
        final TextView textView = root.findViewById(R.id.text_device_fv100);
        fv100ViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>()
        {
            @Override
            public void onChanged(@Nullable String s)
            {
                textView.setText(s);
            }
        });
        try
        {
            FragmentActivity context = getActivity();
            if (context != null)
            {
                // Bonded Device List
                prepareDeviceSelection(context, root);

                // Device Query Button
                final Button queryButton = root.findViewById(R.id.query_to_device);
                FV100DeviceQuery deviceQuery = new FV100DeviceQuery(context, this, fv100ViewModel);
                FV100PropertySetting propertySetting = new FV100PropertySetting(context, deviceQuery);
                if (queryButton != null)
                {
                    queryButton.setOnClickListener(deviceQuery);
                }

                // Reload Button
                final ImageButton reloadButton = root.findViewById(R.id.reload_button);
                if (reloadButton != null)
                {
                    reloadButton.setOnClickListener(deviceQuery);
                }

                // WiFi Connect Button
                final ImageButton wifiConnectButton = root.findViewById(R.id.wifi_connect_button);
                if (wifiConnectButton != null)
                {
                    wifiConnectButton.setOnClickListener(deviceQuery);
                }

                // Change Image Size Button
                final ImageButton imageSizeButton = root.findViewById(R.id.change_image_size_button);
                if (imageSizeButton != null)
                {
                    imageSizeButton.setOnClickListener(propertySetting);
                }

                // Change Video size Button
                final ImageButton videoSizeButton = root.findViewById(R.id.change_video_size_button);
                if (videoSizeButton != null)
                {
                    videoSizeButton.setOnClickListener(propertySetting);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (root);
    }

    /**
     *   通信先デバイスの設定 （選択できるようにする）
     *
     * @param context  context
     * @param root     view root
     */
    private void prepareDeviceSelection(@NonNull Context context, @NonNull View root)
    {
        try
        {
            final Spinner selection_device = root.findViewById(R.id.spinner_selection_device);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,android.R.layout.simple_spinner_item);
            bondedDeviceList = MyBleAdapter.getBondedDevices();
            adapter.addAll(bondedDeviceList);
            selection_device.setAdapter(adapter);
            selection_device.setSelection(selectedDevicePosition);
            selection_device.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    Log.v(TAG, "onItemSelected : " + position + " (" + id + ")");
                    try
                    {
                        selectedDevicePosition = position;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent)
                {
                    Log.v(TAG, "onNothingSelected");
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public String getQueryDeviceName()
    {
        String deviceName = "";
        try
        {
            deviceName = bondedDeviceList.get(selectedDevicePosition);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (deviceName);
    }
}
