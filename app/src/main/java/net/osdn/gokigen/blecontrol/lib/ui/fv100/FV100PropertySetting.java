package net.osdn.gokigen.blecontrol.lib.ui.fv100;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import net.osdn.gokigen.blecontrol.lib.ble.R;

public class FV100PropertySetting implements View.OnClickListener
{
    private String TAG = toString();
    private final FragmentActivity context;
    private final PropertySetter propertySetter;

    FV100PropertySetting(@NonNull FragmentActivity context, @NonNull PropertySetter propertySetter)
    {
        this.context = context;
        this.propertySetter = propertySetter;
    }


    @Override
    public void onClick(@NonNull View v)
    {
        int id = v.getId();
        switch (id)
        {
            case R.id.change_image_size_button:
                changeImageSize();
                break;

            case R.id.change_video_size_button:
                changeVideoSize();
                break;

            default:
                Log.v(TAG, " onClick : " + id);
                break;
        }
    }


    private void changeImageSize()
    {
        // 撮影イメージサイズの変更
        propertySetter.setProperty("photo_size", "2208x2208 1:1");
    }

    private void changeVideoSize()
    {
        // 撮影ビデオサイズの変更
        propertySetter.setProperty("video_resolution", "1280x720 30P 16:9");
    }

    public interface PropertySetter
    {
        void setProperty(@NonNull String propertyName, @NonNull String propertyValue);
    }
}
