package io.agora.cloudgame.webview;

import android.util.Log;
import android.webkit.JavascriptInterface;

import io.agora.cloudgame.constants.Constants;

public class WebAppInterface {
    private static final String TAG = Constants.TAG + "-WebAppInterface";

    @JavascriptInterface
    public void method1() {
        Log.d(TAG, "method1");
    }

    @JavascriptInterface
    public void method2(String param1, String param2) {
        Log.e(TAG, "method2" + param1 + param2);
    }
}
