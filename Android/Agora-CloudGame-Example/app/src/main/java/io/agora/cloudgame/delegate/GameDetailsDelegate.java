package io.agora.cloudgame.delegate;

import android.Manifest;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;
import com.gyf.immersionbar.ImmersionBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import agora.pb.rctrl.RemoteCtrlMsg;
import io.agora.cloudgame.AppContext;
import io.agora.cloudgame.KeyCenter;
import io.agora.cloudgame.RareBackend;
import io.agora.cloudgame.Store;
import io.agora.cloudgame.example.BuildConfig;
import io.agora.cloudgame.example.R;
import io.agora.cloudgame.example.databinding.DelegateHostBinding;
import io.agora.cloudgame.network.model.ApiResult;
import io.agora.cloudgame.network.model.GameEntity;
import io.agora.cloudgame.network.model.GameResult;
import io.agora.cloudgame.network.model.GiftEntity;
import io.agora.cloudgame.network.model.MessageEntity;
import io.agora.cloudgame.network.model.RtcConfig;
import io.agora.cloudgame.network.model.SendMessage;
import io.agora.cloudgame.utilities.DialogUtils;
import io.agora.cloudgame.widget.SoftKeyboardStateWatcher;
import io.agora.cloudgame.widget.VideoTextureView;
import io.agora.cloudgame.widget.ViewJudge;
import io.agora.cloudgame.widget.ViewUtils;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.ClientRoleOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.DataStreamConfig;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;
import me.add1.iris.ApiRequestException;
import me.add1.iris.PageDelegate;
import me.add1.iris.utilities.ThreadUtils;

public class GameDetailsDelegate extends PageDelegate {

    private static final String TAG = io.agora.cloudgame.constants.Constants.TAG + "-" + GameDetailsDelegate.class.getSimpleName();

    protected boolean onStatusBarDarkFont() {
        return true;
    }

    protected int onStatusBarColor() {
        return R.color.white;
    }

    protected boolean immerse() {
        return true;
    }


    private DelegateHostBinding mBinding;

    private RtcEngine mRtcEngine;

    private final String appId = BuildConfig.APP_ID;

    private int mStreamId;

    private ArrayList<GiftEntity> mGifts;

    private VideoTextureView mVideoView;

    private final GameEntity mGameEntity;
    private ChannelMediaOptions mJoinChannelOptions;

    private final boolean isLiveRole;
    private boolean isJoinChannel;

    private Timer mTimer;

    private final List<RemoteCtrlMsg.RctrlMsg> mEventMessagelist;
    private long mLastSendEventMessageTime;
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

    public GameDetailsDelegate(GameEntity entity, boolean live) {
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

        mBinding = DelegateHostBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    protected void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        processPermission();
        initView();
    }

