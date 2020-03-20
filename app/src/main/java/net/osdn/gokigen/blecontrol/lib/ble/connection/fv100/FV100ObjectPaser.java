package net.osdn.gokigen.blecontrol.lib.ble.connection.fv100;

import org.json.JSONObject;

class FV100ObjectPaser
{

    FV100ObjectPaser()
    {

    }

    String parseData(String receivedData)
    {
        String parsedData = "";
        try
        {
            JSONObject object = new JSONObject(receivedData.substring(receivedData.indexOf("{")));
            {
                int msgId = getObjectInt(object, "msg_id");
                int rval = getObjectInt(object, "rval");
                parsedData = "id: " + msgId + " rval: " + rval;
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


}
