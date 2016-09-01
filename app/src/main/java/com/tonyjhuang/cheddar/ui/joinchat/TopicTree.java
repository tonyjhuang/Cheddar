package com.tonyjhuang.cheddar.ui.joinchat;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import java.util.List;

/**
 * Created by tonyjhuang on 9/1/16.
 */
@AutoValue
public abstract class TopicTree implements Parcelable{
    public abstract List<Topic> topics();

    public static TopicTree create(List<Topic> topics) {
        return new AutoValue_TopicTree(topics);
    }
}
