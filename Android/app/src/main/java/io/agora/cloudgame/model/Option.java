package io.agora.cloudgame.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public interface Option {
    String getId();

    String getContent();

    String getFilterContent();

    public static class StringOption implements Option {
        @NonNull
        private String mS;

        public static StringOption obtain(@NonNull String s) {
            return new StringOption(s);
        }

        private StringOption(String s) {
            mS = s;
        }

        @Override
        public String getContent() {
            return mS;
        }

        @Override
        public String getFilterContent() {
            return mS;
        }

        @Override
        public String getId() {
            return mS;
        }
    }

    class RegularOption implements Option {
        @Expose
        @SerializedName("id")
        public String id;
        @Expose
        @SerializedName("text")
        public String text;
        @Expose
        @SerializedName("job_count")
        public int jobCount;


        public RegularOption() {
        }

        @Override
        public String getContent() {
            return text;
        }

        @Override
        public String getFilterContent() {
            if (jobCount <= 0) return text;
            return text + " (" + jobCount + ") ";
        }

        @Override
        public String getId() {
            return id;
        }

        public int getJobCount() {
            return jobCount;
        }

        @NonNull
        @Override
        public RegularOption clone() {
            RegularOption t = new RegularOption();
            t.id = id;
            t.text = text;
            return t;
        }
    }
}