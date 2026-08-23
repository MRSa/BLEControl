package net.osdn.gokigen.blecontrol.lib.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import net.osdn.gokigen.blecontrol.lib.ble.R
import net.osdn.gokigen.blecontrol.lib.ble.connect.ITextDataUpdater
import android.net.wifi.WifiInfo
import java.lang.Exception

class WifiConnector(
    private val context: FragmentActivity,
    private val dataUpdater: ITextDataUpdater
) {

    private val tag: String = this.javaClass.simpleName

    fun interface WifiConnectNotify {
        fun onWifiConnected(isConnect: Boolean)
    }

    private val connectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onReceiveBroadcastOfConnection(context, intent)
        }
    }

    private fun onReceiveBroadcastOfConnection(context: Context, intent: Intent) {
        val action = intent.action ?: return

        try {
            Log.v(tag, "onReceiveBroadcastOfConnection() : $action")
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            if (wifiManager == null) {
                Log.v(tag, " WifiManager() : null")
                return
            }

            @Suppress("DEPRECATION")
            val info: WifiInfo? = wifiManager.connectionInfo
            if (wifiManager.isWifiEnabled && info != null) {
                if (info.networkId != -1) {
                    Log.v(tag, "Network ID is NOT -1, there is connected network.")
                }
            } else {
                if (info == null) {
                    Log.v(tag, "NETWORK INFO IS NULL.")
                } else {
                    Log.v(tag, "isWifiEnabled : ${wifiManager.isWifiEnabled} NetworkId : ${info.networkId}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun connectToWifi(wifiSsId: String, wifiKey: String, callback: WifiConnectNotify) {
        try {
            Log.v(tag, "connect_wifi\n    SSID : $wifiSsId  Key : $wifiKey")
            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

            turnOnWiFi(wifi)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // API LEVEL < 29
                connectToWifiOlderVersion(wifi, wifiSsId, wifiKey, callback)
            } else {
                // API LEVEL >= 29
                connectToWifiNewerVersion(wifiSsId, wifiKey, callback)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    private fun connectToWifiOlderVersion(
        wifi: WifiManager?,
        wifiSsId: String,
        wifiKey: String,
        callback: WifiConnectNotify
    ) {
        var isConnect = false
        try {
            if (wifi == null) {
                callback.onWifiConnected(false)
                return
            }

            val ssId = "\"$wifiSsId\""
            val key = "\"$wifiKey\""
            var networkId = -1
            var targetConfiguration: WifiConfiguration? = null
            val configs: List<WifiConfiguration>? = wifi.configuredNetworks
            if (configs != null) {
                for (config in configs) {
                    Log.v(tag, "NETWORK : ${config.SSID} (hidden : ${config.hiddenSSID}) ${config.networkId}")
                    if (config.SSID != null && config.SSID.matches(Regex(ssId))) {
                        Log.v(tag, " FOUND SSID : $ssId")
                        val ret = wifi.removeNetwork(config.networkId)
                        if (ret) {
                            Log.v(tag, " NETWORK IS REMOVED. : ${config.SSID}")
                            targetConfiguration = null
                            break
                        }
                        config.preSharedKey = key
                        networkId = config.networkId
                        targetConfiguration = config
                        break
                    }
                }
            }

            if (targetConfiguration == null) {
                targetConfiguration = WifiConfiguration().apply {
                    allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                    SSID = ssId
                    preSharedKey = key
                    hiddenSSID = true
                }
                networkId = wifi.addNetwork(targetConfiguration)
            }

            if (networkId != -1) {
                wifi.configuredNetworks?.forEach { config ->
                    wifi.enableNetwork(config.networkId, false)
                }
                wifi.enableNetwork(networkId, true)
                dataUpdater.showSnackBar("${context.getString(R.string.try_to_connect_wifi)} $ssId")
                isConnect = true
            } else {
                dataUpdater.showSnackBar("${context.getString(R.string.connect_wifi_failure)} $ssId")
                callback.onWifiConnected(false)
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        callback.onWifiConnected(isConnect)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToWifiNewerVersion(
        wifiSsId: String,
        wifiKey: String,
        callback: WifiConnectNotify
    ) {
        Log.v(tag, "connectToWifiNewerVersion() : '$wifiSsId' [$wifiKey]")
        try {
            removeWifiSuggestionNewerVersion(wifiSsId, wifiKey)

            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid(wifiSsId)
                .setIsHiddenSsid(true)
                .setWpa2Passphrase(wifiKey)
                .build()

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build()

            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (connectivityManager != null) {
                val networkCallback = WiFiCallback(callback)
                connectivityManager.requestNetwork(request, networkCallback)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun removeWifiSuggestionNewerVersion(wifiSsId: String, wifiKey: String) {
        try {
            val suggestion = WifiNetworkSuggestion.Builder()
                .setSsid(wifiSsId)
                .setWpa2Passphrase(wifiKey)
                .build()

            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifiManager?.removeNetworkSuggestions(listOf(suggestion))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun turnOnWiFi(wifi: WifiManager?) {
        if (wifi != null && !wifi.isWifiEnabled) {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    @Suppress("DEPRECATION")
                    if (!wifi.setWifiEnabled(true)) {
                        dataUpdater.showSnackBar(R.string.turn_on_wifi_is_failed)
                    }
                } else {
                    // Android 10 (API 29) 以降はアプリから直接 ON にできない。
                    // 必要に応じて Settings.ACTION_WIFI_SETTINGS などのインテントを発行してやる
                    Log.w(tag, "Cannot enable Wi-Fi programmatically on Android 10+")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startWatchWifiStatus(context: Context) {
        Log.v(tag, "startWatchWifiStatus()")
        try {
            val filter = IntentFilter().apply {
                @Suppress("DEPRECATION")
                addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
                @Suppress("DEPRECATION")
                addAction(ConnectivityManager.CONNECTIVITY_ACTION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    addAction(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)
                }
            }
            context.applicationContext.registerReceiver(connectionReceiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopWatchWifiStatus(context: Context) {
        Log.v(tag, "stopWatchWifiStatus()")
        try {
            context.applicationContext.unregisterReceiver(connectionReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private inner class WiFiCallback(
        private val callback: WifiConnectNotify
    ) : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            Log.v(tag, "onAvailable $network")
            callback.onWifiConnected(true)
        }

        override fun onLost(network: Network) {
            Log.v(tag, "onLost $network")
            callback.onWifiConnected(false)
        }
    }
}
