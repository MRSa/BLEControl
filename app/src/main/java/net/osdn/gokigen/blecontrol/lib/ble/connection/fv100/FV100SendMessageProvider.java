package net.osdn.gokigen.blecontrol.lib.ble.connection.fv100;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class FV100SendMessageProvider
{
    private final String TAG = toString();
    private List<Integer> msgIdList;
    private List<String> typeList;
    private List<String> paramList;

    private int tokenId = 0;
    private int index = 0;
    private int offset = 0;

    private int sequene = 0;
    private boolean isMessageFinished = false;


    FV100SendMessageProvider()
    {
        initializeMsgIdList();
    }

    void resetSequence()
    {
        index = 0;
        offset = 0;
        isMessageFinished = false;
    }

    void setTokenId(int tokenId)
    {
        Log.v(TAG, " set token ID : " + tokenId);
        this.tokenId = tokenId;
    }

    byte[] provideMessage()
    {
        byte[] messageToSend = createSendMessage(msgIdList.get(index), typeList.get(index), paramList.get(index));

        Log.v(TAG, "INDEX: " + index + " LENGTH: " + messageToSend.length + " OFFSET: " + offset + " " + Arrays.toString(messageToSend));
        ByteArrayOutputStream baosm = new ByteArrayOutputStream();
        try
        {
            if (offset == 0)
            {
                if (messageToSend.length > offset + 20)
                {
                    baosm.write(Arrays.copyOfRange(messageToSend, offset, offset + 20));
                    offset = offset + 20;
                }
                else
                {
                    baosm.write(Arrays.copyOfRange(messageToSend, offset, messageToSend.length));
                    offset = offset + messageToSend.length;
                }
            }
            else
            {
                if (messageToSend.length > offset + 20)
                {
                    baosm.write((byte) 0x02);  // メッセージは続く
                    baosm.write(Arrays.copyOfRange(messageToSend, offset, offset + 20));
                    offset = offset + 20;
                }
                else
                {
                    baosm.write((byte) 0x03);  // メッセージ終了
                    baosm.write(Arrays.copyOfRange(messageToSend, offset, messageToSend.length));
                    offset = offset + messageToSend.length;
                }
            }
            if (offset >= messageToSend.length)
            {
                index++;
                offset = 0;
                if (index > msgIdList.size())
                {
                    index = 0;
                    isMessageFinished = true;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        byte[] message = baosm.toByteArray();
        Log.v(TAG, "provideMessage() [" + message.length + "] " + Arrays.toString(message));
        return (message);
    }

    boolean isMessageFinished()
    {
        return (isMessageFinished);
    }

    private void initializeMsgIdList()
    {
        msgIdList = new ArrayList<>();
        typeList = new ArrayList<>();
        paramList = new ArrayList<>();

        msgIdList.add(257);    // {"msg_id":257,"token":0}
        typeList.add(null);
        paramList.add(null);

        msgIdList.add(18);     // {"msg_id":18,"token":1}
        typeList.add(null);
        paramList.add(null);

        msgIdList.add(17);     // {"msg_id":17,"token":1}
        typeList.add(null);
        paramList.add(null);

        msgIdList.add(5);      // {"msg_id":5,"token":1}
        typeList.add(null);
        paramList.add(null);

        msgIdList.add(11);     // {"msg_id":11,"token":1}
        typeList.add(null);
        paramList.add(null);

        //msgIdList.add(1);      // {"msg_id":1,"token":1,"type":"ap_mode"}
        //typeList.add("ap_mode");
        //paramList.add(null);

        msgIdList.add(61441);  // {"msg_id":61441,"token":1}
        typeList.add(null);
        paramList.add(null);
    }

    private byte[] createSendMessage(int msg_id, @Nullable String type, @Nullable String param)
    {
        String data = "{\"msg_id\":" + msg_id + ",\"token\":" + tokenId;
        if (type != null)
        {
            data = data + ",\"type\":\"" + type + "\'";
        }
        if (param != null)
        {
            data = data + ",\"param\":\"" + param + "\'";
        }
        data = data + "}";

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try
        {
            byte[] header = {(byte) 0x01, (byte) 0x00, (byte) data.length()};
            output.write(header);
            output.write(data.getBytes());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (output.toByteArray());
    }

}
