package net.osdn.gokigen.blecontrol.lib.ble.connect;

public interface ICameraPowerOn
{
    // カメラ起動指示
    void wakeup(PowerOnCameraCallback callback);

    // 実行終了時のコールバックのインタフェース
    interface PowerOnCameraCallback
    {
        void wakeupExecuted(boolean isExecute);
    }
}
