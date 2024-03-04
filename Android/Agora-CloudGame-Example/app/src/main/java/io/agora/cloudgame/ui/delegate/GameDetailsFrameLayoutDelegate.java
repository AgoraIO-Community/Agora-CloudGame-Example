package io.agora.cloudgame.ui.delegate;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import java.util.Objects;

import io.agora.base.VideoFrame;
import io.agora.cloudgame.AppContext;
import io.agora.cloudgame.context.GameDataContext;
import io.agora.cloudgame.example.databinding.GameDetailFramelayoutBinding;
import io.agora.cloudgame.ui.widget.VideoTextureView;
import io.agora.cloudgame.utils.KeyCenter;
import io.agora.cloudgame.utils.ViewUtils;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.ClientRoleOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.DataStreamConfig;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.IVideoFrameObserver;
import io.agora.rtc2.video.VideoCanvas;
import me.add1.iris.utilities.ThreadUtils;

public class GameDetailsFrameLayoutDelegate extends GameDetailsBaseDelegate {
    private GameDetailFramelayoutBinding mBinding;
    private RtcEngine mRtcEngine;
    private int mStreamId;
    private VideoTextureView mVideoView;
    private ChannelMediaOptions mJoinChannelOptions;

    public GameDetailsFrameLayoutDelegate(boolean live) {
        super(live);
    }

    @Override
    protected View getRootView(LayoutInflater inflater, ViewGroup container) {
        mBinding = GameDetailFramelayoutBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    protected void initData() {
        TAG = io.agora.cloudgame.constants.Constants.TAG + "-" + GameDetailsFrameLayoutDelegate.class.getSimpleName();
        super.initData();

        mJoinChannelOptions = new ChannelMediaOptions();
        mJoinChannelOptions.clientRoleType = isLiveRole ? Constants.CLIENT_ROLE_BROADCASTER :
                Constants.CLIENT_ROLE_AUDIENCE;
        mJoinChannelOptions.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        mJoinChannelOptions.autoSubscribeVideo = true;
        mJoinChannelOptions.autoSubscribeAudio = true;
        mJoinChannelOptions.publishCameraTrack = false;
    }

    @Override
    protected void initRtc() {
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

            DataStreamConfig dataStreamConfig = new DataStreamConfig();
            dataStreamConfig.ordered = true;
            dataStreamConfig.syncWithAudio = true;
            mStreamId = mRtcEngine.createDataStream(dataStreamConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        mGameViewLayout = mBinding.frameLayout;
        mGameStateTv = mBinding.gameStateTv;
        mFrameRateTv = mBinding.frameRateTv;
        mDumpVideoFrameTv = mBinding.dumpVideoFrameView;
        super.initView();
    }

    @Override
    protected void onGameStateChange(String state) {
        super.onGameStateChange(state);
        if ("started".equals(state)) {
            if (!isJoinChannel) {
                isJoinChannel = true;
                if (isLiveRole && null != mRtcEngine) {
                    mRtcEngine.registerVideoFrameObserver(iRtcVideoFrameObserver);
                    mRtcEngine.joinChannel(KeyCenter.getRtcToken(GameDataContext.getInstance().getRtcConfig().channelName, GameDataContext.getInstance().getRtcConfig().broadcastUid), GameDataContext.getInstance().getRtcConfig().channelName,
                            GameDataContext.getInstance().getRtcConfig().broadcastUid, mJoinChannelOptions);
                }
            }
        }
    }

    @Override
    protected void startGameForAudience() {
        super.startGameForAudience();
        if (!isJoinChannel) {
            isJoinChannel = true;
            mRtcEngine.registerVideoFrameObserver(iRtcVideoFrameObserver);
            logD("startGameForAudience uid:" + GameDataContext.getInstance().getAudienceUid() + " channelName:" + GameDataContext.getInstance().getRtcConfig().channelName);
            int ret = mRtcEngine.joinChannel(KeyCenter.getRtcToken(GameDataContext.getInstance().getRtcConfig().channelName, GameDataContext.getInstance().getAudienceUid()), GameDataContext.getInstance().getRtcConfig().channelName, GameDataContext.getInstance().getAudienceUid(), mJoinChannelOptions);
            logI("startGameForAudience->ret:" + ret);
        }
    }


    @Override
    protected void sendStreamMessage(byte[] data) {
        super.sendStreamMessage(data);
        int ret = mRtcEngine.sendStreamMessage(mStreamId, data);
        Log.i(TAG, "sendEventMessages:ret:" + ret);
        if (0 == ret) {
            mEventMessagelist.clear();
        } else {
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast("控制事件发送失败(sendStreamMessage)！");
                }
            });
        }
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
            logD("onLeaveChannel->" + stats);
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RtcEngine.destroy();
                    mRtcEngine = null;
                }
            });
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            logD("onJoinChannelSuccess->" + uid + ", channel->" + channel + ", elapsed->" + elapsed);
            DataStreamConfig config = new DataStreamConfig();
            config.ordered = true;
            config.syncWithAudio = true;
            mStreamId = mRtcEngine.createDataStream(config);
            Log.i(TAG, "createDataStream->mStreamId" + mStreamId);
        }

        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed);
            Log.i(TAG, "onRemoteVideoStateChanged->" + uid + ", state->" + state + ", reason->" + reason);
            if (Constants.REMOTE_VIDEO_STATE_STARTING == state) {
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
            } else if (Constants.REMOTE_VIDEO_STATE_STOPPED == state || Constants.REMOTE_VIDEO_STATE_FAILED == state) {
                ThreadUtils.postOnUiThread(() -> {
                    mBinding.frameLayout.removeAllViews();
                    mVideoView = null;
                });
            }
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
            if (null != mVideoView && null != GameDetailsFrameLayoutDelegate.this.getContext()) {
                ThreadUtils.postOnUiThread(() -> {
                    final int gameLayoutWidth = mBinding.gameLayout.getMeasuredWidth();
                    final int gameLayoutHeight = mBinding.gameLayout.getMeasuredHeight();
                    Log.i(TAG, "onVideoSizeChanged->gameLayoutWidth:" + gameLayoutWidth + ",gameLayoutHeight:" + gameLayoutHeight);
                    int targetWidth = gameLayoutWidth;
                    int targetHeight;
                    float scale = (float) targetWidth / width;
                    targetHeight = (int) (height * scale);

                    final int topMargin = gameLayoutHeight - targetHeight;

                    Log.i(TAG, "onVideoSizeChanged->targetWidth:" + targetWidth + ",targetHeight:" + targetHeight + ",topMargin:" + topMargin);

                    mVideoView.setVideoSize(targetWidth, targetHeight);
                    mVideoView.post(new Runnable() {
                        @Override
                        public void run() {
                            ViewGroup.LayoutParams layoutParams = mBinding.frameLayout.getLayoutParams();
                            layoutParams.width = targetWidth;
                            layoutParams.height = targetHeight;
                            if (topMargin < 0) {
                                ((LinearLayout.LayoutParams) layoutParams).topMargin = topMargin;
                            }
                            mBinding.frameLayout.setLayoutParams(layoutParams);

                            if (topMargin > 10) {
                                ViewGroup.LayoutParams operatorLayoutParams = mBinding.operatorLayout.getLayoutParams();
                                operatorLayoutParams.height = topMargin - ViewUtils.dp2px(GameDetailsFrameLayoutDelegate.this.getContext(), 10);
                                ((CoordinatorLayout.LayoutParams) operatorLayoutParams).bottomMargin = 0;
                                mBinding.operatorLayout.setLayoutParams(operatorLayoutParams);
                            }
                        }
                    });
                });
            }

        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            Log.i(TAG, "onUserJoined->" + uid + ", elapsed->" + elapsed);

