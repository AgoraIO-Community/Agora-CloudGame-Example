// -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
//
// Copyright (C) 2018 Opera Software AS. All rights reserved.
//
// This file is an original work developed by Opera Software AS

package me.add1.iris.collection;

import android.content.Context;
import android.view.View;

public class InvalidViewHolder<T extends ViewItem> extends CollectionItemViewHolder<T> {
    public InvalidViewHolder(Context context) {
        this(new View(context));
    }

    private InvalidViewHolder(View itemView) {
        super(itemView);
        itemView.setVisibility(View.GONE);
    }
}