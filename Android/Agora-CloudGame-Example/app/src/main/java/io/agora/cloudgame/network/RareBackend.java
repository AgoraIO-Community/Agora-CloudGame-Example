package io.agora.cloudgame.network;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import io.agora.cloudgame.AppContext;
import io.agora.cloudgame.example.BuildConfig;
import io.agora.cloudgame.model.DynamicModel;
import io.agora.cloudgame.model.DynamicModelRuntimeTypeAdapterFactory;
import io.agora.cloudgame.model.JsonModel;
import io.agora.cloudgame.network.model.ApiResult;
import io.agora.cloudgame.network.model.GameDetailResult;
import io.agora.cloudgame.network.model.GameListResult;
import io.agora.cloudgame.network.model.GameStateResult;
import io.agora.cloudgame.network.model.SendMessageBody;
import io.agora.cloudgame.network.model.StartGameBody;
import io.agora.cloudgame.network.model.StartGameResult;
import io.agora.cloudgame.network.model.StopGameBody;
import me.add1.iris.ApiRequestException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RareBackend {

    private static RareBackend sS;
    public RareService mRareService;

    public static RareBackend getInstance() {
        if (sS == null) {
            sS = new RareBackend();
        }
        return sS;
    }

    public RareBackend() {
        Context mContext = AppContext.Companion.get();
        OkHttpClient mApiClient = HttpFactory.getInstance(mContext).getApiHttpClient();

        DynamicModelRuntimeTypeAdapterFactory factory =
                DynamicModelRuntimeTypeAdapterFactory.of(DynamicModel.class);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_HOST)
                .client(mApiClient)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .registerTypeAdapterFactory(factory)
                                .create()))
                .build();

        mRareService = retrofit.create(RareService.class);
    }


    public interface ApiRequestCallback<T> {
        void onSucceed(@NonNull ApiResult<T> t);

        default void onSucceed(@NonNull T t) {
            ApiResult<T> result = new ApiResult<>();
            result.data = t;
            result.errNo = 0;
            onSucceed(result);
        }

        default void onFailure(@NonNull ApiRequestException e) {

        }
    }

    public static class DefaultApiCallback<T> implements Callback<ApiResult<T>> {
        @Nullable
        final ApiRequestCallback<T> callback;

        public DefaultApiCallback(ApiRequestCallback<T> callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<ApiResult<T>> call, Response<ApiResult<T>> response) {
            if (callback == null) {
                return;
            }

            if (response.body() == null) {
                callback.onFailure(
                        ApiRequestException.obtain(response.code(), response.message()));
            } else if (!response.body().isSucceed()) {
                callback.onFailure(
                        ApiRequestException.obtain(response.body().errNo, response.body().errorMsg));
            } else {
                if (0 != response.body().errNo) {
                    callback.onFailure(
                            ApiRequestException.obtain(response.body().errNo, response.body().errorMsg));
                } else {
                    callback.onSucceed(response.body());
                }
            }
        }

        @Override
        public void onFailure(Call<ApiResult<T>> call, Throwable t) {
            if (callback != null) {
                callback.onFailure(ApiRequestException.obtain(t));
            }
        }
    }

    public static class ActionApiCallback implements Callback<ApiResult<JsonModel.Empty>> {
        @Nullable
        final ApiRequestCallback<Boolean> callback;

        public ActionApiCallback(ApiRequestCallback<Boolean> callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<ApiResult<JsonModel.Empty>> call,
                               Response<ApiResult<JsonModel.Empty>> response) {
            if (callback == null) {
                return;
            }
            if (response.body() == null) {
                callback.onFailure(
                        ApiRequestException.obtain(ApiRequestException.CODE_EMPTY_BODY));
            } else if (!response.body().isSucceed()) {
                callback.onFailure(
                        ApiRequestException.obtain(response.body().errNo, response.body().errorMsg));
            } else {
                if (0 != response.body().errNo) {
                    callback.onFailure(
                            ApiRequestException.obtain(response.body().errNo, response.body().errorMsg));
                } else {
                    callback.onSucceed(true);
                }
            }
        }

        @Override
        public void onFailure(Call<ApiResult<JsonModel.Empty>> call, Throwable t) {
            if (callback != null) {
                callback.onFailure(ApiRequestException.obtain(t));
            }
        }
    }

    public void getGames(String appId, @Nullable final ApiRequestCallback<GameListResult> callback) {
        mRareService.getGames(appId).enqueue(new DefaultApiCallback<>(callback));
    }

    public void gamesDetails(String appId, String gameId, @Nullable final ApiRequestCallback<GameDetailResult> callback) {
        mRareService.gamesDetails(appId, gameId).enqueue(new DefaultApiCallback<>(callback));
    }

    public void startGame(String appId, String gameId, String roomId, StartGameBody body, @Nullable final ApiRequestCallback<StartGameResult> callback) {
        mRareService.startGame(appId, gameId, roomId, body).enqueue(new DefaultApiCallback<>(callback));
    }

    public void stopGame(String appId, String gameId, String roomId, StopGameBody body, @Nullable final ApiRequestCallback<Boolean> callback) {
        mRareService.stopGame(appId, gameId, roomId, body).enqueue(new ActionApiCallback(callback));
    }

    public void gameState(String appId, String taskId, @Nullable final ApiRequestCallback<GameStateResult> callback) {
        mRareService.gameState(appId, taskId).enqueue(new DefaultApiCallback<>(callback));
    }

    public void sendGiftV2(String appId, String roomId, String gameId, SendMessageBody entity, @Nullable final ApiRequestCallback<Boolean> callback) {
        mRareService.sendGiftV2(appId, roomId, gameId, entity).enqueue(new ActionApiCallback(callback));
    }

    public void gameLikeV2(String appId, String roomId, String gameId, SendMessageBody entity, @Nullable final ApiRequestCallback<Boolean> callback) {
        mRareService.gameLikeV2(appId, roomId, gameId, entity).enqueue(new ActionApiCallback(callback));
    }

    public void gameCommentV2(String appId, String roomId, String gameId, SendMessageBody entity, @Nullable final ApiRequestCallback<Boolean> callback) {
        mRareService.gameCommentV2(appId, roomId, gameId, entity).enqueue(new ActionApiCallback(callback));
    }

    public void downloadFile(@NonNull Context context, @NonNull String url,
                             @NonNull File targetFile,
                             @NonNull ApiRequestCallback<Boolean> callback) {
        HttpFactory.getInstance(context).getResourceHttpClient()
                .newCall(new Request.Builder().url(url)
                        .get().build()).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                        callback.onFailure(ApiRequestException.obtain(e));
                    }

                    @Override
                    public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response)
                            throws IOException {
                        Source source = response.body().source();

                        BufferedSink sink = Okio.buffer(Okio.sink(targetFile));
                        sink.writeAll(source);
                        sink.close();
                        callback.onSucceed(true);
                    }
                });
    }

}
