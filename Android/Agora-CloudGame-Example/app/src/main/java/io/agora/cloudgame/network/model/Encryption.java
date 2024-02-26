package io.agora.cloudgame.network.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.agora.cloudgame.model.JsonModel;

public class Encryption implements JsonModel {

    @Expose
    @SerializedName("mode")
    public int mode;

    @Expose
    @SerializedName("secret")
    public String secret;

    @Expose
    @SerializedName("salt")
    public String salt;

    @NonNull
    @Override
    public String toString() {
        return "Encryption{" +
                "mode=" + mode +
                ", secret='" + secret + '\'' +
                ", salt='" + salt + '\'' +
                '}';
    }
}
