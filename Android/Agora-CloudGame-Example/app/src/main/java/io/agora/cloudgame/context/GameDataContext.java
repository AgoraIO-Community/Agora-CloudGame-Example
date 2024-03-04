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
    private int mBroadcastUid;
    private int mAgentUid;
    private int mAudienceUid;
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
        mBroadcastUid = 0;
        mAgentUid = 0;
        mAudienceUid = 0;
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

    public int getBroadcastUid() {
        return mBroadcastUid;
    }

    public void setBroadcastUid(int broadcastUid) {
        this.mBroadcastUid = broadcastUid;
        mAgentUid = broadcastUid + 1;
    }

    public int getAgentUid() {
        return mAgentUid;
    }

    public void setAgentUid(int agentUid) {
        this.mAgentUid = agentUid;
    }

    public int getAudienceUid() {
        return mAudienceUid;
    }

    public void setAudienceUid(int audienceUid) {
        this.mAudienceUid = audienceUid;
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
