package me.add1.iris;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ApiRequestException extends RuntimeException {

    public static final int CODE_UNKNOWN = -1;
    public static final int CODE_EMPTY_BODY = -2;
    public final int code;
    @Nullable
    public final String message;

    private ApiRequestException(int code, @Nullable String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    private ApiRequestException(@NonNull Throwable e) {
        super(e);
        this.code = CODE_UNKNOWN;
        this.message = null;
    }

    public static ApiRequestException obtain(int code) {
        return new ApiRequestException(code, null);
    }

    public static ApiRequestException obtain(@NonNull String message) {
        return new ApiRequestException(CODE_UNKNOWN, message);
    }

    public static ApiRequestException obtain(int code, @Nullable String message) {
        return new ApiRequestException(code, message);
    }

    public static ApiRequestException obtain(@NonNull Throwable e) {
        return new ApiRequestException(e);

    }
}
