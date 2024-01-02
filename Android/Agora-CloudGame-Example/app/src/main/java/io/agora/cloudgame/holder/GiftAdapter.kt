package io.agora.cloudgame.holder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.agora.cloudgame.example.R
import io.agora.cloudgame.network.model.GiftEntity
import io.agora.cloudgame.widget.AmountView
import io.agora.cloudgame.widget.AsyncImageView
import me.add1.iris.Callback

class GiftAdapter constructor(
    context: Context?,
    list: ArrayList<GiftEntity>?,
    callback: Callback<GiftEntity>?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mInflater: LayoutInflater? = null

    private var mData: ArrayList<GiftEntity>? = null
    private var mCallback: Callback<GiftEntity>? = null

    private var mContext: Context? = null

    init {
        mInflater = LayoutInflater.from(context)
        mCallback = callback
        mData = list
        mContext = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View? = mInflater?.inflate(R.layout.item_share_gift, parent, false)
        return GiftViewHolder(view!!, mContext, mCallback, mData)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as GiftViewHolder
        val entity = mData!![position]
        viewHolder.linearLayout.background =
            mContext?.getDrawable(if (entity.isSelect) R.drawable.shape_gift_select else R.drawable.shape_gift_in_select)
        viewHolder.nameView.text = entity.name
        viewHolder.cicadaView.text = entity.price.toString()
        viewHolder.imageView.load(entity.thumbnail, null)
        viewHolder.amountView.amount = entity.giftNum
    }

    override fun getItemCount(): Int {
        return mData!!.size
    }

    @SuppressLint("ResourceAsColor")
    class GiftViewHolder(
        itemView: View,
        context: Context?,
        callback: Callback<GiftEntity>?,
        mData: ArrayList<GiftEntity>?
    ) : RecyclerView.ViewHolder(itemView) {
        var nameView: TextView
        var imageView: AsyncImageView
        var cicadaView: TextView
        var amountView: AmountView
        var linearLayout: LinearLayout

        init {
            nameView = itemView.findViewById(R.id.name_view)
            imageView = itemView.findViewById(R.id.image_view)
            cicadaView = itemView.findViewById(R.id.cicada_view)
            amountView = itemView.findViewById(R.id.amount_view)
            linearLayout = itemView.findViewById(R.id.line_layout)

            amountView.amount = 1

            itemView.setOnClickListener {
                for (entity in mData!!) {
                    entity.isSelect = false
                }
                mData[layoutPosition].isSelect = true
                mData[layoutPosition].giftNum = amountView.amount
                callback!!.callback(mData[layoutPosition])
                linearLayout.background = context?.getDrawable(R.drawable.shape_gift_select)
            }
            amountView.setOnAmountChangeListener { view, amount, position ->
                for (entity in mData!!) {
                    entity.isSelect = false
                }
                mData[layoutPosition].isSelect = true
                mData[layoutPosition].giftNum = amount
                callback!!.callback(mData[layoutPosition])
                linearLayout.background = context?.getDrawable(R.drawable.shape_gift_select)
            }

        }
    }

}