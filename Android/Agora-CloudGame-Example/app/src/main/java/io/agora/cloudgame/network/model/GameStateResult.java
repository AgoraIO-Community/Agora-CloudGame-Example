package io.agora.cloudgame.network.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.agora.cloudgame.model.JsonModel;

public class GameStateResult implements JsonModel {
    @Expose
    @SerializedName("status")
    public String status;

    @NonNull
    @Override
    public String toString() {
        return "GameStateResult{" +
                "status='" + status + '\'' +
                '}';
    }
}
