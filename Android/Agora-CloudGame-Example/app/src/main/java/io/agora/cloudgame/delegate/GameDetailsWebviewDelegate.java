package io.agora.cloudgame.delegate;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.gyf.immersionbar.ImmersionBar;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import agora.pb.rctrl.RemoteCtrlMsg;
import io.agora.cloudgame.KeyCenter;
import io.agora.cloudgame.RareBackend;
import io.agora.cloudgame.Store;
import io.agora.cloudgame.example.BuildConfig;
import io.agora.cloudgame.example.R;
import io.agora.cloudgame.example.databinding.GameDetailWebviewBinding;
import io.agora.cloudgame.network.model.ApiResult;
import io.agora.cloudgame.network.model.GameEntity;
import io.agora.cloudgame.network.model.GameResult;
import io.agora.cloudgame.network.model.GiftEntity;
import io.agora.cloudgame.network.model.MessageEntity;
import io.agora.cloudgame.network.model.MessageEntityV2;
import io.agora.cloudgame.network.model.RtcConfig;
import io.agora.cloudgame.network.model.SendMessageV2;
import io.agora.cloudgame.utilities.DialogUtils;
import io.agora.cloudgame.webview.WebAppInterface;
import io.agora.cloudgame.widget.SoftKeyboardStateWatcher;
import io.agora.cloudgame.widget.ViewJudge;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import me.add1.iris.ApiRequestException;
import me.add1.iris.PageDelegate;
import me.add1.iris.utilities.ThreadUtils;

public class GameDetailsWebViewDelegate extends PageDelegate {

    private static final String TAG = io.agora.cloudgame.constants.Constants.TAG + "-" + GameDetailsWebViewDelegate.class.getSimpleName();

    protected boolean onStatusBarDarkFont() {
        return true;
    }

    protected int onStatusBarColor() {
        return R.color.white;
    }

    protected boolean immerse() {
        return true;
    }


    private GameDetailWebviewBinding mBinding;

    private final String appId = BuildConfig.APP_ID;

    private int mStreamId;

    private ArrayList<GiftEntity> mGifts;

    private final GameEntity mGameEntity;

    private final boolean isLiveRole;
    private boolean isJoinChannel;
    private ScheduledExecutorService mScheduledExecutorService;

    private final List<RemoteCtrlMsg.RctrlMsg> mEventMessagelist;
    private long mLastSendEventMessageTime;
    private String mPreGameState;
    private WebAppInterface mWebAppInterface;

    private final static int INTERVAL_SEND_EVENT_MESSAGE = 30;

