package net.osdn.gokigen.blecontrol.lib.ble.connect

class CameraBleSetArrayItem
    (
    private val dataId: String?,
    private val btName: String?,
    private val btPassCode: String?,
    private val information: String?
) {
    fun getDataId(): String? {
        return (dataId)
    }

    fun getBtName(): String? {
        return (btName)
    }

    fun getBtPassCode(): String? {
        return (btPassCode)
    }

    fun getInformation(): String? {
        return (information)
    }
}
