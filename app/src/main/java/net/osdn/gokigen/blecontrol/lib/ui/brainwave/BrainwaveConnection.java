package net.osdn.gokigen.blecontrol.lib.ui.brainwave;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import net.osdn.gokigen.blecontrol.lib.ble.R;
import net.osdn.gokigen.blecontrol.lib.ble.connect.ITextDataUpdater;
import net.osdn.gokigen.blecontrol.lib.ble.connect.eeg.MindWaveCommunication;
import net.osdn.gokigen.blecontrol.lib.ui.SnackBarMessage;

public class BrainwaveConnection implements View.OnClickListener, ITextDataUpdater
{
    private final String TAG = toString();
    private final FragmentActivity context;
    private final SelectDevice deviceSelection;
    private final BrainwaveMobileViewModel viewModel;
    private final MindWaveCommunication communicator;
    private final SnackBarMessage messageToShow;

    BrainwaveConnection(@NonNull FragmentActivity context, @NonNull SelectDevice deviceSelection, @NonNull BrainwaveMobileViewModel viewModel)
    {
        this.context = context;
        this.deviceSelection = deviceSelection;
        this.viewModel = viewModel;
        this.communicator = new MindWaveCommunication(context, this);
        this.messageToShow = new SnackBarMessage(context, false);
    }

    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        switch (id)
        {
            case R.id.connect_to_eeg:
                connectToEEG(deviceSelection.getSelectedDeviceName());
                break;

            default:
                Log.v(TAG, " onClick : " + id);
                break;
        }
    }

    private void connectToEEG(@Nullable final String selectedDevice)
    {
        if (selectedDevice == null)
        {
            Log.v(TAG, " DEVICE is NULL.");
            return;
        }
        try
        {
            Log.v(TAG, " CONNECT TO EEG. : " + selectedDevice);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    communicator.connect(selectedDevice);
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void setText(final String data)
    {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewModel.setText(data);
            }
        });
    }

    @Override
    public void addText(final String data)
    {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewModel.addText(data);
            }
        });
    }

    @Override
    public void showSnackBar(String message)
    {
        messageToShow.showMessage(message);
    }

    @Override
    public void showSnackBar(int rscId)
    {
        messageToShow.showMessage(rscId);
    }

    @Override
    public void enableOperation(final boolean isEnable)
    {
        try
        {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final ImageButton dummyButton = context.findViewById(R.id.dummy_button1);
                    if (dummyButton != null)
                    {
                        dummyButton.setEnabled(isEnable);
                        dummyButton.setVisibility((isEnable? View.INVISIBLE : View.INVISIBLE));
                    }
                    //Log.v(TAG, " >> ITextDataUpdater::enableOperation() : " + isEnable);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    interface SelectDevice
    {
        String getSelectedDeviceName();
    }
}
