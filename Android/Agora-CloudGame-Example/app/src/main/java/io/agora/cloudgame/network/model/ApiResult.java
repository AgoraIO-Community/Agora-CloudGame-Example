package io.agora.cloudgame.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApiResult<T> {
    @Expose
    @SerializedName("err_no")
    public int errNo;

    @Expose
    @SerializedName("err_msg")
    public String errorMsg;

    @Expose
    @SerializedName("logid")
    public String logId;

    @Expose
    @SerializedName("data")
    public T data;


    public boolean isSucceed() {
        return errNo == 0;
    }
}
