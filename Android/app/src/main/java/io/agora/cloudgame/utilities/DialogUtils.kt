package io.agora.cloudgame.utilities

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import io.agora.cloudgame.constants.Constants
import io.agora.cloudgame.example.R
import io.agora.cloudgame.holder.GiftAdapter
import io.agora.cloudgame.network.model.GiftEntity
import me.add1.iris.Callback


class DialogUtils {
    companion object {

        fun show(context: Context, title: String, message: String, callback: Callback<Boolean>) {
            MaterialDialog(context)
                .title(text = title)
                .message(text = message)
                .show {
                    positiveButton(R.string.alert_dialog_ok) {
                        dismiss()
                        callback.callback(true)
                    }
                    negativeButton(R.string.alert_dialog_cancel) {
                        dismiss()
                        callback.callback(false)
                    }

                }
        }


        fun showOk(
            context: Context,
            @StringRes title: Int,
            @StringRes message: Int,
            callback: Callback<Boolean>
        ) {
            MaterialDialog(context)
                .title(title)
                .message(message)
                .show {
                    positiveButton(R.string.alert_dialog_ok) {
                        dismiss()
                        callback.callback(true)
                    }
                }
        }

        fun gameGift(context: Context, callback: Callback<String>) {
            val dialog = MaterialDialog(context)
                .cornerRadius(null, R.dimen.dialog_corner)
                .customView(R.layout.view_game_join)
                .show {}
            dialog.window?.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            dialog.window?.setGravity(Gravity.BOTTOM)
            dialog.window?.setWindowAnimations(R.style.dialog_style)

            val textView = dialog.getCustomView().findViewById<TextView>(R.id.join_view)

            textView.setOnClickListener {
                callback.callback("")
                dialog.dismiss()
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun showGift(
            context: Context,
            list: ArrayList<GiftEntity>?,
            callback: Callback<GiftEntity>
        ) {

            val dialog = MaterialDialog(context)
                .cornerRadius(null, R.dimen.dialog_corner)
                .customView(R.layout.view_gift)
                .show {}
            dialog.window?.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            dialog.window?.setGravity(Gravity.BOTTOM)

            val rareView = dialog.getCustomView().findViewById<RecyclerView>(R.id.rare_view)

            val rareManager = GridLayoutManager(context, 4)
            rareManager.orientation = GridLayoutManager.VERTICAL
            rareView.layoutManager = rareManager
            var giftEntity: GiftEntity? = null

            for (entity in list!!) {
                entity.giftNum = 1
            }
            val giftAdapter = GiftAdapter(context, list, Callback { s: GiftEntity ->
                rareView.adapter!!.notifyDataSetChanged()
                giftEntity = s
                Log.d(Constants.TAG, "gift entity:$giftEntity")

            })
            rareView.adapter = giftAdapter

            dialog.findViewById<TextView>(R.id.send_view).setOnClickListener {
                if (null != giftEntity) {
                    dialog.dismiss()
                    callback.callback(giftEntity)
                }
            }
        }


    }
}


