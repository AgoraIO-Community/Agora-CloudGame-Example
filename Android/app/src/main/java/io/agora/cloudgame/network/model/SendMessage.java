package io.agora.cloudgame.network.model;

import io.agora.cloudgame.model.JsonModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SendMessage implements JsonModel {

    @Expose
    @SerializedName("vid")
    public String vid;

    @Expose
    @SerializedName("room_id")
    public String roomId;

    @Expose
    @SerializedName("payload")
    public List<MessageEntity> payload;

}
