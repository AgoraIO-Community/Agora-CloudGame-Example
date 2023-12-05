package io.agora.cloudgame.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ViewJudge {

    INSTANCE;

    public boolean mFiltered;

    Map<Integer, String> mMapStr = new HashMap<>();

    public String getStringText(@NonNull TextView view) {
        return view.getText().toString();
    }

    public String getStringEdit(@NonNull EditText view) {
        return view.getText().toString();
    }

    public boolean isEmpty(@NonNull String str) {
        return TextUtils.isEmpty(str);
    }

    public boolean isEmptyEdit(@NonNull EditText view) {
        return TextUtils.isEmpty(getStringEdit(view));
    }

    public boolean isEmptyText(@NonNull TextView view) {
        return TextUtils.isEmpty(getStringText(view));
    }

    public int getLength(@NonNull String str) {
        return str.length();
    }

    public int getLengthEdit(@NonNull EditText view) {
        return getStringEdit(view).length();
    }

    public int getLengthText(@NonNull TextView view) {
        return getStringText(view).length();
    }

    public boolean isEmail(@NonNull String email) {
        if (null == email || "".equals(email)) return false;

        String str =
                "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);

        return m.matches();
    }

    public boolean isInteger(@NonNull String str) {
        if (str.length() != 10) return false;
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public String getGenderString(@NonNull int arg0) {
        mMapStr.clear();
        mMapStr.put(0, "");
        mMapStr.put(1, "Male");
        mMapStr.put(2, "Female");
        mMapStr.put(3, "Secret");
        return mMapStr.get(arg0);
    }

    public Integer getGenderInteger(@NonNull String arg0) {

        for (Integer key : mMapStr.keySet()) {
            if (mMapStr.get(key).equals(arg0)) {
                return key;
            }
        }
        return 0;
    }

    public void getEditFocus(@NonNull Context context, EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
        editText.setSelection(getLengthEdit(editText));
    }


    public int getWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    public int getHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    public boolean isSoftShowing(@NonNull Activity activity) {
        int screenHeight = activity.getWindow().getDecorView().getHeight();
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return (screenHeight - rect.bottom) > screenHeight * 0.3;
    }

    public void hideKeyboard(@NonNull Activity activity) {
        InputMethodManager imm =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && activity.getCurrentFocus() != null) {
            if (activity.getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public void setTextChangedListener(@NonNull EditText editText,
                                       @NonNull ResultHandler resultHandler) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @SuppressLint({"ResourceAsColor", "NewApi"})
            @Override
            public void afterTextChanged(Editable editable) {
                if (resultHandler != null) {
                    resultHandler.handler(editable.toString());
                }
            }
        });
    }

    public interface ResultHandler {
        void handler(String text);
    }

    public boolean touchEventInView(View view, float x, float y) {
        if (view == null) {
            return false;
        }

        int[] location = new int[2];
        view.getLocationOnScreen(location);

        int left = location[0];
        int top = location[1];

        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();

        if (y >= top && y <= bottom && x >= left && x <= right) {
            return true;
        }

        return false;
    }

    public void addImg(EditText et, int res, String name, Context mc) {
        Drawable drawable = mc.getResources().getDrawable(res);
        drawable.setBounds(0, 0, 60, 60);
        SpannableString spannable = new SpannableString(name);
        ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
        spannable.setSpan(span, 0, name.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        et.append(spannable);
    }

    public List<String> extractMessageByRegular(String msg) {

        List<String> list = new ArrayList<String>();
        if (TextUtils.isEmpty(msg)) return list;
        Pattern p = Pattern.compile("(\\[[^\\]]*\\])");
        Matcher m = p.matcher(msg);
        while (m.find()) {
            list.add(m.group());
        }
        return list;
    }

}
