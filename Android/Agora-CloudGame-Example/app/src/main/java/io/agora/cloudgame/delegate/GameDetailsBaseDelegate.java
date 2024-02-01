package io.agora.cloudgame.delegate;

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
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.gyf.immersionbar.ImmersionBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import agora.pb.rctrl.RemoteCtrlMsg;
import io.agora.cloudgame.KeyCenter;
import io.agora.cloudgame.RareBackend;
import io.agora.cloudgame.Store;
import io.agora.cloudgame.constants.Constants;
import io.agora.cloudgame.example.BuildConfig;
import io.agora.cloudgame.example.R;
import io.agora.cloudgame.network.model.ApiResult;
import io.agora.cloudgame.network.model.GameEntity;
import io.agora.cloudgame.network.model.GameResult;
import io.agora.cloudgame.network.model.GiftEntity;
import io.agora.cloudgame.network.model.MessageEntity;
import io.agora.cloudgame.network.model.MessageEntityV2;
import io.agora.cloudgame.network.model.RtcConfig;
import io.agora.cloudgame.network.model.SendMessageV2;
import io.agora.cloudgame.utilities.DialogUtils;
import io.agora.cloudgame.widget.SoftKeyboardStateWatcher;
import io.agora.cloudgame.widget.ViewJudge;
import me.add1.iris.ApiRequestException;
import me.add1.iris.PageDelegate;
import me.add1.iris.utilities.ThreadUtils;

public class GameDetailsBaseDelegate extends PageDelegate {
    protected static String TAG = Constants.TAG;

    protected boolean onStatusBarDarkFont() {
        return true;
    }

    protected int onStatusBarColor() {
        return R.color.white;
    }

    protected boolean immerse() {
        return true;
    }

    protected final String appId = BuildConfig.APP_ID;


    protected ArrayList<GiftEntity> mGifts;

    protected final GameEntity mGameEntity;

    protected final boolean isLiveRole;
    protected boolean isJoinChannel;
    protected ScheduledExecutorService mScheduledExecutorService;

    protected final List<RemoteCtrlMsg.RctrlMsg> mEventMessagelist;
    protected long mLastSendEventMessageTime;
    protected String mPreGameState;

    protected TextView mLikeView;
    protected View mCommentView;
    protected TextView mZView;
    protected View mGiftView;
    protected TextView mNameView;
    protected View mRootView;
    protected View mOperatorLayout;
    protected View mChatLayout;
    protected View mChatRightView;
    protected EditText mChatInputEditText;
    protected View mBackView;
    protected View mGameViewLayout;
    protected TextView mGameStateTv;

    protected boolean mIsNativeRtc;


    protected final static int INTERVAL_SEND_EVENT_MESSAGE = 30;

