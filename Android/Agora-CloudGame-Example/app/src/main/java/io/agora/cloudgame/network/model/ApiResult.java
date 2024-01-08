package io.agora.cloudgame.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.agora.cloudgame.model.TrackInfo;

public class ApiResult<T> {
    @Expose
    @SerializedName("message")
    public String msg;

    @Expose
    @SerializedName("err_msg")
    public String errorMsg;

    @Expose
    @SerializedName("code")
    public int code;

    @Expose
    @SerializedName("err_no")
    public int errNo;

    @Expose
    @SerializedName("result")
    public T data;

    @SerializedName("trace")
    public String trace;

    @SerializedName("uri")
    public String uri;

    public boolean isSucceed() {
        return code == 200 || code == 0;
    }

    public TrackInfo obtainTrackInfo(final String origin) {
        return TrackInfo.obtain(trace, uri, origin);
    }
}
