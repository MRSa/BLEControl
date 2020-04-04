package net.osdn.gokigen.blecontrol.lib.ui.brainwave;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
            Log.v(TAG, " initialize.");
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
        int centerY = canvas.getHeight() / 2;

        float magnification = 1.0f;
        float maxRange = 2200.0f;

        //Log.v(TAG, " Canvas SIZE : (" + canvas.getWidth() + "," + canvas.getHeight() +" )");

        int rangeHeight = canvas.getHeight();
        int rangeWidth = canvas.getWidth();

        float resolution = ((rangeHeight / 2.0f) / maxRange);

        // Clears the canvas.
        canvas.drawARGB(255, 0, 0, 0);

        // 背景真ん中のライン
        Paint bgLine = new Paint();
        bgLine.setColor(Color.DKGRAY);
        canvas.drawLine(0, centerY, rangeWidth, centerY, bgLine);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        int maxValue = 0;
        int minValue = 0;
        int[] values = dataHolder.getValues(rangeWidth);
        if (values != null)
        {
            int pointX = 0;
            //magnification
            float previousY = centerY;
            for (int value : values)
            {
                float currentY = ((float)(value)) * resolution * magnification + centerY;
                canvas.drawLine(pointX, previousY, (pointX + 1), currentY, paint);
                pointX++;
                previousY = currentY;
                if (maxValue < value)
                {
                    maxValue = value;
                }
                if (minValue > value)
                {
                    minValue = value;
                }
            }
        }
        String message = "max: " + maxValue + " min: " + minValue;
        //Log.v(TAG, message);
        canvas.drawText(message, rangeWidth - 125, 20, paint);

        paint.setColor(Color.argb(255, 32, 32, 32));
        float lineY = ((float)(maxValue)) * resolution * magnification + centerY;
        canvas.drawLine(0, lineY, canvas.getWidth(), lineY, paint);

        lineY = ((float)(minValue)) * resolution * magnification + centerY;
        canvas.drawLine(0, lineY, canvas.getWidth(), lineY, paint);
    }


    /**
     * 　 画面にメッセージを表示する
     */
    private void drawInformationMessages(Canvas canvas)
    {
        //Paint paint = new Paint();
        //paint.setColor(Color.DKGRAY);
        //canvas.drawText("[" + canvas.getWidth() + "," + canvas.getHeight() + "]", 20, 20, paint);

    }

    @Override
    public void drawGraph()
    {
        postInvalidate();
    }
}

