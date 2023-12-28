package io.agora.cloudgame.network.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.gson.annotations.Expose;

import java.io.Serializable;

import io.agora.cloudgame.model.JsonModel;

public class MessageEntityV2 implements JsonModel, Serializable {

    @Expose
    @JSONField(name = "msg_id")
    public String msgId;

    @Expose
    @JSONField(name = "openid")
    public String openId;

    @Expose
    @JSONField(name = "avatar_url")
    public String avatar;

    @Expose
    @JSONField(name = "nickname")
    public String nickname;

    @Expose
    @JSONField(name = "content")
    public String content;

    @Expose
    @JSONField(name = "gift_id")
    public String giftId;

    @Expose
    @JSONField(name = "gift_num")
    public int giftNum;

    @Expose
    @JSONField(name = "like_num")
    public int likeNum;

    @Expose
    @JSONField(name = "gift_value")
    public int giftValue;

    @Expose
    @JSONField(name = "timestamp")
    public Long timestamp;

}
