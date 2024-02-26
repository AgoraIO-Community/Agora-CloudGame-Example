package io.agora.cloudgame.network.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.agora.cloudgame.model.JsonModel;

public class RtcConfig implements JsonModel {

    @Expose
    @SerializedName("broadcast_uid")
    public int broadcastUid;

    @Expose
    @SerializedName("uid")
    public int uid;

    @Expose
    @SerializedName("token")
    public String token;

    @Expose
    @SerializedName("channel_name")
    public String channelName;

    @Expose
    @SerializedName("encryption")
    public String encryption;

    @NonNull
    @Override
    public String toString() {
        return "RtcConfig{" +
                "broadcastUid=" + broadcastUid +
                ", uid=" + uid +
                ", token='" + token + '\'' +
                ", channelName='" + channelName + '\'' +
                ", encryption='" + encryption + '\'' +
                '}';
    }
}
