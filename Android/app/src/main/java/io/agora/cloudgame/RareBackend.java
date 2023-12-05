package io.agora.cloudgame;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import io.agora.cloudgame.example.BuildConfig;
import io.agora.cloudgame.model.DynamicModel;
import io.agora.cloudgame.model.DynamicModelRuntimeTypeAdapterFactory;
import io.agora.cloudgame.model.JsonModel;
import io.agora.cloudgame.network.RareService;
import io.agora.cloudgame.network.http.HttpFactory;
import io.agora.cloudgame.network.model.ApiResult;
import io.agora.cloudgame.network.model.GameEntity;
import io.agora.cloudgame.network.model.GameResult;
import io.agora.cloudgame.network.model.SendMessage;
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
    private final Store mStore;

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


        mStore = new Store(mContext);

    }

    @NonNull
    public Store getStore() {
        return mStore;
    }


    public interface ApiRequestCallback<T> {
        void onSucceed(@NonNull ApiResult<T> t);

        default void onSucceed(@NonNull T t) {
            ApiResult<T> result = new ApiResult<>();
            result.data = t;
            result.code = 200;
            onSucceed(result);
        }

        default void onFailure(@NonNull ApiRequestException e) {
            onFailure(e);
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
                String msg = response.body().msg;
                if (TextUtils.isEmpty(msg)) {
                    msg = response.body().errorMsg;
                }
                callback.onFailure(
                        ApiRequestException.obtain(response.body().code, msg));
            } else {
                callback.onSucceed(response.body());
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
                        ApiRequestException.obtain(response.body().code, response.body().msg));
            } else {
                callback.onSucceed(true);
            }
        }

        @Override
        public void onFailure(Call<ApiResult<JsonModel.Empty>> call, Throwable t) {
            if (callback != null) {
                callback.onFailure(ApiRequestException.obtain(t));
            }
        }
    }

    public void getGames(String phone, @Nullable final ApiRequestCallback<GameResult> callback) {
        mRareService.getGames(phone).enqueue(new DefaultApiCallback<>(callback));
    }

    public void gamesDetails(String id, String gameId, @Nullable final ApiRequestCallback<GameResult> callback) {
        mRareService.gamesDetails(id, gameId).enqueue(new DefaultApiCallback<>(callback));
    }

    public void startGame(String id, String gameId, GameEntity entity, @Nullable final ApiRequestCallback<GameResult> callback) {
        mRareService.startGame(id, gameId, entity).enqueue(new DefaultApiCallback<>(callback));
    }

    public void stopGame(String id, String gameId, GameEntity entity, @Nullable final ApiRequestCallback<Boolean> callback) {
        mRareService.stopGame(id, gameId, entity).enqueue(new ActionApiCallback(callback));
    }

    public void gameState(String id, String gameId, String taskId, @Nullable final ApiRequestCallback<GameResult> callback) {
        mRareService.gameState(id, gameId, taskId).enqueue(new DefaultApiCallback<>(callback));
    }

    public void sendGift(String id, String gameId, SendMessage entity, @Nullable final ApiRequestCallback<Boolean> callback) {
        mRareService.sendGift(id, gameId, entity).enqueue(new ActionApiCallback(callback));
    }

    public void gameLike(String id, String gameId, SendMessage entity, @Nullable final ApiRequestCallback<Boolean> callback) {
        mRareService.gameLike(id, gameId, entity).enqueue(new ActionApiCallback(callback));
    }

    public void gameComment(String id, String gameId, SendMessage entity, @Nullable final ApiRequestCallback<Boolean> callback) {
        mRareService.gameComment(id, gameId, entity).enqueue(new ActionApiCallback(callback));
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
