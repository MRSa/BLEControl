package net.osdn.gokigen.blecontrol.lib.ui.brainwave

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import net.osdn.gokigen.blecontrol.lib.ble.R
import net.osdn.gokigen.blecontrol.lib.data.brainwave.BrainwaveDataHolder

class BrainwaveRawGraphView : View, IBrainwaveDataDrawer {
    private val TAG = this.toString()
    private var dataHolder: BrainwaveDataHolder? = null
    private var context: Context? = null

    constructor(context: Context) : super(context) {
        initComponent(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initComponent(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initComponent(context)
    }

    private fun initComponent(context: Context) {
        try {
            Log.v(TAG, " initialize.")
            this.context = context
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCanvas(canvas)

        // Show Message(Overwrite)
        drawInformationMessages(canvas)
    }

    fun setDataHolder(dataHolder: BrainwaveDataHolder?) {
        this.dataHolder = dataHolder
    }


    private fun drawCanvas(canvas: Canvas) {
        val centerY = canvas.getHeight() / 2

        val magnification = 1.0f
        val maxRange = 2200.0f

        //Log.v(TAG, " Canvas SIZE : (" + canvas.getWidth() + "," + canvas.getHeight() +" )");
        val rangeHeight = canvas.getHeight()
        val rangeWidth = canvas.getWidth()

        val resolution = ((rangeHeight / 2.0f) / maxRange)

        // Clears the canvas.
        canvas.drawARGB(255, 0, 0, 0)

        // 背景真ん中のライン
        val bgLine = Paint()
        bgLine.setColor(Color.DKGRAY)
        canvas.drawLine(0f, centerY.toFloat(), rangeWidth.toFloat(), centerY.toFloat(), bgLine)

        val paint = Paint()
        paint.setColor(Color.WHITE)

        var maxValue = 0
        var minValue = 0
        val values = dataHolder!!.getValues(rangeWidth)
        if (values != null) {
            var pointX = 0
            //magnification
            var previousY = centerY.toFloat()
            for (value in values) {
                val currentY = ((value).toFloat()) * resolution * magnification + centerY
                canvas.drawLine(
                    pointX.toFloat(),
                    previousY,
                    (pointX + 1).toFloat(),
                    currentY,
                    paint
                )
                pointX++
                previousY = currentY
                if (maxValue < value) {
                    maxValue = value
                }
                if (minValue > value) {
                    minValue = value
                }
            }
        }
        val message = "max: " + maxValue + " min: " + minValue
        //Log.v(TAG, message);
        canvas.drawText(message, (rangeWidth - 125).toFloat(), 20f, paint)

        paint.setColor(Color.argb(255, 32, 32, 32))
        var lineY = ((maxValue).toFloat()) * resolution * magnification + centerY
        canvas.drawLine(0f, lineY, canvas.getWidth().toFloat(), lineY, paint)

        lineY = ((minValue).toFloat()) * resolution * magnification + centerY
        canvas.drawLine(0f, lineY, canvas.getWidth().toFloat(), lineY, paint)
    }


    /**
     * 　 画面にメッセージを表示する
     */
    private fun drawInformationMessages(canvas: Canvas) {
        try {
            val summaryData = dataHolder!!.summaryData
            val paint = Paint()
            paint.setColor(Color.DKGRAY)

            val metrics = paint.getFontMetrics()
            val lineHeight = (metrics.bottom - metrics.top).toInt() + 2
            var positionY = 20

            var message =
                context!!.getString(R.string.value_title_attention) + " " + summaryData.getAttention()
            canvas.drawText(message, 10f, positionY.toFloat(), paint)
            positionY = positionY + lineHeight

            message =
                context!!.getString(R.string.value_title_mediation) + " " + summaryData.getMediation()
            canvas.drawText(message, 10f, positionY.toFloat(), paint)
            positionY = positionY + lineHeight


            if (!summaryData.isSkinConnected) {
                paint.setColor(Color.RED)
                var notConnectMessage = "Sensor lead is not connected."
                if (context != null) {
                    notConnectMessage = context!!.getString(R.string.sensor_not_contacted)
                }
                canvas.drawText(notConnectMessage, 10f, positionY.toFloat(), paint)
            }
            paint.setColor(Color.DKGRAY)
            positionY = canvas.getHeight() - lineHeight

            var value = summaryData.getMidGamma()
            paint.setColor(Color.DKGRAY)
            message = context!!.getString(R.string.value_title_midGamma) + " " + value
            canvas.drawText(message, 10f, positionY.toFloat(), paint)
            positionY = positionY - lineHeight

            value = summaryData.getLowGamma()
            paint.setColor(Color.DKGRAY)
            message = context!!.getString(R.string.value_title_lowGamma) + " " + value
            canvas.drawText(message, 10f, positionY.toFloat(), paint)
            positionY = positionY - lineHeight

            value = summaryData.getHighBeta()
            paint.setColor(Color.DKGRAY)
            message = context!!.getString(R.string.value_title_highBeta) + " " + value
            canvas.drawText(message, 10f, positionY.toFloat(), paint)
            positionY = positionY - lineHeight

            value = summaryData.getLowBeta()
            paint.setColor(Color.DKGRAY)
            message = context!!.getString(R.string.value_title_lowBeta) + " " + value
            canvas.drawText(message, 10f, positionY.toFloat(), paint)
            positionY = positionY - lineHeight

            value = summaryData.getHighAlpha()
            paint.setColor(Color.DKGRAY)
            message = context!!.getString(R.string.value_title_highAlpha) + " " + value
            canvas.drawText(message, 10f, positionY.toFloat(), paint)
            positionY = positionY - lineHeight

            value = summaryData.getLowAlpha()
            paint.setColor(Color.DKGRAY)
            message = context!!.getString(R.string.value_title_lowAlpha) + " " + value
            canvas.drawText(message, 10f, positionY.toFloat(), paint)
            positionY = positionY - lineHeight

            value = summaryData.getTheta()
            paint.setColor(Color.DKGRAY)
            message = context!!.getString(R.string.value_title_theta) + " " + value
            canvas.drawText(message, 10f, positionY.toFloat(), paint)
            positionY = positionY - lineHeight

            value = summaryData.getDelta()
            paint.setColor(Color.DKGRAY)
            message = context!!.getString(R.string.value_title_delta) + " " + value
            canvas.drawText(message, 10f, positionY.toFloat(), paint)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun drawGraph() {
        postInvalidate()
    }
}

