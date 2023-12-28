package io.agora.cloudgame.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.agora.cloudgame.model.JsonModel;

public class SendMessageV2 implements JsonModel {
    @Expose
    @SerializedName("payload")
    public String payload;

}
