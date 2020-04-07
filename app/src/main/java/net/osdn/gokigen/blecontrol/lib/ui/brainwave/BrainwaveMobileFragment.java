package net.osdn.gokigen.blecontrol.lib.ui.brainwave;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import net.osdn.gokigen.blecontrol.lib.ble.MyBleAdapter;
import net.osdn.gokigen.blecontrol.lib.ble.R;
import net.osdn.gokigen.blecontrol.lib.data.brainwave.BrainwaveDataHolder;

import java.util.List;

public class BrainwaveMobileFragment extends Fragment implements BrainwaveConnection.SelectDevice
{
    private final String TAG = toString();
    private List<String> bondedDeviceList = null;
    private BrainwaveDataHolder dataHolder = null;
    private int selectedDevicePosition = 0;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final BrainwaveMobileViewModel brainwaveViewModel = ViewModelProviders.of(this).get(BrainwaveMobileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_brainwave, container, false);
        final BrainwaveRawGraphView cameraLiveImageView = root.findViewById(R.id.cameraLiveImageView);
        dataHolder = new BrainwaveDataHolder(cameraLiveImageView, 16000);
        cameraLiveImageView.setDataHolder(dataHolder);
        final TextView textView = root.findViewById(R.id.text_brainwave);
        brainwaveViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
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

                // Connect Button
                final Switch loggingSwitch = root.findViewById(R.id.switch_logging);
                final BrainwaveConnection eegConnection = new BrainwaveConnection(context, this, brainwaveViewModel, dataHolder, loggingSwitch);
                final Button queryButton = root.findViewById(R.id.connect_to_eeg);
                if (queryButton != null)
                {
                    queryButton.setOnClickListener(eegConnection);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return root;
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
            final Spinner selection_device = root.findViewById(R.id.spinner_selection_eeg_device);
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
    public String getSelectedDeviceName()
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
