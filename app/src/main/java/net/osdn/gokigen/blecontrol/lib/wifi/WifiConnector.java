package net.osdn.gokigen.blecontrol.lib.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import net.osdn.gokigen.blecontrol.lib.ble.R;
import net.osdn.gokigen.blecontrol.lib.ui.SnackBarMessage;

import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class WifiConnector
{
    private String TAG = toString();

    private final SnackBarMessage messageToShow;
    private final FragmentActivity context;
    private final BroadcastReceiver connectionReceiver;

    public WifiConnector(@NonNull FragmentActivity context, @NonNull SnackBarMessage messageToShow)
    {
        this.context = context;
        this.messageToShow = messageToShow;
        connectionReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                onReceiveBroadcastOfConnection(context, intent);
            }
        };
    }

    private void onReceiveBroadcastOfConnection(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (action == null)
        {
            // action不明だった。抜ける。
            // Log.v(TAG, "intent.getAction() : null");
            return;
        }
        if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
        {
            // 不要な Broadcastだった。抜ける。
            return;
        }

        try
        {
            Log.v(TAG, "onReceiveBroadcastOfConnection() : CONNECTIVITY_ACTION");
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null)
            {
                // WifiManagerが取得できなかった
                Log.v(TAG, " WifiManager() : null");
                return;
            }
            WifiInfo info = wifiManager.getConnectionInfo();
            if (wifiManager.isWifiEnabled() && info != null)
            {
                if (info.getNetworkId() != -1)
                {
                    Log.v(TAG, "Network ID is NOT -1, there is no currently connected network.");
                }
            }
            else
            {
                if (info == null)
                {
                    Log.v(TAG, "NETWORK INFO IS NULL.");
                }
                else
                {
                    Log.v(TAG, "isWifiEnabled : " + wifiManager.isWifiEnabled() + " NetworkId : " + info.getNetworkId());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void connectToWifi(@NonNull String wifiSsId, @NonNull String wifiKey, @NonNull WifiConnectNotify callback)
    {
        try
        {
            String message = "connect_wifi\n    SSID : " + wifiSsId + "  Key : " + wifiKey;
            Log.v(TAG, message);
            //messageToShow.showMessage(message);
            WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
            turnOnWiFi(wifi);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            {
                //  API LEVEL < 29
                connectToWifiOlderVersion(wifi, wifiSsId, wifiKey, callback);
            }
            else
            {
                // API LEVEL >= 29
                connectToWifiNewerVersion();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void connectToWifiOlderVersion(@Nullable WifiManager wifi, @NonNull String wifiSsId, @NonNull String wifiKey, @NonNull WifiConnectNotify callback)
    {
        try
        {
            if (wifi == null)
            {
                callback.onWifiConnected(false);
                return;
            }

            String ssId = "\"" + wifiSsId + "\"";
            String key = "\"" + wifiKey + "\"";
            int networkId = -1;
            WifiConfiguration targetConfiguration = null;
            List<WifiConfiguration> configs = wifi.getConfiguredNetworks();
            for (WifiConfiguration config : configs)
            {
                Log.v(TAG, "NETWORK : " + config.SSID + " (hidden : " + config.hiddenSSID + ") " + config.networkId);
                if (config.SSID.matches(ssId))
                {
                    // すでにネットワークが設定済
                    Log.v(TAG, " FOUND SSID : " + ssId);
                    boolean ret = wifi.removeNetwork(config.networkId);
                    if (ret)
                    {
                        Log.v(TAG, " NETWORK IS REMOVED. : " + config.SSID);
                        targetConfiguration = null;
                        break;
                    }
                    config.preSharedKey = key;
                    networkId = config.networkId;
                    targetConfiguration = config;
                    break;
                }
            }
            if (targetConfiguration == null)
            {
                // ネットワークが未設定だった場合...
                targetConfiguration = new WifiConfiguration();
                targetConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                targetConfiguration.SSID = ssId;
                targetConfiguration.preSharedKey = key;
                targetConfiguration.hiddenSSID = true;
                networkId = wifi.addNetwork(targetConfiguration);
            }

            // ネットワークIDが取得できた場合、、、
            if (networkId != -1)
            {
                // いったん WIFIを無効化してから...
                for (WifiConfiguration config : wifi.getConfiguredNetworks())
                {
                    wifi.enableNetwork(config.networkId, false);
                }

                // WIFIを接続するにする
                wifi.enableNetwork(networkId, true);
            }
            else
            {
                // 接続に失敗した
                messageToShow.showMessage(context.getString(R.string.connect_wifi_failure) + " " + ssId);
                callback.onWifiConnected(false);
                return;
            }
            messageToShow.showMessage(context.getString(R.string.try_to_connect_wifi) + " " + ssId);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void connectToWifiNewerVersion()
    {


    }

    private void turnOnWiFi(@Nullable WifiManager wifi)
    {
        if ((wifi != null)&&(!wifi.isWifiEnabled()))
        {
            try
            {
                // WiFi を ON にする (たぶん失敗する...)
                if (!wifi.setWifiEnabled(true))
                {
                    messageToShow.showMessage(R.string.turn_on_wifi_is_failed);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void startWatchWifiStatus(Context context)
    {
        Log.v(TAG, "startWatchWifiStatus()");
        try
        {
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.getApplicationContext().registerReceiver(connectionReceiver, filter);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void stopWatchWifiStatus(Context context)
    {
        Log.v(TAG, "stopWatchWifiStatus()");
        try
        {
            context.getApplicationContext().unregisterReceiver(connectionReceiver);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public interface WifiConnectNotify
    {
        void onWifiConnected(boolean isConnect);
    }

}
