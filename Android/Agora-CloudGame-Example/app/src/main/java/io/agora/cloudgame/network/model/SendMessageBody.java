package io.agora.cloudgame.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.agora.cloudgame.model.JsonModel;

public class SendMessageBody implements JsonModel {
    @Expose
    @SerializedName("payload")
    public String payload;

}
