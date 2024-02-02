package io.agora.cloudgame.delegate;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import io.agora.cloudgame.KeyCenter;
import io.agora.cloudgame.constants.Constants;
import io.agora.cloudgame.example.databinding.GameDetailWebviewBinding;
import io.agora.cloudgame.network.model.GameEntity;
import io.agora.cloudgame.webview.WebAppInterface;
import io.agora.cloudgame.widget.ViewUtils;
import me.add1.iris.utilities.ThreadUtils;

public class GameDetailsWebViewDelegate extends GameDetailsBaseDelegate implements WebAppInterface.WebAppInterfaceListener {
    private GameDetailWebviewBinding mBinding;

    private WebAppInterface mWebAppInterface;


    public GameDetailsWebViewDelegate(GameEntity entity, boolean live) {
        super(entity, live);
    }

    @Override
    protected View getRootView(LayoutInflater inflater, ViewGroup container) {
        mBinding = GameDetailWebviewBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    protected void initData() {
        TAG = Constants.TAG + "-" + GameDetailsWebViewDelegate.class.getSimpleName();
        super.initData();
        mWebAppInterface = new WebAppInterface(this);
        mIsNativeRtc = false;
    }


    @Override
    protected void initView() {
        mZView = mBinding.zView;
        mLikeView = mBinding.likeView;
        mCommentView = mBinding.commentView;
        mGiftView = mBinding.giftView;
        mNameView = mBinding.nameView;
        mRootView = mBinding.rootView;
        mOperatorLayout = mBinding.operatorLayout;
        mChatLayout = mBinding.inputBottom.chatBottom;
        mChatRightView = mBinding.inputBottom.chatRight;
        mChatInputEditText = mBinding.inputBottom.chatInput;
        mBackView = mBinding.backView;
        mGameViewLayout = mBinding.webviewLayout;
        mGameStateTv = mBinding.gameStateTv;
        super.initView();

        initWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        mBinding.webviewLayout.getSettings().setJavaScriptEnabled(true);
        mBinding.webviewLayout.getSettings().setSupportZoom(false);
        mBinding.webviewLayout.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mBinding.webviewLayout.getSettings().setUseWideViewPort(true);
        mBinding.webviewLayout.getSettings().setLoadWithOverviewMode(true);

        mBinding.webviewLayout.loadUrl("file:///android_asset/web_rtc/index.html");

        mBinding.webviewLayout.addJavascriptInterface(mWebAppInterface, "NativeInterface");

        mBinding.webviewLayout.setInitialScale(100);
        mBinding.webviewLayout.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i(TAG, "onPageFinished:" + url);
                if (!isLiveRole) {
                    callJs("joinChannel(" + false + ",'" + mGameEntity.rtcConfig.channelName + "'," + KeyCenter.getUserUid() + ",'" + KeyCenter.APP_ID + "','" + KeyCenter.getRtcToken(mGameEntity.rtcConfig.channelName, KeyCenter.getUserUid()) + "')", value -> {
                        Log.i(TAG, "joinChannel onReceiveValue:" + value);
                    });
                }
            }
        });
        mBinding.webviewLayout.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(TAG, "web console message:" + consoleMessage.message() + " from line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return super.onConsoleMessage(consoleMessage);

            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                super.onPermissionRequest(request);
                logD("onPermissionRequest :" + Arrays.toString(request.getResources()));
                //request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
            }
        });

    }

    @Override
    protected void onGameStateChange(String state) {
        super.onGameStateChange(state);
        if ("started".equals(state)) {
            if (!isJoinChannel) {
                isJoinChannel = true;
                String token = KeyCenter.getRtcToken(mGameEntity.rtcConfig.channelName, mGameEntity.rtcConfig.broadcastUid);
                String channelName = mGameEntity.rtcConfig.channelName;
                int uid = mGameEntity.rtcConfig.broadcastUid;
                callJs("joinChannel(" + true + ",'" + channelName + "'," + uid + ",'" + KeyCenter.APP_ID + "','" + token + "')", value -> Log.i(TAG, "liver joinChannel onReceiveValue:" + value));
            }
        }
    }

    @Override
    protected void sendStreamMessage(byte[] data) {
        super.sendStreamMessage(data);
        callJs("sendDataStream('" + Base64.getEncoder().encodeToString(data) + "')", value -> {
            Log.i(TAG, "sendEventMessages onReceiveValue:" + value);
            if (io.agora.cloudgame.constants.Constants.SUCCESS_RET.equals(value)) {
                mEventMessagelist.clear();
            } else {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("控制事件发送失败(sendStreamMessage)！");
                    }
                });
            }
        });
    }


    @Override
    protected void onDestroyView() {
        super.onDestroyView();

        if (isJoinChannel) {
            callJs("leave()", value -> Log.i(TAG, "leaves channel onReceiveValue:" + value));
            isJoinChannel = false;
        }

//        mBinding.webviewLayout.removeJavascriptInterface("NativeInterface");
//        mBinding.webviewLayout.destroy();
    }

    private void callJs(String methodWithParams, ValueCallback<String> callback) {
        logD("callJs methodWithParams:" + methodWithParams);
        mBinding.webviewLayout.evaluateJavascript("javascript:" + methodWithParams, callback);
    }

    @Override
    public void onVideoSizeChange(int width, int height) {
        if (null != this.getContext()) {
            ThreadUtils.postOnUiThread(() -> {
                final int gameLayoutWidth = mBinding.gameLayout.getMeasuredWidth();
                final int gameLayoutHeight = mBinding.gameLayout.getMeasuredHeight();
                Log.i(TAG, "onVideoSizeChange->gameLayoutWidth:" + gameLayoutWidth + ",gameLayoutHeight:" + gameLayoutHeight);
                int targetWidth = gameLayoutWidth;
                int targetHeight;
                float scale = (float) targetWidth / width;
                targetHeight = (int) (height * scale);

                final int topMargin = gameLayoutHeight - targetHeight;

                Log.i(TAG, "onVideoSizeChange->targetWidth:" + targetWidth + ",targetHeight:" + targetHeight + ",topMargin:" + topMargin);

                mBinding.webviewLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        ViewGroup.LayoutParams layoutParams = mBinding.webviewLayout.getLayoutParams();
                        layoutParams.width = targetWidth;
                        layoutParams.height = targetHeight;
                        if (topMargin < 0) {
                            ((LinearLayout.LayoutParams) layoutParams).topMargin = topMargin;
                        }
                        mBinding.webviewLayout.setLayoutParams(layoutParams);

                        if (topMargin > 10) {
                            ViewGroup.LayoutParams operatorLayoutParams = mBinding.operatorLayout.getLayoutParams();
                            operatorLayoutParams.height = topMargin - ViewUtils.dp2px(GameDetailsWebViewDelegate.this.getContext(), 10);
                            ((CoordinatorLayout.LayoutParams) operatorLayoutParams).bottomMargin = 0;
                            mBinding.operatorLayout.setLayoutParams(operatorLayoutParams);
                        }
                    }
                });
            });
        }
    }


}
