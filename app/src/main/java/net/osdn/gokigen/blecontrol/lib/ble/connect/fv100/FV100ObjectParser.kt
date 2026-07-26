package net.osdn.gokigen.blecontrol.lib.ble.connect.fv100

import android.util.Log
import org.json.JSONObject

internal class FV100ObjectParser
    (private val notifier: ReceivedDataNotify) {
    private val TAG: String = toString()

    fun parseData(receivedData: String): String {
        var parsedData = ""
        try {
            val data = receivedData.substring(receivedData.indexOf("{"))
            Log.v(TAG, " RECV: $data")
            val `object` = JSONObject(data)
            run {
                val msgId = getObjectInt(`object`, "msg_id")
                when (msgId) {
                    REPLY_INITIAL ->                         //  {"rval":0,"msg_id":257,"param":1}
                        parsedData = parseInitialMessage(msgId, `object`)

                    REPLY_SET_PROPERTY ->                         //  {"rval":0,"msg_id":2,"type":"camera_clock"}
                        parsedData = parseSetPropertyMessage(msgId, `object`)

                    REPLY_CONNECTION_ROUTE ->                         //  {"rval":0,"msg_id":18,"type":"wireless","param":"Connection"}
                        parsedData = parseConnectionMessage(msgId, `object`)

                    REPLY_HARDWARE_ID ->                         //  {"rval":0,"msg_id":1,"type":"ap_mode","param":"0"}
                        parsedData = parseHardwareMessage(msgId, `object`)

                    REPLY_BATTERY_STATUS ->                         //   {"rval":0,"msg_id":17,"type":"charging","param":100}
                        parsedData = parseBatteryStatusMessage(msgId, `object`)

                    REPLY_STORAGE_INFO ->                         //  {"rval":0,"msg_id":5,"total":(total),"free":(free),"photo_num":XXXX,"video_length":YYYY}
                        parsedData = parseContentInfoMessage(msgId, `object`)

                    REPLY_WIFI_MODE ->                         //  {"rval":0,"msg_id":1,"type":"ap_mode","param":"0"}
                        parsedData = parseWifiModeMessage(msgId, `object`)

                    REPLY_WIFI_INFO ->                         //  {"rval":0,"msg_id":61441,"ssid":"Canon FV100 BL ????","passwd": "XXXXXXXX"}
                        parsedData = parseWifiInfoMessage(msgId, `object`)

                    REPLY_DIRECTORY_CONTENT ->                         //  {"rval":0,"msg_id":1282,"listing":[{"DCIM/":"(DateTime))"},{"File":"(DateTime)"}]}
                        parsedData = parseDirectoryContentMessage(msgId, `object`)

                    DUPLICATE_CONNECTION -> parsedData = parseDuplicateConnection(receivedData)
                    EVENT_STATUS ->                         //  {"msg_id":7,"type":"operation_mode","mode":"wireless","param":"Connection"}
                        parsedData = parseEventMessage(msgId, `object`)

                    else -> parsedData = "?(" + msgId + "): " + data
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (parsedData)
    }

    private fun getObjectInt(`object`: JSONObject, name: String): Int {
        var value = -1
        try {
            value = `object`.getInt(name)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (value)
    }

    private fun getObjectString(`object`: JSONObject, name: String): String {
        var value = ""
        try {
            value = `object`.getString(name)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (value)
    }


    private fun parseInitialMessage(msgId: Int, `object`: JSONObject): String {
        var parsedData = ""
        try {
            //  {"rval":0,"msg_id":257,"param":1}
            val rval = getObjectInt(`object`, "rval")
            val param = getObjectInt(`object`, "param")
            //parsedData = "id: " + msgId + " rval: " + rval + " param: " + param;
            if (rval == 0) {
                parsedData = " Initial. : " + param + "(" + rval + ")"
            }
            notifier.setTokenId(param)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (parsedData)
    }

    private fun parseSetPropertyMessage(msgId: Int, `object`: JSONObject): String {
        var parsedData = ""
        try {
            //  {"rval":0,"msg_id":2,"type":"camera_clock"}
            //  {"rval":0,"msg_id":2,"type":"ap_mode"}
            //  {"rval":0,"msg_id":2,"type":"gps_info"}
            val rval = getObjectInt(`object`, "rval")
            val type = getObjectString(`object`, "type")
            if (rval == 0) {
                parsedData = " " + type + " : OK"
            } else {
                parsedData = " " + type + " : NG (" + rval + ")"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (parsedData)
    }


    private fun parseBatteryStatusMessage(msgId: Int, `object`: JSONObject): String {
        var parsedData = ""
        try {
            //   {"rval":0,"msg_id":17,"type":"charging","param":100}
            val rval = getObjectInt(`object`, "rval")
            val type = getObjectString(`object`, "type")
            val param = getObjectInt(`object`, "param")
            if (rval == 0) {
                parsedData = " Battery: " + param + "% " + type
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (parsedData)
    }


    private fun parseContentInfoMessage(msgId: Int, `object`: JSONObject): String {
        var parsedData = ""
        try {
            //  {"rval":0,"msg_id":5,"total":(total),"free":(free),"photo_num":XXXX,"video_length":YYYY}
            val rval = getObjectInt(`object`, "rval")
            val total = getObjectInt(`object`, "total")
            val free = getObjectInt(`object`, "free")
            val photo = getObjectInt(`object`, "photo_num")
            val video = getObjectInt(`object`, "video_length")
            if (rval == 0) {
                parsedData =
                    " Memory Card : " + free + "/" + total + "\n (photo: " + photo + " video: " + video + ")"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (parsedData)
    }

    private fun parseHardwareMessage(msgId: Int, `object`: JSONObject): String {
        var parsedData = ""
        try {
            //   {"rval":0,"msg_id":11,"wifi_mac":"xx:...","ble_mac":"xx:...","model_number":"FV-100","serial_number":"..","firmware_version":"..", "rf_version":".."}
            val rval = getObjectInt(`object`, "rval")
            val wifi_mac = getObjectString(`object`, "wifi_mac")
            val ble_mac = getObjectString(`object`, "ble_mac")
            val model_number = getObjectString(`object`, "model_number")
            val serial_number = getObjectString(`object`, "serial_number")
            val firmware_version = getObjectString(`object`, "firmware_version")
            val rf_version = getObjectString(`object`, "rf_version")
            if (rval == 0) {
                parsedData =
                    " Model: " + model_number + "\n Serial: " + serial_number + "\n WIFI: " + wifi_mac + "\n BLE: " + ble_mac + "\n FirmVer: " + firmware_version + "\n RF_Ver.: " + rf_version + "\n"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (parsedData)
    }

    private fun parseWifiModeMessage(msgId: Int, `object`: JSONObject): String {
        var parsedData = ""
        try {
            //  {"rval":0,"msg_id":1,"type":"ap_mode","param":"0"}
            val rval = getObjectInt(`object`, "rval")
            val type = getObjectString(`object`, "type")
            val param = getObjectString(`object`, "param")
            if (rval == 0) {
                parsedData = " WIFI: " + type + " : " + param + " (" + rval + ")"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (parsedData)
    }

    private fun parseWifiInfoMessage(msgId: Int, `object`: JSONObject): String {
        var parsedData = ""
        try {
            //  {"rval":0,"msg_id":61441,"ssid":"Canon FV100 BL ????","passwd": "XXXXXXXX"}
            val rval = getObjectInt(`object`, "rval")
            val ssid = getObjectString(`object`, "ssid")
            val passwd = getObjectString(`object`, "passwd")
            if (rval == 0) {
                parsedData = " WIFI: " + ssid + "  " + passwd + "\n\n\n\n"
            }
            notifier.detectWifiKey(ssid, passwd)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (parsedData)
    }

    private fun parseDirectoryContentMessage(msgId: Int, `object`: JSONObject): String {
        var parsedData = ""
        try {
            //  {"rval":0,"msg_id":1282,"listing":[{"DCIM/":"(DateTime))"},{"File":"(DateTime)"}]}
            val rval = getObjectInt(`object`, "rval")
            parsedData = " " + msgId + " : " + rval
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (parsedData)
    }

    private fun parseConnectionMessage(msgId: Int, `object`: JSONObject): String {
        var parsedData = ""
        try {
            //  {"rval":0,"msg_id":18,"type":"wireless","param":"Connection"}
            val rval = getObjectInt(`object`, "rval")
            val type = getObjectString(`object`, "type")
            val param = getObjectString(`object`, "param")
            val mode = getObjectString(`object`, "mode")
            if (rval == 0) {
                parsedData = " " + type + " : " + param + " " + mode
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (parsedData)
    }

    private fun parseDuplicateConnection(receivedData: String): String {
        val data = receivedData.substring(receivedData.indexOf("{"))
        Log.v(TAG, " RECV: " + data)
        return (" : " + parseData(data.substring(data.indexOf("{", 2))))
    }

    private fun parseEventMessage(msgId: Int, `object`: JSONObject): String {
        var parsedData = ""
        try {
            //  {"msg_id":7,"type":"operation_mode","mode":"wireless","param":"Connection"}
            //  {"msg_id":7,"type":"device_orientation","param":"vertical"}
            //  {"msg_id":7,"type":"force_disconnect","param":"switch off"}
            val type = getObjectString(`object`, "type")
            val param = getObjectString(`object`, "param")
            val mode = getObjectString(`object`, "mode")
            parsedData = " " + type + " : " + param + " " + mode
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (parsedData)
    }

    interface ReceivedDataNotify {
        fun setTokenId(id: Int)
        fun detectWifiKey(ssId: String?, key: String?)
    }

    companion object {
        private const val REPLY_INITIAL = 257
        private const val REPLY_SET_PROPERTY = 2
        private const val REPLY_CONNECTION_ROUTE = 18
        private const val REPLY_BATTERY_STATUS = 17
        private const val REPLY_STORAGE_INFO = 5
        private const val REPLY_DIRECTORY_CONTENT = 1282
        private const val REPLY_HARDWARE_ID = 11
        private const val REPLY_WIFI_MODE = 1
        private const val REPLY_WIFI_INFO = 61441
        private const val DUPLICATE_CONNECTION = 1793
        private const val EVENT_STATUS = 7
    }
}
