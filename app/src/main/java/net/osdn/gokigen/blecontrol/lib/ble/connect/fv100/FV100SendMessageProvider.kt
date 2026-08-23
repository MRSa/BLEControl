package net.osdn.gokigen.blecontrol.lib.ble.connect.fv100

import android.util.Log
import java.io.ByteArrayOutputStream
import java.util.Arrays
import kotlin.math.min

internal class FV100SendMessageProvider
    (private val notifier: MessageSequenceNotify) {
    private val TAG: String = toString()
    private var msgIdList: MutableList<Int?>? = null
    private var typeList: MutableList<String?>? = null
    private var paramList: MutableList<String?>? = null

    private var tokenId = 0
    private var index = 0
    private var offset = 0
    private var isMessageFinished = false

    init {
        initializeMsgIdList()
    }

    fun resetSequence() {
        index = 0
        offset = 0
        isMessageFinished = false
        notifier.messageFinished(false)
    }

    fun setTokenId(tokenId: Int) {
        Log.v(TAG, " set token ID : " + tokenId)
        this.tokenId = tokenId
    }


    fun provideMessage(): ByteArray {
        val messageToSend = createSendMessage(
            msgIdList!!.get(index)!!,
            typeList!!.get(index),
            paramList!!.get(index)
        )

        Log.v(
            TAG,
            "INDEX: " + index + " LENGTH: " + messageToSend.size + " OFFSET: " + offset + " " + messageToSend.contentToString()
        )
        val baosm = ByteArrayOutputStream()
        try {
            if (offset == 0) {
                if (messageToSend.size > offset + 20) {
                    baosm.write(Arrays.copyOfRange(messageToSend, offset, offset + 20))
                    offset = offset + 20
                } else {
                    baosm.write(Arrays.copyOfRange(messageToSend, offset, messageToSend.size))
                    offset = offset + messageToSend.size
                }
            } else {
                if (messageToSend.size > offset + 20) {
                    baosm.write(0x02.toByte().toInt()) // メッセージは続く
                    baosm.write(Arrays.copyOfRange(messageToSend, offset, offset + 20))
                    offset = offset + 20
                } else {
                    baosm.write(0x03.toByte().toInt()) // メッセージ終了
                    baosm.write(Arrays.copyOfRange(messageToSend, offset, messageToSend.size))
                    offset = offset + messageToSend.size
                }
            }
            if (offset >= messageToSend.size) {
                index++
                offset = 0
                if (index >= msgIdList!!.size) {
                    index = 0
                    isMessageFinished = true
                    notifier.messageFinished(true)
                    Log.v(TAG, " - - - - - - -  STATUS GET SEQUENCE FINISHED  - - - - - - - ")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val message = baosm.toByteArray()
        Log.v(TAG, "provideMessage() [" + message.size + "] " + message.contentToString())
        return (message)
    }

    val isMessageSending: Boolean
        get() = (!isMessageFinished)

    private fun initializeMsgIdList() {
        msgIdList = ArrayList<Int?>()
        typeList = ArrayList<String?>()
        paramList = ArrayList<String?>()

        msgIdList!!.add(257) // {"msg_id":257,"token":0}  INDEX : 0
        typeList!!.add(null)
        paramList!!.add(null)

        msgIdList!!.add(18) // {"msg_id":18,"token":1}   INDEX : 1
        typeList!!.add(null)
        paramList!!.add(null)

        msgIdList!!.add(17) // {"msg_id":17,"token":1}   INDEX : 2
        typeList!!.add(null)
        paramList!!.add(null)

        msgIdList!!.add(5) // {"msg_id":5,"token":1}      INDEX : 3
        typeList!!.add(null)
        paramList!!.add(null)

        msgIdList!!.add(11) // {"msg_id":11,"token":1}    INDEX : 4
        typeList!!.add(null)
        paramList!!.add(null)

        //msgIdList.add(1);      // {"msg_id":1,"token":1,"type":"ap_mode"}
        //typeList.add("ap_mode");
        //paramList.add(null);
        msgIdList!!.add(61441) // {"msg_id":61441,"token":1}  INDEX : 5
        typeList!!.add(null)
        paramList!!.add(null)
    }

    private fun createSendMessage(
        msg_id: Int,
        type: String?,
        param: String?
    ): ByteArray {
        var data = "{\"msg_id\":" + msg_id + ",\"token\":" + tokenId
        if (type != null) {
            data = data + ",\"type\":\"" + type + "\""
        }
        if (param != null) {
            data = data + ",\"param\":\"" + param + "\""
        }
        data = data + "}"

        val output = ByteArrayOutputStream()
        try {
            val header = byteArrayOf(0x01.toByte(), 0x00.toByte(), data.length.toByte())
            output.write(header)
            output.write(data.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (output.toByteArray())
    }


    fun provideSetPropertyMessage(type: String?, param: String?): MutableList<ByteArray?> {
        val messageToSend = createSendMessage(2, type, param)
        val messageArray: MutableList<ByteArray?> = ArrayList<ByteArray?>()
        try {
            var addLength = 20
            val messageLength = messageToSend.size
            run {
                var offset = 0
                while (offset < messageLength) {
                    val targetLength = min((offset + addLength), messageLength)
                    val messageBlock = Arrays.copyOfRange(messageToSend, offset, targetLength)
                    val baosm = ByteArrayOutputStream()
                    if (targetLength == messageLength) {
                        baosm.write(0x03.toByte().toInt())
                    } else if (offset != 0) {
                        baosm.write(0x02.toByte().toInt())
                    } else  // if (offset == 0)
                    {
                        addLength--
                        offset++
                    }
                    baosm.write(messageBlock)
                    messageArray.add(baosm.toByteArray())
                    offset = offset + addLength
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (messageArray)
    }

    interface MessageSequenceNotify {
        fun messageFinished(isFinished: Boolean)
    }
}
