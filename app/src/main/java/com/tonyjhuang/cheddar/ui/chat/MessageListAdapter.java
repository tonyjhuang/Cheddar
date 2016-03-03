package com.tonyjhuang.cheddar.ui.chat;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.ui.chat.MessageInfo.Direction;
import com.tonyjhuang.cheddar.ui.chat.MessageInfo.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyjhuang on 2/18/16.
 */
public class MessageListAdapter extends BaseAdapter {

    List<MessageInfo> messages = new ArrayList<>();

    // Holds indices to all of the placeholder messages in messages.
    List<Integer> placeholderMessages = new ArrayList<>();
    private String currentUserId;

    public MessageListAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void addOrUpdateMessage(Message message) {
        Direction direction = message.getAlias().getUserId().equals(currentUserId) ?
                MessageInfo.Direction.OUTGOING : Direction.INCOMING;
        if (direction == Direction.OUTGOING) {
            updateOutgoing(message, Status.SENT);
        } else {
            // todo: Set the proper location for the message based on date
            addNewMessageInfo(new MessageInfo(message, direction, Status.SENT));
        }
    }

    public void notifyFailed(Message message) {
        updateOutgoing(message, Status.FAILED);
    }

    private void updateOutgoing(Message message, Status newStatus) {
        // If we have a placeholder, update that, otherwise add as a new outgoing message.
        int placeholderIndexIndex = findPlaceholderMessageIndexIndex(message.getBody());
        if (placeholderIndexIndex != -1) {
            int placeholderIndex = placeholderMessages.get(placeholderIndexIndex);
            placeholderMessages.remove(placeholderIndexIndex);
            messages.get(placeholderIndex).status = newStatus;
            notifyDataSetChanged();
        } else {
            addNewMessageInfo(new MessageInfo(message, Direction.OUTGOING, newStatus));
        }
    }

    // Adds a new MessageInfo to our list of MessageInfos based on createdAt time.
    private void addNewMessageInfo(MessageInfo messageInfo) {
        for(int i = messages.size()-1; i >= 0; i--) {
            if(messageInfo.message.getCreatedAt().after(getItem(i).message.getCreatedAt())) {
                messages.add(i, messageInfo);
                notifyDataSetChanged();
                return;
            }
        }
        messages.add(0, messageInfo);
        notifyDataSetChanged();
    }

    // Find the oldest MessageInfo that has |body| as its body.
    // NOTE: Returns an index into placeholderMessages, NOT the index
    // into messages.
    private int findPlaceholderMessageIndexIndex(String body) {
        for (int i = 0; i < placeholderMessages.size(); i++) {
            int index = placeholderMessages.get(i);
            MessageInfo info = getItem(index);
            if (info.message.getBody().equals(body)) {
                return i;
            }
        }
        return -1;
    }

    public void addPlaceholderMessage(Message placeholder) {
        MessageInfo info = new MessageInfo(placeholder, Direction.OUTGOING, Status.SENDING);
        messages.add(info);
        placeholderMessages.add(messages.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public MessageInfo getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).direction == Direction.INCOMING ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageInfo info = getItem(position);
        MessageInfo prevInfo = position - 1 >= 0 ? getItem(position - 1) : null;
        MessageInfo nextInfo = position + 1 < messages.size() ? getItem(position + 1) : null;

        if (convertView == null) {
            convertView = MessageView_.build(parent.getContext(), info.direction);
        }

        MessageView messageView = (MessageView) convertView;
        messageView.setInfo(info, prevInfo, nextInfo);

        return convertView;
    }

}
