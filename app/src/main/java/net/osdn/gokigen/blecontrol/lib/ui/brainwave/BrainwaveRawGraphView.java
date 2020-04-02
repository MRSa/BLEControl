package net.osdn.gokigen.blecontrol.lib.ui.brainwave;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import net.osdn.gokigen.blecontrol.lib.data.brainwave.BrainwaveDataHolder;

public class BrainwaveRawGraphView extends View implements IBrainwaveDataDrawer
{
    private final String TAG = this.toString();
    private BrainwaveDataHolder dataHolder = null;

    public BrainwaveRawGraphView(@NonNull Context context)
    {
        super(context);
        initComponent(context);
    }

    public BrainwaveRawGraphView(@NonNull Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initComponent(context);
    }

    public BrainwaveRawGraphView(@NonNull Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initComponent(context);
    }

    private void initComponent(@NonNull Context context)
    {
        try
        {

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        drawCanvas(canvas);

        // Show Message(Overwrite)
        drawInformationMessages(canvas);
    }

    public void setDataHolder(BrainwaveDataHolder dataHolder)
    {
        this.dataHolder = dataHolder;
    }


    private void drawCanvas(Canvas canvas)
    {
        //int centerX = canvas.getWidth() / 2;
        //int centerY = canvas.getHeight() / 2;

        Log.v(TAG, " Canvas SIZE : (" + canvas.getWidth() + "," + canvas.getHeight() +" )");

        int rangeHeight = canvas.getHeight();

        // Clears the canvas.
        canvas.drawARGB(255, 0, 0, 0);
    }


    /**
     * 　 画面にメッセージを表示する
     */
    private void drawInformationMessages(Canvas canvas)
    {

    }

    @Override
    public void drawGraph()
    {
        postInvalidate();
    }
}

