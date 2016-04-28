package com.tonyjhuang.cheddar.api.models.realm;

import com.tonyjhuang.cheddar.api.models.value.MetaData;
import com.tonyjhuang.cheddar.api.models.value.User;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmUser extends RealmObject implements ValueSource {

    @PrimaryKey
    private String objectId;
    private Date createdAt;
    private Date updatedAt;
    private String username;
    private String password;

    public User toValue() {
        MetaData metaData = MetaData.create(objectId, createdAt, updatedAt);
        return User.create(metaData, username, password);
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
