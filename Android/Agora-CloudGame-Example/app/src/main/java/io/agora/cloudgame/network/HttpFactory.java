package io.agora.cloudgame.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.agora.cloudgame.constants.Constants;
import io.agora.cloudgame.example.BuildConfig;
import io.agora.cloudgame.utils.FileUtils;
import io.agora.cloudgame.utils.KeyCenter;
import io.agora.cloudgame.utils.Utils;
import me.add1.iris.utilities.Lazy;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpFactory {
    private static HttpFactory sS;

    private final Lazy<OkHttpClient> mApiHttpClient;
    private final Lazy<OkHttpClient> mResourceHttpClient;


    public static HttpFactory getInstance(@NonNull Context context) {
        if (sS == null) {
            sS = new HttpFactory(context);
        }
        return sS;
    }

    public HttpFactory(Context context) {
        mApiHttpClient = new Lazy<OkHttpClient>() {
            @Override
            protected OkHttpClient make() {
                File apiDir = FileUtils.getCacheDir(context, "api");
                HttpLoggingInterceptor interceptor =
                        new HttpLoggingInterceptor(msg -> Log.i(Constants.TAG + "-" + HttpFactory.class.getSimpleName(), msg));
                interceptor.level(HttpLoggingInterceptor.Level.BODY);

                return new OkHttpClient.Builder()
                        .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
                        .addInterceptor(interceptor)
                        .cache(new Cache(apiDir, 10 * 1024 * 1024))
                        .addInterceptor(new AuthorizationInterceptor())
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .connectTimeout(10, TimeUnit.SECONDS).build();
            }
        };

        mResourceHttpClient = new Lazy<OkHttpClient>() {
            @Override
            protected OkHttpClient make() {
                return new OkHttpClient.Builder()
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .connectTimeout(10, TimeUnit.SECONDS).build();
            }
        };

        sS = this;
    }

    public OkHttpClient getApiHttpClient() {
        return mApiHttpClient.get();
    }

    public OkHttpClient getResourceHttpClient() {
        return mResourceHttpClient.get();
    }

    class AuthorizationInterceptor implements Interceptor {
        String apiHost;

        AuthorizationInterceptor() {
            apiHost = Uri.parse(BuildConfig.API_HOST).getHost();
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {

            Request originalRequest = chain.request();
            if (!originalRequest.url().host().equals(apiHost)) {
                return chain.proceed(originalRequest);
            }


            Request authRequest = originalRequest.newBuilder()
                    .addHeader("Content-Type", "text/plain")
                    .removeHeader("User-Agent")
                    .addHeader("User-Agent", Utils.getUserAgent())
                    .addHeader("Authorization", String.format("agora token=%s", KeyCenter.getRtmToken2(KeyCenter.getUserUid())))
                    .method(originalRequest.method(), originalRequest.body())
                    .build();
            return chain.proceed(authRequest);
        }
    }

}
