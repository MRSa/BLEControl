package net.osdn.gokigen.blecontrol.lib.ble.connection.fv100;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

class FV100ObjectPaser
{
    private String TAG = toString();
    private static final int REPLY_INITIAL = 257;
    private static final int REPLY_SET_PROPERTY = 2;
    private static final int REPLY_CONNECTION_ROUTE = 18;
    private static final int REPLY_BATTERY_STATUS = 17;
    private static final int REPLY_STORAGE_INFO = 5;
    private static final int REPLY_DIRECTORY_CONTENT = 1282;
    private static final int REPLY_HARDWARE_ID = 11;
    private static final int REPLY_WIFI_MODE = 1;
    private static final int REPLY_WIFI_INFO = 61441;
    private static final int EVENT_STATUS = 7;

    private final ReceivedDataNotify notifier;

    FV100ObjectPaser(@NonNull ReceivedDataNotify notifier)
    {
        this.notifier = notifier;
    }

    String parseData(String receivedData)
    {
        String parsedData = "";
        try
        {
            String data = receivedData.substring(receivedData.indexOf("{"));
            Log.v(TAG, " RECV: " + data);
            JSONObject object = new JSONObject(data);
            {
                int msgId = getObjectInt(object, "msg_id");
                switch (msgId)
                {
                    case REPLY_INITIAL:
                        //  {"rval":0,"msg_id":257,"param":1}
                        parsedData = parseInitialMessage(msgId, object);
                        break;
                    case REPLY_SET_PROPERTY:
                        //  {"rval":0,"msg_id":2,"type":"camera_clock"}
                        parsedData = parseSetPropertyMessage(msgId, object);
                        break;

                    case REPLY_CONNECTION_ROUTE:
                        //  {"rval":0,"msg_id":18,"type":"wireless","param":"Connection"}
                        parsedData = parseConnectionMessage(msgId, object);
                        break;

                    case REPLY_HARDWARE_ID:
                        //  {"rval":0,"msg_id":1,"type":"ap_mode","param":"0"}
                        parsedData = parseHardwareMessage(msgId, object);
                        break;

                    case REPLY_BATTERY_STATUS:
                        //   {"rval":0,"msg_id":17,"type":"charging","param":100}
                        parsedData = parseBatteryStatusMessage(msgId, object);
                        break;

                    case REPLY_STORAGE_INFO:
                        //  {"rval":0,"msg_id":5,"total":(total),"free":(free),"photo_num":XXXX,"video_length":YYYY}
                        parsedData = parseContentInfoMessage(msgId, object);
                        break;

                    case REPLY_WIFI_MODE:
                        //  {"rval":0,"msg_id":1,"type":"ap_mode","param":"0"}
                        parsedData = parseWifiModeMessage(msgId, object);
                        break;

                    case REPLY_WIFI_INFO:
                        //  {"rval":0,"msg_id":61441,"ssid":"Canon FV100 BL ????","passwd": "XXXXXXXX"}
                        parsedData = parseWifiInfoMessage(msgId, object);
                        break;

                    case REPLY_DIRECTORY_CONTENT:
                        //  {"rval":0,"msg_id":1282,"listing":[{"DCIM/":"(DateTime))"},{"File":"(DateTime)"}]}
                        parsedData = parseDirectoryContentMessage(msgId, object);
                        break;

                    case EVENT_STATUS:
                        //  {"msg_id":7,"type":"operation_mode","mode":"wireless","param":"Connection"}
                        parsedData = parseEventMessage(msgId, object);
                        break;
                    default:
                        parsedData = "?(" + msgId + "): " + data;
                        break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (parsedData);
    }

    private int getObjectInt(JSONObject object, String name)
    {
        int value = -1;
        try
        {
            value = object.getInt(name);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (value);
    }

    private String getObjectString(JSONObject object, String name)
    {
        String value = "";
        try
        {
            value = object.getString(name);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (value);
    }


    private String parseInitialMessage(int msgId, @NonNull JSONObject object)
    {
        String parsedData = "";
        try
        {
            //  {"rval":0,"msg_id":257,"param":1}
            int rval = getObjectInt(object, "rval");
            int param = getObjectInt(object, "param");
            //parsedData = "id: " + msgId + " rval: " + rval + " param: " + param;
            parsedData = " Initial. : " + param + "(" + rval + ")";
            notifier.setTokenId(param);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (parsedData);
    }

    private String parseSetPropertyMessage(int msgId, @NonNull JSONObject object)
    {
        String parsedData = "";
        try
        {
            //  {"rval":0,"msg_id":2,"type":"camera_clock"}
            //  {"rval":0,"msg_id":2,"type":"ap_mode"}
            //  {"rval":0,"msg_id":2,"type":"gps_info"}
            int rval = getObjectInt(object, "rval");
            String type = getObjectString(object, "type");
            parsedData = " " + type + " : " + rval;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (parsedData);
    }


    private String parseBatteryStatusMessage(int msgId, @NonNull JSONObject object)
    {
        String parsedData = "";
        try
        {
            //   {"rval":0,"msg_id":17,"type":"charging","param":100}
            int rval = getObjectInt(object, "rval");
            String type = getObjectString(object, "type");
            int param = getObjectInt(object, "param");
            parsedData = " Battery: " + param + "% " + type;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (parsedData);
    }


    private String parseContentInfoMessage(int msgId, @NonNull JSONObject object)
    {
        String parsedData = "";
        try
        {
            //  {"rval":0,"msg_id":5,"total":(total),"free":(free),"photo_num":XXXX,"video_length":YYYY}
            int rval = getObjectInt(object, "rval");
            int total = getObjectInt(object, "total");
            int free = getObjectInt(object, "free");
            int photo = getObjectInt(object, "photo_num");
            int video = getObjectInt(object, "video_length");
            parsedData = " Memory Card : " + free + "/" + total + "\n (photo: " + photo + " video: " + video + ")";
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (parsedData);
    }

    private String parseHardwareMessage(int msgId, @NonNull JSONObject object)
    {
        String parsedData = "";
        try
        {
            //   {"rval":0,"msg_id":11,"wifi_mac":"xx:...","ble_mac":"xx:...","model_number":"FV-100","serial_number":"..","firmware_version":"..", "rf_version":".."}
            int rval = getObjectInt(object, "rval");
            String wifi_mac = getObjectString(object, "wifi_mac");
            String ble_mac = getObjectString(object, "ble_mac");
            String model_number = getObjectString(object, "model_number");
            String serial_number = getObjectString(object, "serial_number");
            String firmware_version = getObjectString(object, "firmware_version");
            String rf_version = getObjectString(object, "rf_version");
            parsedData = " Model: " + model_number + "\n Serial: " + serial_number + "\n WIFI: " + wifi_mac + "\n BLE: " + ble_mac + "\n FirmVer: " + firmware_version + "\n RF_Ver.: " + rf_version + "\n";
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (parsedData);
    }

    private String parseWifiModeMessage(int msgId, @NonNull JSONObject object)
    {
        String parsedData = "";
        try
        {
            //  {"rval":0,"msg_id":1,"type":"ap_mode","param":"0"}
            int rval = getObjectInt(object, "rval");
            String type = getObjectString(object, "type");
            String param = getObjectString(object, "param");
            parsedData = " WIFI: " + type + " : " + param + " (" + rval + ")";
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (parsedData);
    }

    private String parseWifiInfoMessage(int msgId, @NonNull JSONObject object)
    {
        String parsedData = "";
        try
        {
            //  {"rval":0,"msg_id":61441,"ssid":"Canon FV100 BL ????","passwd": "XXXXXXXX"}
            int rval = getObjectInt(object, "rval");
            String ssid = getObjectString(object, "ssid");
            String passwd = getObjectString(object, "passwd");
            parsedData = " WIFI: " + ssid + "  " + passwd + " (" + rval + ")\n\n\n";
            notifier.detectWifiKey(ssid, passwd);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (parsedData);
    }

    private String parseDirectoryContentMessage(int msgId, @NonNull JSONObject object)
    {
        String parsedData = "";
        try
        {
            //  {"rval":0,"msg_id":1282,"listing":[{"DCIM/":"(DateTime))"},{"File":"(DateTime)"}]}
            int rval = getObjectInt(object, "rval");
            parsedData = " " + msgId + " : " + rval;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (parsedData);
    }

    private String parseConnectionMessage(int msgId, @NonNull JSONObject object)
    {
        String parsedData = "";
        try
        {
            //  {"rval":0,"msg_id":18,"type":"wireless","param":"Connection"}
            int rval = getObjectInt(object, "rval");
            String type = getObjectString(object, "type");
            String param = getObjectString(object, "param");
            String mode = getObjectString(object, "mode");
            parsedData = " " + type + " : " + param + " " + mode;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (parsedData);
    }

    private String parseEventMessage(int msgId, @NonNull JSONObject object)
    {
        String parsedData = "";
        try
        {
            //  {"msg_id":7,"type":"operation_mode","mode":"wireless","param":"Connection"}
            //  {"msg_id":7,"type":"device_orientation","param":"vertical"}
            //  {"msg_id":7,"type":"force_disconnect","param":"switch off"}
            String type = getObjectString(object, "type");
            String param = getObjectString(object, "param");
            String mode = getObjectString(object, "mode");
            parsedData = " " + type + " : " + param + " " + mode;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (parsedData);
    }

    public interface ReceivedDataNotify
    {
        void setTokenId(int id);
        void detectWifiKey(String ssId, String key);
    }

}
