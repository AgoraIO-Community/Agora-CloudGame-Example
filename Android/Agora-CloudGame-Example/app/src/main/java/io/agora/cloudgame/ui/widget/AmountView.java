package io.agora.cloudgame.ui.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import io.agora.cloudgame.example.R;

public class AmountView extends LinearLayout implements View.OnClickListener, TextWatcher {

    private static final String TAG = "AmountView";
    private int position = -1;

    private int amount = 0;
    private int goodsStorage = 100;
    private EditText etAmount;
    private ImageButton btnDecrease;
    private ImageButton btnIncrease;

    public AmountView(Context context) {
        this(context, null);
    }

    public AmountView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //组件布局
        LayoutInflater.from(context).inflate(R.layout.view_amount, this);
        etAmount = (EditText) findViewById(R.id.etAmount);
        btnDecrease = (ImageButton) findViewById(R.id.btnDecrease);
        etAmount.setVisibility(GONE);
        btnDecrease.setVisibility(GONE);
        btnIncrease = (ImageButton) findViewById(R.id.btnIncrease);
        btnDecrease.setOnClickListener(this);
        btnIncrease.setOnClickListener(this);
        etAmount.addTextChangedListener(this);
        etAmount.setFocusable(false);
    }


    /**
     * 位置
     *
     * @param position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * 设置库存方法
     *
     * @param goodsStorage
     */
    public void setGoodsStorage(int goodsStorage) {
        this.goodsStorage = goodsStorage;
    }

    /**
     * 获取数量
     *
     * @return
     */
    public int getAmount() {
        return amount;
    }

    /**
     * 设置数量
     *
     * @param amount
     */
    public void setAmount(int amount) {
        this.amount = amount;
        etAmount.setText(String.valueOf(this.amount));
        if (this.amount >= 0) {
            etAmount.setVisibility(VISIBLE);
            btnDecrease.setVisibility(VISIBLE);
            btnIncrease.setVisibility(VISIBLE);
        }
    }

    /**
     * 增加，减少事件监听
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btnDecrease) {
            if (amount > 0) {
                amount--;
                if (amount == 0) {
                    amount = 1;
                }
                etAmount.setText(String.valueOf(amount));
            }
        } else if (i == R.id.btnIncrease) {
            if (amount < goodsStorage) {
                amount++;
                etAmount.setText(String.valueOf(amount));
                if (amount >= 0) {
                    etAmount.setVisibility(VISIBLE);
                    btnDecrease.setVisibility(VISIBLE);
                }
            }
        }

        etAmount.clearFocus();

        if (mListener != null) {
            mListener.onAmountChange(this, amount, position);
        }
    }

    /**
     * 数量变化监听
     *
     * @param s
     * @param start
     * @param count
     * @param after
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().isEmpty()) {
            return;
        }
        amount = Integer.parseInt(s.toString());
        if (amount > goodsStorage) {
            etAmount.setText(String.valueOf(goodsStorage));
            return;
        }

    }

    /**
     * 自定义接口 监听数量变化，
     */
    private OnAmountChangeListener mListener;

    public interface OnAmountChangeListener {
        void onAmountChange(View view, int amount, int position);
    }

    public void setOnAmountChangeListener(OnAmountChangeListener onAmountChangeListener) {
        this.mListener = onAmountChangeListener;
    }
}
