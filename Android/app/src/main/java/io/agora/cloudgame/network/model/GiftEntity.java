package io.agora.cloudgame.network.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.agora.cloudgame.model.JsonModel;

public class GiftEntity implements JsonModel {

    @Expose
    @SerializedName("name")
    public String name;

    @Expose
    @SerializedName("id")
    public String id;

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
    @SerializedName("gift_id")
    public String giftId;

    @Expose
    @SerializedName("gift_num")
    public int giftNum;

    @Expose
    @SerializedName("value")
    public int value;

    @Expose
    @SerializedName("gift_value")
    public int giftValue;

    @Expose
    @SerializedName("vendor_gift_id")
    public String vendorGiftId;

    @Expose
    @SerializedName("timestamp")
    public Long timestamp;

    @Expose
    @SerializedName("game_id")
    public String gameId;

    @Expose
    @SerializedName("thumbnail")
    public String thumbnail;

    @Expose
    @SerializedName("smallPath")
    public String smallPath;

    @Expose
    @SerializedName("price")
    public int price;

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
