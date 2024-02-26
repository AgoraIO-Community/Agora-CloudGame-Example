package io.agora.cloudgame.network.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.agora.cloudgame.model.JsonModel;

public class StartGameResult implements JsonModel {
    @Expose
    @SerializedName("task_id")
    public String taskId;

    @NonNull
    @Override
    public String toString() {
        return "StartGameResult{" +
                "taskId='" + taskId + '\'' +
                '}';
    }
}