    private void processPermission() {
        String[] perms = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA};
        if (PermissionsUtil.hasPermission(getContext(),
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA)) {
            init();
        } else {
            PermissionsUtil.requestPermission(getActivity(), new PermissionListener() {
                @Override
                public void permissionGranted(@NonNull String[] permissions) {
                    init();
                }

                @Override
                public void permissionDenied(@NonNull String[] permissions) {

                }
            }, perms);
        }
    }

    private void init() {
        initRtc();
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

        LinearLayout.LayoutParams layoutParams =
                (LinearLayout.LayoutParams) mBinding.toolbarView.getLayoutParams();
        layoutParams.height = ViewUtils.getStatusBarHeight(getContext());
        mBinding.toolbarView.setLayoutParams(layoutParams);

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
    }

    private void initRtc() {
        RtcEngineConfig config = new RtcEngineConfig();
        config.mContext = getContext().getApplicationContext();
        config.mAppId = appId;
        config.mEventHandler = iRtcEngineEventHandler;
        /* Sets the channel profile of the Agora RtcEngine. */
        config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        config.mAudioScenario = Constants.AudioScenario.getValue(Constants.AudioScenario.DEFAULT);
        config.mAreaCode = AppContext.Companion.get().getGlobalSettings().getAreaCode();

        try {
            mRtcEngine = RtcEngine.create(config);
            mRtcEngine.enableVideo();
            mRtcEngine.setParameters("{"
                    + "\"rtc.report_app_scenario\":"
                    + "{"
                    + "\"appScenario\":" + 100 + ","
                    + "\"serviceType\":" + 11 + ","
                    + "\"appVersion\":\"" + RtcEngine.getSdkVersion() + "\""
                    + "}"
                    + "}");
            mRtcEngine.setLocalAccessPoint(AppContext.Companion.get().getGlobalSettings().getPrivateCloudConfig());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    boolean isJoin = false;

    private void initData() {
        Random random = new Random();

        mGameEntity.openId = isLiveRole ? "abcd" : KeyCenter.getUserUid() + "";
        mGameEntity.nickname = "00";
        mGameEntity.avatar = "./avatar.png";

        RtcConfig rtcConfig = new RtcConfig();
        rtcConfig.broadcastUid = KeyCenter.getBroadcastUid();
        rtcConfig.uid = mGameEntity.uid;
        rtcConfig.token = KeyCenter.getRtcToken(mGameEntity.roomId, rtcConfig.uid);
        rtcConfig.channelName = mGameEntity.roomId + "";
        mGameEntity.rtcConfig = rtcConfig;
        Log.i(TAG, "rtcConfig:" + rtcConfig);

        mJoinChannelOptions = new ChannelMediaOptions();
        mJoinChannelOptions.clientRoleType = isLiveRole ? Constants.CLIENT_ROLE_BROADCASTER :
                Constants.CLIENT_ROLE_AUDIENCE;
        mJoinChannelOptions.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        mJoinChannelOptions.autoSubscribeVideo = true;
        mJoinChannelOptions.autoSubscribeAudio = true;
        mJoinChannelOptions.publishCameraTrack = false;

        DataStreamConfig config = new DataStreamConfig();
        config.ordered = true;
        config.syncWithAudio = true;
        mStreamId = mRtcEngine.createDataStream(config);

        GameEntity gameEntity = RareBackend.getInstance().getStore().getModel(Store.Pref.ACCOUNT,
                Store.KEY_GAME_DATA, GameEntity.class);


        if (isLiveRole) {
            RareBackend.getInstance().startGame(appId, mGameEntity.gameId, mGameEntity, new RareBackend.ApiRequestCallback<GameResult>() {
                @Override
                public void onSucceed(@NonNull ApiResult<GameResult> t) {
                    if (!TextUtils.isEmpty(t.data.taskId)) {
                        mGameEntity.taskId = t.data.taskId;
                        mTimer = new Timer();

                        mTimer.scheduleAtFixedRate(timerTask, 0, 1000);

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
            mRtcEngine.joinChannel(KeyCenter.getRtcToken(rtcConfig.channelName, KeyCenter.getUserUid()), rtcConfig.channelName, KeyCenter.getUserUid(), mJoinChannelOptions);
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
                        entity.giftId = t.vendorGiftId;
                        entity.giftValue = t.value;
                        entity.giftNum = t.giftNum;
                        Log.i(TAG, "giftId:" + t.vendorGiftId + ",giftValue:" + t.value + ",giftNum:" + t.giftNum);
                        RareBackend.getInstance().sendGift(appId, mGameEntity.gameId,
                                getSendOperator(entity),
                                new RareBackend.ApiRequestCallback<Boolean>() {
                                    @Override
                                    public void onSucceed(@NonNull ApiResult<Boolean> t) {
                                        ThreadUtils.postOnUiThread(() -> {
                                            showToast("赠送礼物成功");
                                        });
                                    }

                                    @Override
                                    public void onFailure(@NonNull ApiRequestException e) {
                                        showToast("赠送礼物失败");
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
                    RareBackend.getInstance().gameLike(appId, mGameEntity.gameId,
                            getSendOperator(entity)
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
            RareBackend.getInstance().gameComment(appId, mGameEntity.gameId,
                    getSendOperator(entity), new RareBackend.ApiRequestCallback<Boolean>() {
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

        mBinding.frameLayout.setOnTouchListener((view, event) -> {
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
        Log.i(TAG, "sendMouseMessage:event x/width:" + event.getX() / mBinding.frameLayout.getMeasuredWidth() + ",event y/height:" + event.getY() / mBinding.frameLayout.getMeasuredHeight());

        int x = ((int) (event.getX()) << 16) / mBinding.frameLayout.getMeasuredWidth();
        int y = ((int) (event.getY()) << 16) / mBinding.frameLayout.getMeasuredHeight();


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

        mRtcEngine.sendStreamMessage(mStreamId, rctrlMsges.toByteArray());
        mEventMessagelist.clear();
    }

    private final IRtcEngineEventHandler iRtcEngineEventHandler = new IRtcEngineEventHandler() {

        @Override
        public void onError(int err) {
            Log.e(TAG, String.format("onError code %d message %s", err,
                    RtcEngine.getErrorDescription(err)));
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            super.onLeaveChannel(stats);
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            DataStreamConfig config = new DataStreamConfig();
            config.ordered = true;
            config.syncWithAudio = true;
            mStreamId = mRtcEngine.createDataStream(config);
            Log.i(TAG, "onJoinChannelSuccess->" + uid + ", channel->" + channel + ", elapsed->" + elapsed + ",mStreamId" + mStreamId);
        }

        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed);
            Log.i(TAG, "onRemoteVideoStateChanged->" + uid + ", state->" + state + ", reason->" + reason);
        }

        @Override
        public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
            super.onFirstRemoteVideoFrame(uid, width, height, elapsed);
            Log.i(TAG, "onFirstRemoteVideoFrame->" + uid + ", width->" + width + ", height->" + height);
        }

        @Override
        public void onVideoSizeChanged(Constants.VideoSourceType source, int uid, int width,
                                       int height, int rotation) {
            super.onVideoSizeChanged(source, uid, width, height, rotation);
            Log.i(TAG, "onVideoSizeChanged->" + uid + ", width->" + width + ", height->" + height);
            if (null != mVideoView) {
                ThreadUtils.postOnUiThread(() -> {
                    final int rootViewWidth = mBinding.rootView.getMeasuredWidth();
                    final int rootViewHeight = mBinding.rootView.getMeasuredHeight();

                    Log.i(TAG, "onVideoSizeChanged->rootViewWidth:" + rootViewWidth + ",rootViewHeight:" + rootViewHeight);
                    int targetWidth;
                    int targetHeight;
                    if ((float) rootViewWidth / rootViewHeight > (float) width / height) {
                        targetHeight = rootViewHeight;
                        float scale = (float) targetHeight / height;
                        targetWidth = (int) (width * scale);
                    } else {
                        targetWidth = rootViewWidth;
                        float scale = (float) targetWidth / width;
                        targetHeight = (int) (height * scale);
                    }

                    Log.i(TAG, "onVideoSizeChanged->targetWidth:" + targetWidth + ",targetHeight:" + targetHeight);

                    mVideoView.setVideoSize(targetWidth, targetHeight);
                    mVideoView.post(new Runnable() {
                        @Override
                        public void run() {
                            ViewGroup.LayoutParams layoutParams = mBinding.frameLayout.getLayoutParams();
                            layoutParams.width = targetWidth;
                            layoutParams.height = targetHeight;
                            mBinding.frameLayout.setLayoutParams(layoutParams);
                        }
                    });
                });
            }

        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            Log.i(TAG, "onUserJoined->" + uid + ", elapsed->" + elapsed + ", elapsed->" + elapsed);

            if (uid != mGameEntity.rtcConfig.uid) {
                return;
            }

            ThreadUtils.postOnUiThread(() -> {
                mVideoView = new VideoTextureView(Objects.requireNonNull(AppContext.Companion.get()).getApplicationContext());

                mBinding.frameLayout.removeAllViews();
                mBinding.frameLayout.addView(mVideoView);
                try {
                    mRtcEngine.setupRemoteVideo(new VideoCanvas(mVideoView, Constants.RENDER_MODE_FIT, uid));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
        }


        @Override
        public void onClientRoleChanged(int oldRole, int newRole,
                                        ClientRoleOptions newRoleOptions) {
            super.onClientRoleChanged(oldRole, newRole, newRoleOptions);
            Log.i(TAG, String.format("client role changed from state %d to %d", oldRole, newRole));
        }

        @Override
        public void onStreamMessageError(int uid, int streamId, int error, int missed, int cached) {
            super.onStreamMessageError(uid, streamId, error, missed, cached);
            Log.i(TAG, "onStreamMessageError" + ", error" + error);
        }

    };

    TimerTask timerTask = new TimerTask() {
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
//                        //get game state
                        if (t1.data.status.equals("started")) {
                            if (!isJoinChannel) {
                                isJoinChannel = true;
                                if (isLiveRole && null != mRtcEngine) {
                                    mRtcEngine.joinChannel(KeyCenter.getRtcToken(mGameEntity.rtcConfig.channelName, mGameEntity.rtcConfig.broadcastUid), mGameEntity.rtcConfig.channelName,
                                            mGameEntity.rtcConfig.broadcastUid, mJoinChannelOptions);
                                }
                            }
                        } else if (t1.data.status.equals("schedule_failed")) {
                            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast("暂无可用的游戏资源，请稍后再试");
                                }
                            });
                        }
                    });
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
        if (mRtcEngine != null) {
            // 停止本地视频预览
            mRtcEngine.stopPreview();
            // 离开频道
            mRtcEngine.leaveChannel();
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
        mRtcEngine = null;
    }

    private SendMessage getSendOperator(MessageEntity message) {
        List<MessageEntity> list = new ArrayList<>();
        message.avatar = mGameEntity.avatar;
        message.msgId = System.currentTimeMillis() + "_123";
        message.avatar = mGameEntity.avatar;
        message.nickname = mGameEntity.nickname;
        message.openId = mGameEntity.openId;
        message.timestamp = System.currentTimeMillis();

        list.add(message);
        SendMessage send = new SendMessage();
        send.roomId = mGameEntity.roomId;
        send.payload = list;

        return send;
    }

}
