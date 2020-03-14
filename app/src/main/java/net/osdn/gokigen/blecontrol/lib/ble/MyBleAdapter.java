package net.osdn.gokigen.blecontrol.lib.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MyBleAdapter
{
    static public List<String> getBondedDevices()
    {
        List<String> s = new ArrayList<>();
        try
        {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();

            for (BluetoothDevice bt : bondedDevices)
            {
                s.add(bt.getName());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (s);
    }

}
