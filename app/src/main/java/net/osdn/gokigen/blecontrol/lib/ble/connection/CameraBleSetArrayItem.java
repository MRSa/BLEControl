package net.osdn.gokigen.blecontrol.lib.ble.connection;

class CameraBleSetArrayItem
{
    private final String dataId;
    private String btName;
    private String btPassCode;
    private String information;

    CameraBleSetArrayItem(String dataId, String name, String passCode, String information)
    {
        this.dataId = dataId;
        this.btName = name;
        this.btPassCode = passCode;
        this.information = information;
    }

    String getDataId()
    {
        return (dataId);
    }

    String getBtName()
    {
        return (btName);
    }

    String getBtPassCode()
    {
        return (btPassCode);
    }

    String getInformation()
    {
        return (information);
    }
}
