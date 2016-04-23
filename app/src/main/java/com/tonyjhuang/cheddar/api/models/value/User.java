package com.tonyjhuang.cheddar.api.models.value;

import com.google.auto.value.AutoValue;

import java.util.Date;

/**
 * A single Cheddar user.
 */
@AutoValue
public abstract class User {

    public static User create(MetaData metaData,
                              String userName,
                              String password) {
        return new AutoValue_User(metaData.objectId(),
                metaData.createdAt(),
                metaData.updatedAt(),
                userName, password);
    }

    public abstract String objectId();

    public abstract Date createdAt();

    public abstract Date updatedAt();

    public abstract String userName();

    public abstract String password();

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
