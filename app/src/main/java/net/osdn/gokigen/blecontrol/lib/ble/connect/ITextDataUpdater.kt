package net.osdn.gokigen.blecontrol.lib.ble.connect;

public interface ITextDataUpdater
{
    void setText(String data);
    void addText(String data);
    void showSnackBar(String message);
    void showSnackBar(int rscId);

    void enableOperation(boolean isEnable);
}
