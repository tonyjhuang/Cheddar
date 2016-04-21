package com.tonyjhuang.cheddar.api.models.value;

import com.google.auto.value.AutoValue;

import java.util.Date;

/**
 * Contains the metadata that every Value type should have:
 * - objectId
 * - createdAt
 * - updatedAt
 */
@AutoValue
public abstract class MetaData {

    public static MetaData create(String objectId,
                                  Date createdAt,
                                  Date updatedAt) {
        return new AutoValue_MetaData(objectId, createdAt, updatedAt);
    }

    public abstract String objectId();

    public abstract Date createdAt();

    public abstract Date updatedAt();

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetaData that = (MetaData) o;
        return objectId().equals(that.objectId());
    }

    public int hashCode() {
        return objectId().hashCode();
    }
}
