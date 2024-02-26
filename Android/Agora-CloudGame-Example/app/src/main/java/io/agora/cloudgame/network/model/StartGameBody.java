package io.agora.cloudgame.network.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.agora.cloudgame.model.JsonModel;

public class StartGameBody implements JsonModel {

    @Expose
    @SerializedName("openid")
    public String openId;

    @Expose
    @SerializedName("nickname")
    public String nickname;

    @Expose
    @SerializedName("avatar_url")
    public String avatarUrl;

    @Expose
    @SerializedName("rtc_config")
    public RtcConfig rtcConfig;

    @NonNull
    @Override
    public String toString() {
        return "StartGameBody{" +
                "openId='" + openId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", rtcConfig=" + rtcConfig +
                '}';
    }
}
