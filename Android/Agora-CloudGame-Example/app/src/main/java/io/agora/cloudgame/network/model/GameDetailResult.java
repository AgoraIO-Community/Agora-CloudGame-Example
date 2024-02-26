package io.agora.cloudgame.network.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.agora.cloudgame.model.JsonModel;

public class GameDetailResult implements JsonModel {

    @Expose
    @SerializedName("game_id")
    public String gameId;

    @Expose
    @SerializedName("name")
    public String name;

    @Expose
    @SerializedName("vendor")
    public String vendor;

    @Expose
    @SerializedName("thumbnail")
    public String thumbnail;

    @Expose
    @SerializedName("introduce")
    public String introduce;

    @Expose
    @SerializedName("feature")
    public FeatureEntity feature;

    @Expose
    @SerializedName("gifts")
    public List<GiftEntity> gifts;

    @SerializedName("instruct")
    public List<String> instruct;

    @NonNull
    @Override
    public String toString() {
        return "GameDetailEntity{" +
                "gameId='" + gameId + '\'' +
                ", name='" + name + '\'' +
                ", vendor='" + vendor + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", introduce='" + introduce + '\'' +
                ", feature=" + feature +
                ", gifts=" + gifts +
                ", instruct=" + instruct +
                '}';
    }
}
