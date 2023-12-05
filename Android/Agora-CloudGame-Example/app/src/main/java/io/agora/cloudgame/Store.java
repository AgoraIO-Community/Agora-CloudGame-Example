package io.agora.cloudgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import io.agora.cloudgame.model.JsonModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Store {
    public static final String KEY_ACCOUNT = "account";
    public static final String KEY_USER_INFO = "user_info";
    public static final String KEY_USER_TOKEN = "user_token";
    public static final String KEY_TODAY_POINT = "today_point";

    public static final String KEY_GAME_DATA = "game_data";

    private final Gson mGson;

    public enum Pref {
        ACCOUNT("account"),
        DEF("def");

        @NonNull
        public final String text;

        Pref(@NonNull String text) {
            this.text = text;
        }

        @NonNull
        public static Pref fromValue(@NonNull String text) throws IllegalArgumentException {
            for (Pref i : values()) {
                if (i.text.equals(text)) {
                    return i;
                }
            }
            throw new IllegalArgumentException("unknown type");
        }
    }

    public Store(Context context) {
        mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();


    }

    private SharedPreferences getPreferences(@NonNull Pref pref) {
        return AppContext.Companion.get().getSharedPreferences(pref.text, 0);
    }

    public void clear(@NonNull Pref pref) {
        getPreferences(pref).edit().clear().apply();
    }

    public <T> T getModel(@NonNull Pref pref, @NonNull String key, @NonNull TypeToken<T> token) {
        return getModel(pref, key, token.getType());
    }

    public <T> T getModel(@NonNull Pref pref, @NonNull String key, @NonNull Class<T> cls) {
        String v = getPreferences(pref).getString(key, null);
        if (TextUtils.isEmpty(v)) return null;
        return mGson.fromJson(v, cls);
    }

    public <T> T getModel(@NonNull Pref pref, @NonNull String key, @NonNull Type type) {
        String v = getPreferences(pref).getString(key, null);
        if (TextUtils.isEmpty(v)) return null;
        return mGson.fromJson(v, type);
    }

    public <T extends JsonModel> void setModel(@NonNull Pref pref, @NonNull String key,
                                               @NonNull T t) {
        String str = mGson.toJson(t);
        getPreferences(pref).edit().putString(key, str).apply();
    }

    public <T extends JsonModel> void setModel(@NonNull Pref pref, @NonNull String key,
                                               @NonNull List<T> t) {
        String str = mGson.toJson(t);
        getPreferences(pref).edit().putString(key, str).apply();
    }

    public String getString(@NonNull Pref pref, @NonNull String key) {
        return getPreferences(pref).getString(key, null);
    }

    public void setString(@NonNull Pref pref, @NonNull String key, @NonNull String content) {
        getPreferences(pref).edit().putString(key, content).apply();
    }

    public int getNumber(@NonNull Pref pref, @NonNull String key) {
        return getPreferences(pref).getInt(key, 0);
    }

    public void setNumber(@NonNull Pref pref, @NonNull String key, @NonNull int num) {
        getPreferences(pref).edit().putInt(key, num);
    }
}