    protected final static int MESSAGE_SEND_EVENT_MESSAGE = 1;
    protected final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_SEND_EVENT_MESSAGE:
                    logI("MESSAGE_SEND_EVENT_MESSAGE");
                    sendEventMessages();
                    break;
                default:
                    break;
            }
        }
    };

    public GameDetailsBaseDelegate(GameEntity entity, boolean live) {
        mGameEntity = entity;
        isLiveRole = live;
        isJoinChannel = false;
        mEventMessagelist = new ArrayList<>(1);
        mLastSendEventMessageTime = 0;
    }

    protected void setImmerse() {
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
        return getRootView(inflater, container);
    }

    protected View getRootView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Override
    protected void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    protected void init() {
        initData();
        initRtc();
        initView();
        initListener();
        initGameDetails();
        startGame();
    }


    protected void initData() {
        mIsNativeRtc = true;
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
        logI("rtcConfig:" + rtcConfig);
    }

    protected void initRtc() {

    }

    protected void initView() {
        if (null != getActivity()) {
            getActivity().findViewById(R.id.version).setVisibility(View.GONE);
        }
        if (null != mZView) {
            mZView.setVisibility(isLiveRole ? View.VISIBLE : View.GONE);
        }

        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append("游戏名称：");
        nameBuilder.append(mGameEntity.name);
        nameBuilder.append("\n");
        nameBuilder.append("uid: ");
        nameBuilder.append(isLiveRole ? mGameEntity.rtcConfig.broadcastUid : KeyCenter.getUserUid());
        nameBuilder.append(isLiveRole ? "(主播)" : "(观众)");
        nameBuilder.append("\n");
        nameBuilder.append("房间号：");
        nameBuilder.append(mGameEntity.roomId);
        nameBuilder.append("(");
        nameBuilder.append(mIsNativeRtc ? "Native RTC" : "Web RTC");
        nameBuilder.append(")");
        if (null != mNameView) {
            mNameView.setText(nameBuilder.toString());
        }

        if (null != mRootView && null != mOperatorLayout && null != mChatLayout && null != mChatInputEditText) {
            new SoftKeyboardStateWatcher(mRootView, getContext()).addSoftKeyboardStateListener(
                    new SoftKeyboardStateWatcher.SoftKeyboardStateListener() {
                        @Override
                        public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                        }

                        @Override
                        public void onSoftKeyboardClosed() {
                            new Handler().postDelayed(() -> mOperatorLayout.setVisibility(View.VISIBLE), 200);
                            mChatLayout.setVisibility(View.GONE);
                            mChatInputEditText.getText().clear();
                        }
                    }
            );
        }
    }

    protected void initListener() {
        if (null != mBackView) {
            mBackView.setOnClickListener(v -> {
                if (onBackPressed()) {
                    return;
                }
                Objects.requireNonNull(getActivity()).onBackPressed();
            });
        }

        if (null != mGiftView) {
            mGiftView.setOnClickListener(v -> {
                if (mGifts == null) {
                    showToast("等待游戏开始");
                    return;
                }
                for (GiftEntity giftEntity : mGifts) {
                    giftEntity.isSelect = false;
                }
                DialogUtils.Companion.showGift(Objects.requireNonNull(getContext()), mGifts, t -> {
                    MessageEntity entity = new MessageEntity();
                    entity.giftId = t.id;
                    entity.giftValue = t.price * t.giftNum;
                    entity.giftNum = t.giftNum;
                    logI("giftId:" + t.id + ",giftValue:" + t.value + ",giftNum:" + t.giftNum);
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
            });
        }

        if (null != mCommentView) {
            mCommentView.setOnClickListener(v -> {
                mChatLayout.setVisibility(View.VISIBLE);
                mOperatorLayout.setVisibility(View.GONE);
                ViewJudge.INSTANCE.getEditFocus(Objects.requireNonNull(getContext()), mChatInputEditText);
            });
        }

        if (null != mLikeView) {
            mLikeView.setOnClickListener(v -> {
                setControllerView(mLikeView, false);
                MessageEntity entity = new MessageEntity();
                entity.likeNum = 1;
                setControllerView(mLikeView, true);
                RareBackend.getInstance().gameLikeV2(appId, mGameEntity.roomId, mGameEntity.gameId,
                        getSendOperatorV2(entity)
                        , t -> ThreadUtils.postOnUiThread(() -> {
                        }));
            });
        }

        if (null != mChatRightView && null != mChatInputEditText) {
            mChatRightView.setOnClickListener(v -> {
                MessageEntity entity = new MessageEntity();
                entity.content = mChatInputEditText.getText().toString();
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
                mChatLayout.setVisibility(View.GONE);
                mChatInputEditText.getText().clear();
            });
        }

        if (null != mGameViewLayout) {
            mGameViewLayout.setOnTouchListener((view, event) -> {
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
        }

        mZView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setControllerView(mZView, false);
                        sendKeyboardMessage(RemoteCtrlMsg.KeyboardEventType.KEYBOARD_EVENT_KEY_DOWN, 'Z');
                        break;
                    case MotionEvent.ACTION_UP:
                        setControllerView(mZView, true);
                        sendKeyboardMessage(RemoteCtrlMsg.KeyboardEventType.KEYBOARD_EVENT_KEY_UP, 'Z');
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void initGameDetails() {
        RareBackend.getInstance().gamesDetails(appId, mGameEntity.gameId, new RareBackend.ApiRequestCallback<GameResult>() {
            @Override
            public void onSucceed(@NonNull ApiResult<GameResult> t1) {
                ThreadUtils.postOnUiThread(() -> {
                    mGifts = (ArrayList<GiftEntity>) t1.data.gifts;
                    if (null != mLikeView) {
                        mLikeView.setVisibility(t1.data.feature.like == 1 ? View.VISIBLE :
                                View.GONE);
                    }
                    if (null != mCommentView) {
                        mCommentView.setVisibility(t1.data.feature.comment == 1 ? View.VISIBLE :
                                View.GONE);
                    }
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

    private void startGame() {
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
                                            if (null != getActivity() && null != mGameStateTv) {
                                                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mGameStateTv.setText(t1.data.status);
                                                    }
                                                });
                                            }

                                            onGameStateChange(t1.data.status);
                                            if (!mPreGameState.equals("schedule_failed") && t1.data.status.equals("schedule_failed")) {
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
            startGameForAudience();
        }
    }

    protected void onGameStateChange(String state) {
        logI("onGameStateChange:" + state);
    }


    protected void startGameForAudience() {

    }


    protected void setControllerView(TextView textView, boolean isClick) {
        textView.setAlpha(isClick ? 1.0f : 0.5f);
        textView.setFocusable(isClick);
        textView.setClickable(isClick);
    }

    protected synchronized void sendMouseMessage(MotionEvent event, int value) {
        if (!isLiveRole) {
            return;
        }
        logI("sendMouseMessage:event x:" + event.getX() + ",event y:" + event.getY() + ",value:" + value);

        int x = ((int) (event.getX()) << 16) / mGameViewLayout.getMeasuredWidth();
        int y = ((int) (event.getY()) << 16) / mGameViewLayout.getMeasuredHeight();


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


    protected synchronized void sendKeyboardMessage(RemoteCtrlMsg.KeyboardEventType eventType, char key) {
        if (!isLiveRole) {
            return;
        }

        logI("sendKeyboardMessage:" + eventType + ",key:" + key);

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
        logI("sendEventMessages:" + mEventMessagelist.size());
        RemoteCtrlMsg.RctrlMsges rctrlMsges = RemoteCtrlMsg.RctrlMsges.newBuilder()
                .addAllMsges(mEventMessagelist)
                .build();

        mLastSendEventMessageTime = System.currentTimeMillis();

        sendStreamMessage(rctrlMsges.toByteArray());
    }

    protected void sendStreamMessage(byte[] data) {

    }

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

        if (null != mScheduledExecutorService) {
            mScheduledExecutorService.shutdown();
        }
    }

    protected SendMessageV2 getSendOperatorV2(MessageEntity message) {
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

    protected void logD(String message) {
        Log.d(TAG, message);
    }

    protected void logI(String message) {
        Log.i(TAG, message);
    }

    protected void logE(String message) {
        Log.e(TAG, message);
    }
}
