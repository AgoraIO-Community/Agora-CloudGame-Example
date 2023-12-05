package io.agora.cloudgame.network.model;

import io.agora.cloudgame.model.JsonModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GameEntity implements JsonModel {

    @Expose
    @SerializedName("id")
    public Long id;

    @Expose
    @SerializedName("vid")
    public String vid;

    @Expose
    @SerializedName("uid")
    public int uid;

    @Expose
    @SerializedName("room_id")
    public String roomId;

    @Expose
    @SerializedName("open_id")
    public String openId;

    @Expose
    @SerializedName("task_id")
    public String taskId;

    @Expose
    @SerializedName("nickname")
    public String nickname;

    @Expose
    @SerializedName("avatar")
    public String avatar;

    @Expose
    @SerializedName("rtc_config")
    public RtcConfig rtcConfig;

    @Expose
    @SerializedName("game_id")
    public String gameId;

    @Expose
    @SerializedName("introduce")
    public String brief;

    @Expose
    @SerializedName("name")
    public String name;

    @Expose
    @SerializedName("channel_name")
    public String channelName;


}
