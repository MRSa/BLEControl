package net.osdn.gokigen.blecontrol.lib.ui.fv100;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
        try
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.select_image_size));
            builder.setCancelable(true);
            builder.setSingleChoiceItems(R.array.photo_size, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(TAG, " Index : " + which);
                    try
                    {
                        if (which >= 0)
                        {
                            String[] selectionList = context.getResources().getStringArray(R.array.photo_size_value);
                            String param = selectionList[which];

                            // 撮影イメージサイズの変更
                            propertySetter.setProperty("photo_size", param);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(@NonNull DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });
            builder.create();
            builder.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void changeVideoSize()
    {
        try
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.select_video_resolution));
            builder.setCancelable(true);
            builder.setSingleChoiceItems(R.array.video_size, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(TAG, " Index : " + which);
                    try
                    {
                        if (which >= 0)
                        {
                            String[] selectionList = context.getResources().getStringArray(R.array.video_size_value);
                            String param = selectionList[which];

                            // ビデオ撮影サイズの変更
                            propertySetter.setProperty("video_resolution", param);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(@NonNull DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });
            builder.create();
            builder.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public interface PropertySetter
    {
        void setProperty(@NonNull String propertyName, @NonNull String propertyValue);
    }
}
