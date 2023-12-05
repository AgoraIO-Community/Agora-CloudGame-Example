// -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
//
// Copyright (C) 2018 Opera Software AS. All rights reserved.
//
// This file is an original work developed by Opera Software AS

package me.add1.iris.feed;


import android.app.Activity;
import android.content.Context;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import me.add1.iris.collection.ViewItem;

public class FeedItem<T> implements ViewItem, Serializable {

    public static class Status {
        public static final int IMPRESSION = 1;
        public static final int END_IMPRESSION = 1 << 1;
        public static final int VISIBILITY = 1 << 2;
        public static final int STATUS_PROCESS = 1 << 3;
        public static final int RENDERED = 1 << 4;
        public static final int SELECTED = 1 << 5;
    }

    private static final long serialVersionUID = 5672984308215396569L;

    public int status;

    @NonNull
    public final String id;
    @NonNull
    public final int type;
    @NonNull
    public final T model;
    @NonNull
    public Context context;

    @NonNull
    public Activity activity;

    @NonNull
    public FragmentManager fragmentManager;

    public FeedItem(int type, @NonNull String id, @NonNull T model) {
        this.id = id;
        this.type = type;
        this.model = model;
    }
    public FeedItem(int type, @NonNull String id, @NonNull T model,@NonNull Context context) {
        this.id = id;
        this.type = type;
        this.model = model;
        this.context=context;
    }

    public FeedItem(int type, @NonNull String id, @NonNull T model,@NonNull Activity activity) {
        this.id = id;
        this.type = type;
        this.model = model;
        this.activity=activity;
    }

    public FeedItem(int type, @NonNull String id, @NonNull T model,@NonNull Context context,@NonNull FragmentManager fragmentManager) {
        this.id = id;
        this.type = type;
        this.model = model;
        this.context=context;
        this.fragmentManager=fragmentManager;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getType() {
        return type;
    }

    @NonNull
    public T getModel() {
        return model;
    }

    public boolean isStatus(int mask) {
        return (status & mask) == mask;
    }

    public void setStatus(int mask) {
        status = (status | mask);
    }

    public void removeStatus(int mask) {
        status = (status & ~mask);
    }
}
