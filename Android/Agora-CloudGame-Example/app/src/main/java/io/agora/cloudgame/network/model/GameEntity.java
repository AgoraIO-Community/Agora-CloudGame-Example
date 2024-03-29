package io.agora.cloudgame.network.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.agora.cloudgame.model.JsonModel;

public class GameEntity implements JsonModel {

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

    @NonNull
    @Override
    public String toString() {
        return "GameEntity{" +
                "gameId='" + gameId + '\'' +
                ", name='" + name + '\'' +
                ", vendor='" + vendor + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", introduce='" + introduce + '\'' +
                '}';
    }
}
