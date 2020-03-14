package net.osdn.gokigen.blecontrol.lib.ble.connection;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;

import net.osdn.gokigen.blecontrol.lib.ble.R;


public class CameraBleSetArrayAdapter extends ArrayAdapter<CameraBleSetArrayItem>
{
    private final String TAG = toString();
    private final Context context;
    private LayoutInflater inflater;
    private final int textViewResourceId;
    private final ICameraSetDialogDismiss dialogDismiss;
    private List<CameraBleSetArrayItem> listItems;

    CameraBleSetArrayAdapter(Context context, int textId, List<CameraBleSetArrayItem> items, ICameraSetDialogDismiss dialogDismiss)
    {
        super(context, textId, items);

        this.context = context;
        textViewResourceId = textId;
        listItems = items;
        this.dialogDismiss = dialogDismiss;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     *
     */
    @Override
    public @NonNull
    View getView(int position, View convertView, @NonNull ViewGroup parent)
    {
        View view;
        if(convertView != null)
        {
            view = convertView;
        }
        else
        {
            view = inflater.inflate(textViewResourceId, null);
        }

        try
        {
            final CameraBleSetArrayItem item = listItems.get(position);

            final EditText btNameEdit = view.findViewWithTag("bt_name");
            btNameEdit.setText(item.getBtName());

            final EditText passCodeEdit = view.findViewWithTag("bt_passcode");
            passCodeEdit.setText(item.getBtPassCode());

            TextView infoView = view.findViewWithTag("info");
            infoView.setText(item.getInformation());

            Button button = view.findViewWithTag("button");
            button.setOnClickListener(new Button.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                    String idHeader = item.getDataId();
                    String btName = btNameEdit.getText().toString();
                    String btCode = passCodeEdit.getText().toString();
                    String itemInfo = item.getInformation();

                    Log.v(TAG, "CLICKED : " + idHeader + " " + btName + " [" + btCode + "] (" + item.getBtName() + " " + itemInfo + ")" );
                    if (dialogDismiss != null)
                    {
                        dialogDismiss.setOlyCameraSet(idHeader, btName, btCode, itemInfo);
                    }
                    Log.v(TAG, "REGISTERD CAMERA : " + idHeader + " " + btName);

                    // Toastで保管したことを通知する
                    String restoredMessage = context.getString(R.string.saved_my_camera) + btName;
                    Toast.makeText(context, restoredMessage, Toast.LENGTH_SHORT).show();

                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return (view);
    }

}
