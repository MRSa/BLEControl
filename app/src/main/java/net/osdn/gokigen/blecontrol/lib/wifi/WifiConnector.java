package net.osdn.gokigen.blecontrol.lib.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import net.osdn.gokigen.blecontrol.lib.ble.R;
import net.osdn.gokigen.blecontrol.lib.ble.connect.ITextDataUpdater;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class WifiConnector
{
    private String TAG = toString();

    private final ITextDataUpdater dataUpdater;
    private final FragmentActivity context;
    private final BroadcastReceiver connectionReceiver;

    public WifiConnector(@NonNull FragmentActivity context, @NonNull ITextDataUpdater dataUpdater)
    {
        this.context = context;
        this.dataUpdater = dataUpdater;
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
/*
        if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
        {
            // 不要な Broadcastだった。抜ける。
            return;
        }
*/
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
                connectToWifiNewerVersion(wifiSsId, wifiKey, callback);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void connectToWifiOlderVersion(@Nullable WifiManager wifi, @NonNull String wifiSsId, @NonNull String wifiKey, @NonNull WifiConnectNotify callback)
    {
        boolean isConnect = false;
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
                // WIFIを接続する
                wifi.enableNetwork(networkId, true);
            }
            else
            {
                // 接続に失敗した
                dataUpdater.showSnackBar(context.getString(R.string.connect_wifi_failure) + " " + ssId);
                callback.onWifiConnected(false);
                return;
            }
            dataUpdater.showSnackBar(context.getString(R.string.try_to_connect_wifi) + " " + ssId);
            isConnect = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        callback.onWifiConnected(isConnect);
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void connectToWifiNewerVersion(@NonNull String wifiSsId, @NonNull String wifiKey, @NonNull WifiConnectNotify callback)
    {
        Log.v(TAG, "connectToWifiNewerVersion() : '" + wifiSsId + "' [" + wifiKey + "]");
        try
        {
            turnOffWifiNewerVersion(wifiSsId, wifiKey);

            WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
            builder.setSsid(wifiSsId);
            builder.setIsHiddenSsid(true);
            builder.setWpa2Passphrase(wifiKey);
            WifiNetworkSpecifier specifier = builder.build();

            NetworkRequest.Builder requestbuilder = new NetworkRequest.Builder();
            //requestbuilder.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            requestbuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
            requestbuilder.setNetworkSpecifier(specifier);

            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null)
            {
                final ConnectivityManager.NetworkCallback networkCallback = new WiFiCallback(callback);
                connectivityManager.requestNetwork(requestbuilder.build(), networkCallback);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void turnOffWifiNewerVersion(@NonNull String wifiSsId, @NonNull String wifiKey)
    {
        try
        {

            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null)
            {
                Network currentNetwork = connectivityManager.getActiveNetwork();
                if (currentNetwork == null)
                {
                    // network is not active.
                    return;
                }
                //final ConnectivityManager.NetworkCallback networkCallback = new WiFiCallback(callback);
                //connectivityManager.requestNetwork(requestbuilder.build(), networkCallback);
            }

            WifiNetworkSuggestion.Builder builder = new WifiNetworkSuggestion.Builder();
            builder.setSsid(wifiSsId);
            builder.setWpa2Passphrase(wifiKey);
            WifiNetworkSuggestion suggestion = builder.build();
            List<WifiNetworkSuggestion> list = new ArrayList<>();
            list.add(suggestion);
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null)
            {
                wifiManager.removeNetworkSuggestions(list);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
                    dataUpdater.showSnackBar(R.string.turn_on_wifi_is_failed);
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
            final IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                filter.addAction(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);
            }
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private class WiFiCallback extends ConnectivityManager.NetworkCallback
    {

        private WifiConnectNotify callback;
        WiFiCallback(@NonNull WifiConnectNotify callback)
        {
            this.callback = callback;
        }

        @Override
        public void onAvailable(Network network)
        {
            Log.v(TAG, "onAvailable " + network.toString());
            callback.onWifiConnected(true);

        }

        @Override
        public void onLost(Network network)
        {
            Log.v(TAG, "onLost " + network.toString());
            callback.onWifiConnected(false);
        }
    }


    public interface WifiConnectNotify
    {
        void onWifiConnected(boolean isConnect);
    }

}
