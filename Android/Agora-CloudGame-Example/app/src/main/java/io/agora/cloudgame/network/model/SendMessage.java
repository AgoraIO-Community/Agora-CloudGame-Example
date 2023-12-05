package io.agora.cloudgame.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.agora.cloudgame.model.JsonModel;

public class SendMessage implements JsonModel {
    @Expose
    @SerializedName("room_id")
    public String roomId;

    @Expose
    @SerializedName("payload")
    public List<MessageEntity> payload;

}
