package com.tonyjhuang.cheddar.ui.joinchat;


import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.util.List;

@AutoValue
public abstract class Topic implements Parcelable {

    public static TypeAdapter<Topic> typeAdapter(Gson gson) {
        return new AutoValue_Topic.GsonTypeAdapter(gson);
    }

    public abstract String name();

    public abstract String slug();

    public abstract String drawable();

    public abstract List<Topic> subtopics();
}
