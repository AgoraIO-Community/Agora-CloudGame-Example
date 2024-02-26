package io.agora.cloudgame.network.model;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
    @JSONField(name = "price")
    public int price;

    @Expose
    @JSONField(name = "thumbnail")
    public String thumbnail;

    @Expose
    @JSONField(name = "game_id")
    @SerializedName("game_id")
    public String gameId;

    public int giftNum;

    public boolean isSelect;


    @NonNull
    @Override
    public String toString() {
        return "GiftEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", thumbnail='" + thumbnail + '\'' +
                ", gameId='" + gameId + '\'' +
                ", giftNum=" + giftNum +
                ", isSelect=" + isSelect +
                '}';
    }
}
