package io.agora.cloudgame.network.model;

import io.agora.cloudgame.model.JsonModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MessageEntity implements JsonModel {

    @Expose
    @SerializedName("msg_id")
    public String msgId;

    @Expose
    @SerializedName("open_id")
    public String openId;

    @Expose
    @SerializedName("avatar")
    public String avatar;

    @Expose
    @SerializedName("nickname")
    public String nickname;

    @Expose
    @SerializedName("content")
    public String content;

    @Expose
    @SerializedName("gift_id")
    public String giftId;

    @Expose
    @SerializedName("gift_num")
    public int giftNum;

    @Expose
    @SerializedName("like_num")
    public int likeNum;

    @Expose
    @SerializedName("gift_value")
    public int giftValue;

    @Expose
    @SerializedName("timestamp")
    public Long timestamp;

}
