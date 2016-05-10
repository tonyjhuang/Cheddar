package com.tonyjhuang.cheddar.api.models.value;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.util.Date;

/**
 * A single Cheddar user.
 */
@AutoValue
public abstract class User {

    public static User create(MetaData metaData,
                              String userName,
                              boolean emailVerified) {
        return new AutoValue_User(metaData.objectId(),
                metaData.createdAt(),
                metaData.updatedAt(),
                userName, emailVerified);
    }

    public static TypeAdapter<User> typeAdapter(Gson gson) {
        return new AutoValue_User.GsonTypeAdapter(gson);
    }

    public abstract String objectId();

    public abstract Date createdAt();

    public abstract Date updatedAt();

    public abstract String username();

    public abstract boolean emailVerified();

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User that = (User) o;
        return objectId().equals(that.objectId());
    }

    public int hashCode() {
        return objectId().hashCode();
    }
}
