package com.tonyjhuang.cheddar.api.models.value;

import com.google.auto.value.AutoValue;

/**
 * A single Cheddar user.
 */
@AutoValue
public abstract class User {

    public static User create(MetaData metaData,
                              String userName,
                              String password) {
        return new AutoValue_User(metaData, userName, password);
    }

    public abstract MetaData metaData();

    public abstract String userName();

    public abstract String password();

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User that = (User) o;
        return metaData().equals(that.metaData());
    }

    public int hashCode() {
        return metaData().hashCode();
    }
}
