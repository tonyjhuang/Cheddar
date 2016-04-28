package com.tonyjhuang.cheddar.api.models.realm;

import com.tonyjhuang.cheddar.api.models.value.ChatRoom;
import com.tonyjhuang.cheddar.api.models.value.MetaData;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmChatRoom extends RealmObject implements ValueSource {

    @PrimaryKey
    private String objectId;
    private Date createdAt;
    private Date updatedAt;
    private int maxOccupancy;
    private int numOccupants;

    @Override
    public Object toValue() {
        MetaData metaData = MetaData.create(objectId, createdAt, updatedAt);
        return ChatRoom.create(metaData, maxOccupancy, numOccupants);
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    public void setMaxOccupancy(int maxOccupancy) {
        this.maxOccupancy = maxOccupancy;
    }

    public int getNumOccupants() {
        return numOccupants;
    }

    public void setNumOccupants(int numOccupants) {
        this.numOccupants = numOccupants;
    }
}
