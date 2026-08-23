package net.osdn.gokigen.blecontrol.lib.ui

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import net.osdn.gokigen.blecontrol.lib.ble.R

class ConfirmationDialog : DialogFragment() {
    private var context: Context? = null

    private fun prepare(context: Context?) {
        this.context = context
    }

    fun show(titleResId: Int, messageResId: Int, callback: Callback) {
        var title = ""
        var message = ""

        // タイトルとメッセージをのダイアログを表示する
        if (context != null) {
            title = context!!.getString(titleResId)
            message = context!!.getString(messageResId)
        }
        show(title, message, callback)
    }

    fun show(title: String?, message: String?, callback: Callback) {
        // 確認ダイアログの生成
        val alertDialog = AlertDialog.Builder(context!!)
        alertDialog.setTitle(title)
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert)
        alertDialog.setMessage(message)
        alertDialog.setCancelable(true)

        // ボタンを設定する（実行ボタン）
        alertDialog.setPositiveButton(
            context!!.getString(R.string.dialog_positive_execute),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    callback.confirm()
                    dialog.dismiss()
                }
            })

        // ボタンを設定する (キャンセルボタン）
        alertDialog.setNegativeButton(
            context!!.getString(R.string.dialog_negative_cancel),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.cancel()
                }
            })

        // 確認ダイアログを表示する
        alertDialog.show()
    }

    fun show(iconResId: Int, title: String?, message: String?) {
        // 表示イアログの生成
        val alertDialog = AlertDialog.Builder(context!!)
        alertDialog.setTitle(title)
        alertDialog.setIcon(iconResId)
        alertDialog.setMessage(message)
        alertDialog.setCancelable(true)

        // ボタンを設定する（実行ボタン）
        alertDialog.setPositiveButton(
            context!!.getString(R.string.dialog_positive_execute),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                }
            })

        // 確認ダイアログを表示する
        alertDialog.show()
    }

    // コールバックインタフェース
    interface Callback {
        fun confirm()
    }

    companion object {
        fun newInstance(context: Context?): ConfirmationDialog {
            val instance = ConfirmationDialog()
            instance.prepare(context)

            return (instance)
        }
    }
}
