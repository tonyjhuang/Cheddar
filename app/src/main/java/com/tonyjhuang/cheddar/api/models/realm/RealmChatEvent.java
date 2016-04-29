package com.tonyjhuang.cheddar.api.models.realm;

import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.models.value.MetaData;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmChatEvent extends RealmObject implements ValueSource {

    @PrimaryKey
    private String objectId;
    private Date createdAt;
    private Date updatedAt;
    private String messageId;
    private String type;
    private RealmAlias alias;
    private String body;

    @Override
    public ChatEvent toValue() {
        MetaData metaData = MetaData.create(objectId, createdAt, updatedAt);
        return ChatEvent.create(metaData,
                messageId,
                ChatEvent.ChatEventType.fromString(type),
                alias.toValue(),
                body);
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

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
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
