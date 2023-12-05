package io.agora.cloudgame.network.model;

import io.agora.cloudgame.model.JsonModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FeatureEntity implements JsonModel {

    @Expose
    @SerializedName("like")
    public int like;

    @Expose
    @SerializedName("comment")
    public int comment;

}
