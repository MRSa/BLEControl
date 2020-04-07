package net.osdn.gokigen.blecontrol.lib.data.brainwave;

import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import net.osdn.gokigen.blecontrol.lib.SimpleLogDumper;
import net.osdn.gokigen.blecontrol.lib.ble.R;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BrainwaveFileLogger
{
    private FileOutputStream outputStream;

    public BrainwaveFileLogger(@NonNull FragmentActivity context)
    {
        try
        {
            String fileNamePrefix = context.getString(R.string.app_name2) + "_EEG";
            Calendar calendar = Calendar.getInstance();
            String extendName = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(calendar.getTime());
            final String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/";
            String outputFileName = fileNamePrefix + "_" + extendName + ".bin";
            String filepath = new File(directoryPath.toLowerCase(), outputFileName.toLowerCase()).getPath();
            outputStream = new FileOutputStream(filepath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            outputStream = null;
        }
    }

    public void outputSummaryData(byte[] data)
    {
        try
        {
            SimpleLogDumper.dump_bytes("RECV [" + data.length + "] ", data);
            if ((outputStream != null)&&(data.length >= 36))
            {
                outputStream.write(data, 0, 36);
                outputStream.flush();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
