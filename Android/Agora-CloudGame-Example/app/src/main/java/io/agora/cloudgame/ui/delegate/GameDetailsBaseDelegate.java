package io.agora.cloudgame.ui.delegate;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import agora.pb.rctrl.RemoteCtrlMsg;
import io.agora.base.VideoFrame;
import io.agora.cloudgame.constants.Constants;
import io.agora.cloudgame.context.GameDataContext;
import io.agora.cloudgame.example.BuildConfig;
import io.agora.cloudgame.example.R;
import io.agora.cloudgame.network.RareBackend;
import io.agora.cloudgame.network.model.ApiResult;
import io.agora.cloudgame.network.model.GameDetailResult;
import io.agora.cloudgame.network.model.GiftEntity;
import io.agora.cloudgame.network.model.MessageEntity;
import io.agora.cloudgame.network.model.RtcConfig;
import io.agora.cloudgame.network.model.SendMessageBody;
import io.agora.cloudgame.network.model.StartGameBody;
import io.agora.cloudgame.network.model.StartGameResult;
import io.agora.cloudgame.network.model.StopGameBody;
import io.agora.cloudgame.ui.widget.SoftKeyboardStateWatcher;
import io.agora.cloudgame.ui.widget.ViewJudge;
import io.agora.cloudgame.utils.DialogUtils;
import io.agora.cloudgame.utils.KeyCenter;
import io.agora.cloudgame.utils.YuvDumper;
import me.add1.iris.ApiRequestException;
import me.add1.iris.PageDelegate;
import me.add1.iris.utilities.ThreadUtils;

public class GameDetailsBaseDelegate extends PageDelegate {
    protected static String TAG = Constants.TAG + "-GameDetailsBaseDelegate";
    protected final static boolean ENABLE_DUMP_REMOTE_VIDEO_FRAME = false;

    protected final static int MAX_DUMP_VIDEO_FRAME_COUNT = 80;

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
    protected TextView mFrameRateTv;
    protected TextView mDumpVideoFrameTv;

    protected boolean mIsNativeRtc;
    protected long mCurrentTimeInSeconds;
    protected int mVideoFrameRate;
    protected YuvDumper mYuvDumper;
    protected int mDumperVideoFrameCount;
    protected boolean mEnableDumperVideoFrame;


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

