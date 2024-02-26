package io.agora.cloudgame.network.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.agora.cloudgame.model.JsonModel;

public class GameListResult implements JsonModel {

    @Expose
    @SerializedName("list")
    public List<GameEntity> list;

    @Expose
    @SerializedName("page_size")
    public int pageSize;

    @Expose
    @SerializedName("page_num")
    public int pageNum;

    @Expose
    @SerializedName("total")
    public int total;

    @NonNull
    @Override
    public String toString() {
        return "GameListEntity{" +
                "list=" + list +
                ", pageSize=" + pageSize +
                ", pageNum=" + pageNum +
                ", total=" + total +
                '}';
    }
}
