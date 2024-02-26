package io.agora.cloudgame.context;

import io.agora.cloudgame.network.model.GameEntity;
import io.agora.cloudgame.network.model.RtcConfig;

public class GameDataContext {
    private volatile static GameDataContext instance = null;

    private GameEntity mGameEntity;
    private String mRoomId;
    private String mOpenId;
    private String mNickName;
    private String mAvatarUrl;
    private int mUid;
    private String mChannelName;
    private RtcConfig mRtcConfig;
    private String mTaskId;

    private GameDataContext() {
        init();
    }

    private void init() {
        mGameEntity = null;
        mRoomId = "";
        mOpenId = "";
        mNickName = "";
        mAvatarUrl = "https://agora-video-call.oss-cn-shanghai.aliyuncs.com/aigc/avatar.png";
        mUid = 0;
        mChannelName = "";
        mRtcConfig = null;
        mTaskId = "";
    }

    public static GameDataContext getInstance() {
        if (instance == null) {
            synchronized (GameDataContext.class) {
                if (instance == null) {
                    instance = new GameDataContext();
                }
            }
        }
        return instance;
    }


    public void reset() {
        init();
    }

    public GameEntity getGameEntity() {
        return mGameEntity;
    }

    public void setGameEntity(GameEntity gameEntity) {
        mGameEntity = gameEntity;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String roomId) {
        mRoomId = roomId;
    }

    public String getOpenId() {
        return mOpenId;
    }

    public void setOpenId(String openId) {
        mOpenId = openId;
    }

    public String getNickName() {
        return mNickName;
    }

    public void setNickName(String nickName) {
        mNickName = nickName;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        mAvatarUrl = avatarUrl;
    }

    public int getUid() {
        return mUid;
    }

    public void setUid(int uid) {
        mUid = uid;
    }

    public String getChannelName() {
        return mChannelName;
    }

    public void setChannelName(String channelName) {
        mChannelName = channelName;
    }

    public RtcConfig getRtcConfig() {
        return mRtcConfig;
    }

    public void setRtcConfig(RtcConfig rtcConfig) {
        mRtcConfig = rtcConfig;
    }

    public String getTaskId() {
        return mTaskId;
    }

    public void setTaskId(String taskId) {
        mTaskId = taskId;
    }
}