    public GameDetailsBaseDelegate(boolean live) {
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

        if (ENABLE_DUMP_REMOTE_VIDEO_FRAME) {
            mYuvDumper = new YuvDumper(getContext(), "video_frame", new YuvDumper.DumperCallback() {
                @Override
                public void onDumpSuccess(String filePath) {
                    logI("yuv DumpSuccess:" + filePath);
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToastLong("yuv Dump成功:" + filePath);
                            if (null != mDumpVideoFrameTv) {
                                mDumpVideoFrameTv.setEnabled(true);
                            }
                        }
                    });
                }
            });
        }
        mEnableDumperVideoFrame = false;


        GameDataContext.getInstance().setOpenId(appId + "_" + (isLiveRole ? GameDataContext.getInstance().getBroadcastUid() : GameDataContext.getInstance().getAudienceUid()));
        GameDataContext.getInstance().setNickName("test_" + (isLiveRole ? GameDataContext.getInstance().getBroadcastUid() : GameDataContext.getInstance().getAudienceUid()));

        RtcConfig rtcConfig = new RtcConfig();
        rtcConfig.broadcastUid = GameDataContext.getInstance().getBroadcastUid();
        rtcConfig.uid = GameDataContext.getInstance().getAgentUid();
        rtcConfig.token = KeyCenter.getRtcToken(GameDataContext.getInstance().getChannelName(), rtcConfig.uid);
        rtcConfig.channelName = GameDataContext.getInstance().getChannelName();
        logI("rtcConfig:" + rtcConfig);
        GameDataContext.getInstance().setRtcConfig(rtcConfig);
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
        nameBuilder.append(GameDataContext.getInstance().getGameEntity().name);
        nameBuilder.append("\n");
        nameBuilder.append("uid: ");
        nameBuilder.append(isLiveRole ? GameDataContext.getInstance().getBroadcastUid() : GameDataContext.getInstance().getAudienceUid());
        nameBuilder.append(isLiveRole ? "(主播)" : "(观众)");
        nameBuilder.append("\n");
        nameBuilder.append("房间号：");
        nameBuilder.append(GameDataContext.getInstance().getChannelName());
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

        if (!ENABLE_DUMP_REMOTE_VIDEO_FRAME) {
            if (null != mDumpVideoFrameTv) {
                mDumpVideoFrameTv.setVisibility(View.GONE);
            }
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
                    logI("giftId:" + t.id + ",price:" + t.price + ",giftNum:" + t.giftNum);
                    RareBackend.getInstance().sendGiftV2(appId, GameDataContext.getInstance().getRoomId(), GameDataContext.getInstance().getGameEntity().gameId,
                            getSendMessageBody(entity),
                            new RareBackend.ApiRequestCallback<Boolean>() {
                                @Override
                                public void onSucceed(@NonNull ApiResult<Boolean> t) {
                                    ThreadUtils.postOnUiThread(() -> {
                                        showToast("赠送礼物成功");
                                    });
                                }

                                @Override
                                public void onFailure(@NonNull ApiRequestException e) {
                                    ThreadUtils.postOnUiThread(() -> {
                                        showToast("赠送礼物失败：" + e.code + "," + e.message);
                                    });
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
                RareBackend.getInstance().gameLikeV2(appId, GameDataContext.getInstance().getRoomId(), GameDataContext.getInstance().getGameEntity().gameId,
                        getSendMessageBody(entity)
                        , new RareBackend.ApiRequestCallback<Boolean>() {
                            @Override
                            public void onSucceed(@NonNull ApiResult<Boolean> t) {
                                ThreadUtils.postOnUiThread(() -> {
                                });
                            }

                            @Override
                            public void onFailure(@NonNull ApiRequestException e) {
                                ThreadUtils.postOnUiThread(() -> {
                                    showToast("发送点赞失败：" + e.code + "," + e.message);
                                });
                            }
                        });
            });
        }

        if (null != mChatRightView && null != mChatInputEditText) {
            mChatRightView.setOnClickListener(v -> {
                MessageEntity entity = new MessageEntity();
                entity.content = mChatInputEditText.getText().toString();
                ViewJudge.INSTANCE.hideKeyboard(Objects.requireNonNull(getActivity()));
                RareBackend.getInstance().gameCommentV2(appId, GameDataContext.getInstance().getRoomId(), GameDataContext.getInstance().getGameEntity().gameId,
                        getSendMessageBody(entity), new RareBackend.ApiRequestCallback<Boolean>() {
                            @Override
                            public void onSucceed(@NonNull ApiResult<Boolean> t) {
                                ThreadUtils.postOnUiThread(() -> {
                                    showToast("发送评论成功");
                                });
                            }

                            @Override
                            public void onFailure(@NonNull ApiRequestException e) {
                                ThreadUtils.postOnUiThread(() -> {
                                    showToast("发送评论失败：" + e.code + "," + e.message);
                                });
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

        if (null != mDumpVideoFrameTv) {
            mDumpVideoFrameTv.setOnClickListener(v -> {
                showToastLong("Dump YUV中，请稍等...");
                if (null != mYuvDumper) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
                    String time = format.format(System.currentTimeMillis());

                    mYuvDumper.updateFileName("video_frame_" + time);
                }
                mEnableDumperVideoFrame = true;
                mDumpVideoFrameTv.setEnabled(false);
            });
        }
    }

    private void initGameDetails() {
        RareBackend.getInstance().gamesDetails(appId, GameDataContext.getInstance().getGameEntity().gameId, new RareBackend.ApiRequestCallback<GameDetailResult>() {
            @Override
            public void onSucceed(@NonNull ApiResult<GameDetailResult> t1) {
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
        logD("startGame");
        if (isLiveRole) {
            StartGameBody body = new StartGameBody();
            body.openId = GameDataContext.getInstance().getOpenId();
            body.nickname = GameDataContext.getInstance().getNickName();
            body.avatarUrl = GameDataContext.getInstance().getAvatarUrl();
            body.rtcConfig = GameDataContext.getInstance().getRtcConfig();

            RareBackend.getInstance().startGame(appId, GameDataContext.getInstance().getGameEntity().gameId, GameDataContext.getInstance().getRoomId(), body, new RareBackend.ApiRequestCallback<StartGameResult>() {
                @Override
                public void onSucceed(@NonNull ApiResult<StartGameResult> t) {
                    logD("startGame onSucceed");
                    if (!TextUtils.isEmpty(t.data.taskId)) {
                        GameDataContext.getInstance().setTaskId(t.data.taskId);
                        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                            @Override
                            public void run() {
                                RareBackend.getInstance().gameState(appId, GameDataContext.getInstance().getTaskId(),
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
            StopGameBody body = new StopGameBody();
            body.openId = GameDataContext.getInstance().getOpenId();
            body.taskId = GameDataContext.getInstance().getTaskId();
            RareBackend.getInstance().stopGame(appId, GameDataContext.getInstance().getGameEntity().gameId, GameDataContext.getInstance().getRoomId(), body, new RareBackend.ApiRequestCallback<Boolean>() {
                @Override
                public void onSucceed(@NonNull ApiResult<Boolean> t) {
                    logD("stopGame onSucceed");
                }

                @Override
                public void onFailure(@NonNull ApiRequestException e) {
                    logE("stopGame onFailure:" + e.message);
                    if (isAlive() && !TextUtils.isEmpty(e.message)) {
                        showToast(e.code + " " + e.message);
                    }
                }
            });
        }

        if (null != mScheduledExecutorService) {
            mScheduledExecutorService.shutdown();
        }

        if (null != mYuvDumper) {
            mYuvDumper.clearFrames();
        }
    }

    protected SendMessageBody getSendMessageBody(MessageEntity message) {
        List<MessageEntity> list = new ArrayList<>();
        MessageEntity sendMessageEntity = new MessageEntity();
        sendMessageEntity.msgId = KeyCenter.APP_ID + "_" + System.currentTimeMillis();
        sendMessageEntity.openId = GameDataContext.getInstance().getOpenId();
        sendMessageEntity.content = message.content;
        sendMessageEntity.giftId = message.giftId;
        sendMessageEntity.giftNum = message.giftNum;
        sendMessageEntity.giftValue = message.giftValue;
        sendMessageEntity.likeNum = message.likeNum;
        sendMessageEntity.avatarUrl = GameDataContext.getInstance().getAvatarUrl();
        sendMessageEntity.nickname = GameDataContext.getInstance().getNickName();
        sendMessageEntity.timestamp = System.currentTimeMillis();
        list.add(sendMessageEntity);

        SendMessageBody send = new SendMessageBody();
        send.payload = JSON.toJSONString(list);

        return send;
    }

    protected void handleOnRenderVideoFrame(int uid, VideoFrame videoFrame) {
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        if (currentTimeInSeconds != mCurrentTimeInSeconds) {
            mCurrentTimeInSeconds = currentTimeInSeconds;
            final int frameRate = mVideoFrameRate;
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (null != mFrameRateTv) {
                        mFrameRateTv.setText(String.valueOf(frameRate));
                    }
                }
            });
            mVideoFrameRate = 0;
        } else {
            mVideoFrameRate++;
        }
        if (mEnableDumperVideoFrame) {
            if (mDumperVideoFrameCount <= MAX_DUMP_VIDEO_FRAME_COUNT) {
                if (null != mYuvDumper) {
                    mYuvDumper.pushFrame(videoFrame);
                    mDumperVideoFrameCount++;
                }
            } else {
                if (null != mYuvDumper) {
                    mYuvDumper.saveToFile();
                    mDumperVideoFrameCount = 0;
                }
                mEnableDumperVideoFrame = false;
            }
        }
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
