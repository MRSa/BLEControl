package net.osdn.gokigen.blecontrol.lib.ui.fv100

import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import net.osdn.gokigen.blecontrol.lib.ble.R

class FV100PropertySetting internal constructor(
    private val context: FragmentActivity,
    private val propertySetter: PropertySetter
) : View.OnClickListener {
    private val TAG = toString()

    override fun onClick(v: View) {
        val id = v.getId()
        when (id) {
            R.id.change_image_size_button -> changeImageSize()
            R.id.change_video_size_button -> changeVideoSize()
            else -> Log.v(TAG, " onClick : " + id)
        }
    }

    private fun changeImageSize() {
        try {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.select_image_size))
            builder.setCancelable(true)
            builder.setSingleChoiceItems(
                R.array.photo_size,
                -1,
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        Log.i(TAG, " Index : " + which)
                        try {
                            if (which >= 0) {
                                val selectionList =
                                    context.getResources().getStringArray(R.array.photo_size_value)
                                val param = selectionList[which]

                                // 撮影イメージサイズの変更
                                propertySetter.setProperty("photo_size", param)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        dialog.dismiss()
                    }
                })
            builder.setNegativeButton(
                R.string.btn_cancel,
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        dialog.cancel()
                    }
                })
            builder.create()
            builder.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeVideoSize() {
        try {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.select_video_resolution))
            builder.setCancelable(true)
            builder.setSingleChoiceItems(
                R.array.video_size,
                -1,
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        Log.i(TAG, " Index : " + which)
                        try {
                            if (which >= 0) {
                                val selectionList =
                                    context.getResources().getStringArray(R.array.video_size_value)
                                val param = selectionList[which]

                                // ビデオ撮影サイズの変更
                                propertySetter.setProperty("video_resolution", param)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        dialog.dismiss()
                    }
                })
            builder.setNegativeButton(
                R.string.btn_cancel,
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        dialog.cancel()
                    }
                })
            builder.create()
            builder.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface PropertySetter {
        fun setProperty(propertyName: String, propertyValue: String)
    }
}
