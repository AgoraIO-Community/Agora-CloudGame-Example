package io.agora.cloudgame.network.model;

import io.agora.cloudgame.model.JsonModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GameResult implements JsonModel {

    @Expose
    @SerializedName("list")
    public List<GameEntity> list;

    @Expose
    @SerializedName("task_id")
    public String taskId;

    @Expose
    @SerializedName("status")
    public String status;

    @Expose
    @SerializedName("feature")
    public FeatureEntity feature;

    @Expose
    @SerializedName("gifts")
    public List<GiftEntity> gifts;

}
