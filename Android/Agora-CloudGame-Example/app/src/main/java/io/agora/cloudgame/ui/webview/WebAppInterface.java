package io.agora.cloudgame.ui.webview;

import android.util.Log;
import android.webkit.JavascriptInterface;

import io.agora.cloudgame.constants.Constants;

public class WebAppInterface {
    private static final String TAG = Constants.TAG + "-WebAppInterface";
    private WebAppInterfaceListener mListener;

    public WebAppInterface(WebAppInterfaceListener listener) {
        mListener = listener;
    }

    @JavascriptInterface
    public void onVideoSizeChange(int width, int height) {
        Log.i(TAG, "onVideoSizeChange: width = " + width + ", height = " + height);
        if (null != mListener) {
            mListener.onVideoSizeChange(width, height);
        }

    }

    public interface WebAppInterfaceListener {
        default void onVideoSizeChange(int width, int height) {

        }
    }
}
