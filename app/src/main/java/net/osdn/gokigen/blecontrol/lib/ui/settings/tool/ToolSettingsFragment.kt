package net.osdn.gokigen.blecontrol.lib.ui.settings.tool;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import net.osdn.gokigen.blecontrol.lib.ble.R;

public class ToolSettingsFragment extends Fragment
{

    private ToolSettingsViewModel toolSettingsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        toolSettingsViewModel =
                ViewModelProviders.of(this).get(ToolSettingsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_setting_tools, container, false);
        final TextView textView = root.findViewById(R.id.text_tools);
        toolSettingsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}