    private final static int MESSAGE_SEND_EVENT_MESSAGE = 1;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_SEND_EVENT_MESSAGE:
                    Log.i(TAG, "MESSAGE_SEND_EVENT_MESSAGE");
                    sendEventMessages();
                    break;
                default:
                    break;
            }
        }
    };

    public GameDetailsWebViewDelegate(GameEntity entity, boolean live) {
        mGameEntity = entity;
        isLiveRole = live;
        isJoinChannel = false;
        mEventMessagelist = new ArrayList<>(1);
        mLastSendEventMessageTime = 0;
    }

    private void setImmerse() {
        if (!immerse()) {
            return;
        }

        ImmersionBar.with(getActivity())
                .statusBarColor(onStatusBarColor())
                .statusBarDarkFont(onStatusBarDarkFont(), 0.2f)
                .init();
    }

    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {

        mBinding = GameDetailWebviewBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    protected void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        initView();
    }

    private void init() {
        initData();
        initGameDetails();
        initListener();
    }

    private void initView() {
        if (null != getActivity()) {
            getActivity().findViewById(R.id.version).setVisibility(View.GONE);
        }
        if (isLiveRole) {
            mBinding.nameView.setText(mGameEntity.name + "\nuid: " + mGameEntity.rtcConfig.broadcastUid + "\n房间号：" + mGameEntity.roomId);
        } else {
            mBinding.nameView.setText(mGameEntity.name + "\nuid: " + KeyCenter.getUserUid() + "\n房间号：" + mGameEntity.roomId);
        }

        new SoftKeyboardStateWatcher(mBinding.rootView, getContext()).addSoftKeyboardStateListener(
                new SoftKeyboardStateWatcher.SoftKeyboardStateListener() {
                    @Override
                    public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                    }

                    @Override
                    public void onSoftKeyboardClosed() {
                        new Handler().postDelayed(() -> mBinding.operatorLayout.setVisibility(View.VISIBLE), 200);
                        mBinding.inputBottom.chatBottom.setVisibility(View.GONE);
                        mBinding.inputBottom.chatInput.getText().clear();
                    }
                }
        );

        mBinding.zView.setVisibility(isLiveRole ? View.VISIBLE : View.GONE);
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

        mWebAppInterface = new WebAppInterface();
        mBinding.webviewLayout.addJavascriptInterface(mWebAppInterface, "AndroidWebView");

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
        });

    }

    private void initData() {
        mPreGameState = "";
        mScheduledExecutorService = Executors.newScheduledThreadPool(1);

        mGameEntity.openId = isLiveRole ? "abcd" : KeyCenter.getUserUid() + "";
        mGameEntity.nickname = "00";
        mGameEntity.avatar = "https://agora-video-call.oss-cn-shanghai.aliyuncs.com/aigc/avatar.png";

        RtcConfig rtcConfig = new RtcConfig();
        rtcConfig.broadcastUid = KeyCenter.getBroadcastUid();
        rtcConfig.uid = mGameEntity.uid;
        rtcConfig.token = KeyCenter.getRtcToken(mGameEntity.roomId, rtcConfig.uid);
        rtcConfig.channelName = mGameEntity.roomId;
        mGameEntity.rtcConfig = rtcConfig;
        Log.i(TAG, "rtcConfig:" + rtcConfig);

        if (isLiveRole) {
            RareBackend.getInstance().startGame(appId, mGameEntity.gameId, mGameEntity, new RareBackend.ApiRequestCallback<GameResult>() {
                @Override
                public void onSucceed(@NonNull ApiResult<GameResult> t) {
                    if (!TextUtils.isEmpty(t.data.taskId)) {
                        mGameEntity.taskId = t.data.taskId;
                        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                            @Override
                            public void run() {
                                RareBackend.getInstance().gameState(appId, mGameEntity.gameId, mGameEntity.taskId,
                                        t1 -> {
                                            Log.i(TAG, "gameState->" + t1.data.status);
                                            if (null != getActivity()) {
                                                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mBinding.gameStateTv.setText(t1.data.status);
                                                    }
                                                });
                                            }
                                            //get game state
                                            if (t1.data.status.equals("started")) {
                                                if (!isJoinChannel) {
                                                    isJoinChannel = true;
                                                    String token = KeyCenter.getRtcToken(mGameEntity.rtcConfig.channelName, mGameEntity.rtcConfig.broadcastUid);
                                                    String channelName = mGameEntity.rtcConfig.channelName;
                                                    int uid = mGameEntity.rtcConfig.broadcastUid;
                                                    callJs("joinChannel(" + true + ",'" + channelName + "'," + uid + ",'" + KeyCenter.APP_ID + "','" + token + "')", value -> Log.i(TAG, "liver joinChannel onReceiveValue:" + value));
                                                }
                                            } else if (!mPreGameState.equals("schedule_failed") && t1.data.status.equals("schedule_failed")) {
                                                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        showToast("暂无可用的游戏资源，请稍后再试");
                                                    }
                                                });
                                            }

                                            mPreGameState = t1.data.status;
                                        });
                            }
                        }, 0, 1, TimeUnit.SECONDS);

                        RareBackend.getInstance().getStore().setModel(Store.Pref.ACCOUNT,
                                Store.KEY_GAME_DATA, mGameEntity);
                    }

                }

                @Override
                public void onFailure(@NonNull ApiRequestException e) {
                    if (isAlive() && !TextUtils.isEmpty(e.message)) {
                        showToast(e.code + " " + e.message);
                    }
                }
            });
        } else {
            //delay join channel
        }
    }

    private void initGameDetails() {
        RareBackend.getInstance().gamesDetails(appId, mGameEntity.gameId, new RareBackend.ApiRequestCallback<GameResult>() {
            @Override
            public void onSucceed(@NonNull ApiResult<GameResult> t1) {
                mGifts = (ArrayList<GiftEntity>) t1.data.gifts;
                ThreadUtils.postOnUiThread(() -> {
                    mBinding.likeView.setVisibility(t1.data.feature.like == 1 ? View.VISIBLE :
                            View.GONE);
                    mBinding.commentView.setVisibility(t1.data.feature.comment == 1 ? View.VISIBLE :
                            View.GONE);
                });
            }

            @Override
            public void onFailure(@NonNull ApiRequestException e) {
                if (isAlive() && !TextUtils.isEmpty(e.message)) {
                    showToast(e.code + " " + e.message);
                }
            }
        });

    }


    @SuppressLint({"ClickableViewAccessibility", "NonConstantResourceId"})
    private void initListener() {
        mBinding.setHandler(v -> {
            switch (v.getId()) {
                case R.id.back_view:
                    if (onBackPressed()) {
                        return;
                    }
                    Objects.requireNonNull(getActivity()).onBackPressed();
                    break;
                case R.id.gift_view:
                    if (mGifts == null) {
                        showToast("等待游戏开始");
                        break;
                    }
                    for (GiftEntity giftEntity : mGifts) {
                        giftEntity.isSelect = false;
                    }
                    DialogUtils.Companion.showGift(Objects.requireNonNull(getContext()), mGifts, t -> {
                        MessageEntity entity = new MessageEntity();
                        entity.giftId = t.id;
                        entity.giftValue = t.price * t.giftNum;
                        entity.giftNum = t.giftNum;
                        Log.i(TAG, "giftId:" + t.id + ",giftValue:" + t.value + ",giftNum:" + t.giftNum);
                        RareBackend.getInstance().sendGiftV2(appId, mGameEntity.roomId, mGameEntity.gameId,
                                getSendOperatorV2(entity),
                                new RareBackend.ApiRequestCallback<Boolean>() {
                                    @Override
                                    public void onSucceed(@NonNull ApiResult<Boolean> t) {
                                        ThreadUtils.postOnUiThread(() -> {
                                            showToast("赠送礼物成功");
                                        });
                                    }

                                    @Override
                                    public void onFailure(@NonNull ApiRequestException e) {
                                        showToast(e.code + "," + e.message);
                                    }
                                });
                    });
                    break;
                case R.id.comment_view:
                    mBinding.inputBottom.chatBottom.setVisibility(View.VISIBLE);
                    mBinding.operatorLayout.setVisibility(View.GONE);
                    ViewJudge.INSTANCE.getEditFocus(Objects.requireNonNull(getContext()), mBinding.inputBottom.chatInput);
                    break;
                case R.id.like_view:
                    setControllerView(mBinding.likeView, false);
                    MessageEntity entity = new MessageEntity();
                    entity.likeNum = 1;
                    setControllerView(mBinding.likeView, true);
                    RareBackend.getInstance().gameLikeV2(appId, mGameEntity.roomId, mGameEntity.gameId,
                            getSendOperatorV2(entity)
                            , t -> ThreadUtils.postOnUiThread(() -> {
                            }));
                    break;
                default:
                    break;
            }
        });

        mBinding.inputBottom.chatRight.setOnClickListener(v -> {
            MessageEntity entity = new MessageEntity();
            entity.content = mBinding.inputBottom.chatInput.getText().toString();
            ViewJudge.INSTANCE.hideKeyboard(Objects.requireNonNull(getActivity()));
            RareBackend.getInstance().gameCommentV2(appId, mGameEntity.roomId, mGameEntity.gameId,
                    getSendOperatorV2(entity), new RareBackend.ApiRequestCallback<Boolean>() {
                        @Override
                        public void onSucceed(@NonNull ApiResult<Boolean> t) {
                            showToast("发送评论成功");
                        }

                        @Override
                        public void onFailure(@NonNull ApiRequestException e) {
                            showToast("发送评论失败");
                        }
                    });
            mBinding.inputBottom.chatBottom.setVisibility(View.GONE);
            mBinding.inputBottom.chatInput.getText().clear();
        });

        mBinding.webviewLayout.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    sendMouseMessage(event, RemoteCtrlMsg.MouseEventType.MOUSE_EVENT_LBUTTON_DOWN.getNumber());
                    break;
                case MotionEvent.ACTION_UP:
                    sendMouseMessage(event, RemoteCtrlMsg.MouseEventType.MOUSE_EVENT_LBUTTON_UP.getNumber());
                    break;
                default:
                    break;
            }
            return true;
        });

        mBinding.zView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setControllerView(mBinding.zView, false);
                        sendKeyboardMessage(RemoteCtrlMsg.KeyboardEventType.KEYBOARD_EVENT_KEY_DOWN, 'Z');
                        break;
                    case MotionEvent.ACTION_UP:
                        setControllerView(mBinding.zView, true);
                        sendKeyboardMessage(RemoteCtrlMsg.KeyboardEventType.KEYBOARD_EVENT_KEY_UP, 'Z');
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

    }

    private void setControllerView(TextView textView, boolean isClick) {
        textView.setAlpha(isClick ? 1.0f : 0.5f);
        textView.setFocusable(isClick);
        textView.setClickable(isClick);
    }

    private synchronized void sendMouseMessage(MotionEvent event, int value) {
        if (!isLiveRole) {
            return;
        }
        Log.i(TAG, "sendMouseMessage:event x:" + event.getX() + ",event y:" + event.getY() + ",value:" + value);
        Log.i(TAG, "sendMouseMessage:event x/width:" + event.getX() / mBinding.webviewLayout.getMeasuredWidth() + ",event y/height:" + event.getY() / mBinding.webviewLayout.getMeasuredHeight());

        int x = ((int) (event.getX()) << 16) / mBinding.webviewLayout.getMeasuredWidth();
        int y = ((int) (event.getY()) << 16) / mBinding.webviewLayout.getMeasuredHeight();


        RemoteCtrlMsg.MouseEventMsg eventMsg = RemoteCtrlMsg.MouseEventMsg.newBuilder()
                .setMouseEvent(value)
                .setX(x)
                .setY(y)
                .build();

        RemoteCtrlMsg.RctrlMsg rctrlMsg = RemoteCtrlMsg.RctrlMsg.newBuilder()
                .setType(RemoteCtrlMsg.MsgType.MOUSE_EVENT_TYPE)
                .setTimestamp(System.currentTimeMillis())
                .setPayload(eventMsg.toByteString())
                .build();

        mEventMessagelist.add(rctrlMsg);
        if (System.currentTimeMillis() - mLastSendEventMessageTime > INTERVAL_SEND_EVENT_MESSAGE) {
            sendEventMessages();
        } else {
            mHandler.sendEmptyMessageDelayed(MESSAGE_SEND_EVENT_MESSAGE, INTERVAL_SEND_EVENT_MESSAGE - (System.currentTimeMillis() - mLastSendEventMessageTime));
        }
    }

    private synchronized void sendKeyboardMessage(RemoteCtrlMsg.KeyboardEventType eventType, char key) {
        if (!isLiveRole) {
            return;
        }

        Log.i(TAG, "sendKeyboardMessage:" + eventType + ",key:" + key);

        RemoteCtrlMsg.KeyboardEventMsg eventMsg = RemoteCtrlMsg.KeyboardEventMsg.newBuilder()
                .setVkey((int) key)
                .setState(eventType == RemoteCtrlMsg.KeyboardEventType.KEYBOARD_EVENT_KEY_DOWN ? 1 : 0xC0000001)
                .setKeyboardEvent(eventType.getNumber())
                .build();

        RemoteCtrlMsg.RctrlMsg rctrlMsg = RemoteCtrlMsg.RctrlMsg.newBuilder()
                .setType(RemoteCtrlMsg.MsgType.KEYBOARD_EVENT_TYPE)
                .setTimestamp(System.currentTimeMillis())
                .setPayload(eventMsg.toByteString())
                .build();

        mEventMessagelist.add(rctrlMsg);

        if (System.currentTimeMillis() - mLastSendEventMessageTime > INTERVAL_SEND_EVENT_MESSAGE) {
            sendEventMessages();
        } else {
            mHandler.sendEmptyMessageDelayed(MESSAGE_SEND_EVENT_MESSAGE, INTERVAL_SEND_EVENT_MESSAGE - (System.currentTimeMillis() - mLastSendEventMessageTime));
        }
    }


    private void sendEventMessages() {
        if (mEventMessagelist.size() == 0) {
            return;
        }
        mHandler.removeMessages(MESSAGE_SEND_EVENT_MESSAGE);
        Log.i(TAG, "sendEventMessages:" + mEventMessagelist.size());
        RemoteCtrlMsg.RctrlMsges rctrlMsges = RemoteCtrlMsg.RctrlMsges.newBuilder()
                .addAllMsges(mEventMessagelist)
                .build();

        mLastSendEventMessageTime = System.currentTimeMillis();

        callJs("sendDataStream('" + Base64.getEncoder().encodeToString(rctrlMsges.toByteArray()) + "')", value -> {
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

    private final IRtcEngineEventHandler iRtcEngineEventHandler = new IRtcEngineEventHandler() {

        @Override
        public void onVideoSizeChanged(Constants.VideoSourceType source, int uid, int width,
                                       int height, int rotation) {
            super.onVideoSizeChanged(source, uid, width, height, rotation);
            Log.i(TAG, "onVideoSizeChanged->" + uid + ", width->" + width + ", height->" + height);
//            if (null != mVideoView && null != GameDetailsWebviewDelegate.this.getContext()) {
//                ThreadUtils.postOnUiThread(() -> {
//                    final int gameLayoutWidth = mBinding.gameLayout.getMeasuredWidth();
//                    final int gameLayoutHeight = mBinding.gameLayout.getMeasuredHeight();
//                    Log.i(TAG, "onVideoSizeChanged->gameLayoutWidth:" + gameLayoutWidth + ",gameLayoutHeight:" + gameLayoutHeight);
//                    int targetWidth = gameLayoutWidth;
//                    int targetHeight;
//                    float scale = (float) targetWidth / width;
//                    targetHeight = (int) (height * scale);
//
//                    final int topMargin = gameLayoutHeight - targetHeight;
//
//                    Log.i(TAG, "onVideoSizeChanged->targetWidth:" + targetWidth + ",targetHeight:" + targetHeight + ",topMargin:" + topMargin);
//
//                    mVideoView.setVideoSize(targetWidth, targetHeight);
//                    mVideoView.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            ViewGroup.LayoutParams layoutParams = mBinding.frameLayout.getLayoutParams();
//                            layoutParams.width = targetWidth;
//                            layoutParams.height = targetHeight;
//                            if (topMargin < 0) {
//                                ((LinearLayout.LayoutParams) layoutParams).topMargin = topMargin;
//                            }
//                            mBinding.frameLayout.setLayoutParams(layoutParams);
//
//                            if (topMargin > 10) {
//                                ViewGroup.LayoutParams operatorLayoutParams = mBinding.operatorLayout.getLayoutParams();
//                                operatorLayoutParams.height = topMargin - ViewUtils.dp2px(GameDetailsWebviewDelegate.this.getContext(), 10);
//                                ((CoordinatorLayout.LayoutParams) operatorLayoutParams).bottomMargin = 0;
//                                mBinding.operatorLayout.setLayoutParams(operatorLayoutParams);
//                            }
//                        }
//                    });
//                });
//            }

        }

    };

    @Override
    protected void onActive() {
        super.onActive();
        setImmerse();
    }

    @Override
    protected void onDestroyView() {
        super.onDestroyView();
        if (isLiveRole) {
            RareBackend.getInstance().stopGame(appId, mGameEntity.gameId, mGameEntity, new RareBackend.ApiRequestCallback<Boolean>() {
                @Override
                public void onSucceed(@NonNull ApiResult<Boolean> t) {

                }

                @Override
                public void onFailure(@NonNull ApiRequestException e) {
                    if (isAlive() && !TextUtils.isEmpty(e.message)) {
                        showToast(e.code + " " + e.message);
                    }
                }
            });
        }

        callJs("leave()", value -> Log.i(TAG, "leaves channel onReceiveValue:" + value));

        if(null != mScheduledExecutorService){
            mScheduledExecutorService.shutdown();
        }
    }

    private SendMessageV2 getSendOperatorV2(MessageEntity message) {
        List<MessageEntityV2> list = new ArrayList<>();
        MessageEntityV2 messageEntityV2 = new MessageEntityV2();
        messageEntityV2.msgId = KeyCenter.APP_ID + "_" + System.currentTimeMillis();
        messageEntityV2.openId = mGameEntity.openId;
        messageEntityV2.content = message.content;
        messageEntityV2.giftId = message.giftId;
        messageEntityV2.giftNum = message.giftNum;
        messageEntityV2.giftValue = message.giftValue;
        messageEntityV2.likeNum = message.likeNum;
        messageEntityV2.avatarUrl = mGameEntity.avatar;
        messageEntityV2.nickname = mGameEntity.nickname;
        messageEntityV2.timestamp = System.currentTimeMillis();
        list.add(messageEntityV2);

        SendMessageV2 send = new SendMessageV2();
        send.payload = JSON.toJSONString(list);

        return send;
    }

    private void callJs(String methodWithParams, ValueCallback<String> callback) {
        mBinding.webviewLayout.evaluateJavascript("javascript:" + methodWithParams, callback);
    }

}
