package net.osdn.gokigen.blecontrol.lib.ble.connection;

public interface ITextDataUpdater
{
    void setText(String data);
    void addText(String data);

    void enableOperation(boolean isEnable);
}
