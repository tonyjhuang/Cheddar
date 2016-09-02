package com.tonyjhuang.cheddar.ui.joinchat;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.util.List;

@AutoValue
public abstract class TopicTree implements Parcelable{
    public abstract List<Topic> topics();

    public static TopicTree create(List<Topic> topics) {
        return new AutoValue_TopicTree(topics);
    }

    public static TypeAdapter<TopicTree> typeAdapter(Gson gson) {
        return new AutoValue_TopicTree.GsonTypeAdapter(gson);
    }
}