/*            if (uid != GameDataContext.getInstance().getAgentUid()) {
                return;
            }*/
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

    private final IVideoFrameObserver iRtcVideoFrameObserver = new IVideoFrameObserver() {

        @Override
        public boolean onCaptureVideoFrame(int sourceType, VideoFrame videoFrame) {
            return false;
        }

        @Override
        public boolean onPreEncodeVideoFrame(int sourceType, VideoFrame videoFrame) {
            return false;
        }

        @Override
        public boolean onMediaPlayerVideoFrame(VideoFrame videoFrame, int mediaPlayerId) {
            return false;
        }

        @Override
        public boolean onRenderVideoFrame(String channelId, int uid, VideoFrame videoFrame) {
            //logD("onRenderVideoFrame->channelId:" + channelId + ",uid:" + uid);
            handleOnRenderVideoFrame(uid, videoFrame);
            return false;
        }

        @Override
        public int getVideoFrameProcessMode() {
            return 0;
        }

        @Override
        public int getVideoFormatPreference() {
            return 0;
        }

        @Override
        public boolean getRotationApplied() {
            return false;
        }

        @Override
        public boolean getMirrorApplied() {
            return false;
        }

        @Override
        public int getObservedFramePosition() {
            return 0;
        }
    };

    @Override
    protected void onDestroyView() {
        super.onDestroyView();
        if (mRtcEngine != null && isJoinChannel) {
            // 停止本地视频预览
            mRtcEngine.stopPreview();
            mRtcEngine.registerVideoFrameObserver(null);
            // 离开频道
            mRtcEngine.leaveChannel();
            isJoinChannel = false;
        }
    }
}
