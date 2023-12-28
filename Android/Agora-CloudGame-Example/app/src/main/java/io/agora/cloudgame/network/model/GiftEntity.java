package io.agora.cloudgame.network.model;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.gson.annotations.Expose;

import java.io.Serializable;

import io.agora.cloudgame.model.JsonModel;

public class GiftEntity implements JsonModel, Serializable {

    @Expose
    @JSONField(name = "id")
    public String id;

    @Expose
    @JSONField(name = "name")
    public String name;

    @Expose
    @JSONField(name = "value")
    public int value;

    @Expose
    @JSONField(name = "price")
    public int price;

    @Expose
    @JSONField(name = "msg_id")
    public String msgId;

    @Expose
    @JSONField(name = "open_id")
    public String openId;

    @Expose
    @JSONField(name = "avatar")
    public String avatar;

    @Expose
    @JSONField(name = "nickname")
    public String nickname;

    @Expose
    @JSONField(name = "gift_id")
    public String giftId;

    @Expose
    @JSONField(name = "gift_num")
    public int giftNum;


    @Expose
    @JSONField(name = "gift_value")
    public int giftValue;

    @Expose
    @JSONField(name = "vendor_gift_id")
    public String vendorGiftId;

    @Expose
    @JSONField(name = "timestamp")
    public Long timestamp;

    @Expose
    @JSONField(name = "game_id")
    public String gameId;

    @Expose
    @JSONField(name = "thumbnail")
    public String thumbnail;

    @Expose
    @JSONField(name = "smallPath")
    public String smallPath;


    public boolean isSelect;


    @NonNull
    @Override
    public String toString() {
        return "GiftEntity{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", msgId='" + msgId + '\'' +
                ", openId='" + openId + '\'' +
                ", avatar='" + avatar + '\'' +
                ", nickname='" + nickname + '\'' +
                ", giftId='" + giftId + '\'' +
                ", giftNum=" + giftNum +
                ", value=" + value +
                ", giftValue=" + giftValue +
                ", vendorGiftId='" + vendorGiftId + '\'' +
                ", timestamp=" + timestamp +
                ", gameId='" + gameId + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", smallPath='" + smallPath + '\'' +
                ", price=" + price +
                ", isSelect=" + isSelect +
                '}';
    }
}
