package net.osdn.gokigen.blecontrol.lib.ui.settings.bluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import net.osdn.gokigen.blecontrol.lib.ble.R;

public class SettingsBluetoothFragment extends Fragment
{

    private SettingsBluetoothViewModel settingsBluetoothViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        settingsBluetoothViewModel = ViewModelProviders.of(this).get(SettingsBluetoothViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings_bluetooth, container, false);
        final TextView textView = root.findViewById(R.id.text_settings_bluetooth);
        settingsBluetoothViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        final Button btnConnect = root.findViewById(R.id.btnConnect);
        ConnectViaBluetooth btConnection = new ConnectViaBluetooth(this);
        btnConnect.setOnClickListener(btConnection);
        btnConnect.setOnLongClickListener(btConnection);

        final Button btnWifi = root.findViewById(R.id.btnWifiSet);
        btnWifi.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                try
                {
                    // Wifi 設定画面を表示する
                    getActivity().startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        return (root);
    }
}
