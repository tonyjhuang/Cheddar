package com.tonyjhuang.cheddar.api.models.realm;

import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.models.value.MetaData;

import java.util.Date;

import io.realm.RealmObject;

public class RealmChatEvent extends RealmObject implements ValueSource {

    private String objectId;
    private Date createdAt;
    private Date updatedAt;
    private ChatEvent.ChatEventType type;
    private RealmAlias alias;
    private String body;

    @Override
    public ChatEvent toValue() {
        MetaData metaData = MetaData.create(objectId, createdAt, updatedAt);
        return ChatEvent.create(metaData, type, alias.toValue(), body);
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

    public ChatEvent.ChatEventType getType() {
        return type;
    }

    public void setType(ChatEvent.ChatEventType type) {
        this.type = type;
    }

    public RealmAlias getAlias() {
        return alias;
    }

    public void setAlias(RealmAlias alias) {
        this.alias = alias;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
