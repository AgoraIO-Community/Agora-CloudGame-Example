package io.agora.cloudgame.network.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.agora.cloudgame.model.JsonModel;

public class FeatureEntity implements JsonModel {

    @Expose
    @SerializedName("like")
    public int like;

    @Expose
    @SerializedName("comment")
    public int comment;

    @NonNull
    @Override
    public String toString() {
        return "FeatureEntity{" +
                "like=" + like +
                ", comment=" + comment +
                '}';
    }
}
