package com.tonyjhuang.cheddar.ui.chat;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.ui.chat.chatevent.ChatEventViewInfo;
import com.tonyjhuang.cheddar.ui.chat.chatevent.ChatEventViewInfo.Direction;
import com.tonyjhuang.cheddar.ui.chat.chatevent.ChatEventViewInfo.Status;
import com.tonyjhuang.cheddar.ui.chat.chatevent.IncomingMessageView_;
import com.tonyjhuang.cheddar.ui.chat.chatevent.MessageView;
import com.tonyjhuang.cheddar.ui.chat.chatevent.OutgoingMessageView_;
import com.tonyjhuang.cheddar.ui.chat.chatevent.PresenceView;
import com.tonyjhuang.cheddar.ui.chat.chatevent.PresenceView_;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the displaying of ChatEvent objects in a list.
 */
public class ChatEventListAdapter extends BaseAdapter {

    private static final String TAG = ChatEventListAdapter.class.getSimpleName();

    /**
     * Constants for determining item view type.
     */
    private static final int MESSAGE_LEFT = 0;
    private static final int MESSAGE_RIGHT = 1;
    private static final int PRESENCE = 2;
    /**
     * Used for determining the direction of new Messages.
     */
    private final String currentUserId;
    List<ChatEventViewInfo> itemViewInfos = new ArrayList<>();
    /**
     * Holds indices to all of the placeholder messages in itemViewInfos.
     * INVARIANT: These indices MUST point to MessageChatItemViewInfos.
     */
    List<Integer> placeholderMessageIndexes = new ArrayList<>();

    public ChatEventListAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    /**
     * Adds a ChatEvent to this adapter by order of date. If addToEnd is true
     * then this method will attempt to add it to the end of the list (most recent) and
     * search for the proper state backwards.
     */
    public void addOrUpdateChatEvents(List<ChatEvent> chatEvents, boolean addToEnd) {
        for (ChatEvent chatEvent : chatEvents) {
            switch (chatEvent.type()) {
                case MESSAGE:
                    addOrUpdateMessage(chatEvent, addToEnd);
                    break;
                case PRESENCE:
                    addPresence(chatEvent, addToEnd);
                    break;
                default:
                    Log.e(TAG, "Encountered unrecognized ChatEvent: " + chatEvent.toString());
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Adds |presence| to the adapter by creating a new ChatEventViewInfo.
     */
    private void addPresence(ChatEvent presence, boolean addToEnd) {
        addNewChatItemViewInfo(new ChatEventViewInfo(presence), addToEnd);
    }

    /**
     * Adds a Message to this adapter by either creating a new ChatEventViewInfo
     * or by updating an existing placeholder message.
     */
    private void addOrUpdateMessage(ChatEvent message, boolean addToEnd) {
        Direction direction = message.alias().userId().equals(currentUserId) ?
                ChatEventViewInfo.Direction.OUTGOING : Direction.INCOMING;
        if (direction == Direction.OUTGOING) {
            updateOutgoingMessage(message, Status.SENT, addToEnd);
        } else {
            addNewChatItemViewInfo(new ChatEventViewInfo(message, direction, Status.SENT), addToEnd);
        }
    }

    /**
     * Notifies this adapter that the passed in message should be marked as FAILED.
     */
    public void notifyFailed(ChatEvent message) {
        updateOutgoingMessage(message, Status.FAILED, true);
        notifyDataSetChanged();
    }

    /**
     * Updates the oldest existing adapter that shares the same body as |message| with
     * |newStatus|. If there is no existing ChatEventViewInfo that matches |message|, then
     * a new one is added to the adapter with |newStatus|.
     */
    private void updateOutgoingMessage(ChatEvent message, Status newStatus, boolean addToEnd) {
        // If we have a placeholder, update that, otherwise add as a new outgoing message.
        int placeholderIndexIndex = findPlaceholderMessageIndexIndex(message.body());
        if (placeholderIndexIndex != -1) {
            int placeholderIndex = placeholderMessageIndexes.get(placeholderIndexIndex);
            placeholderMessageIndexes.remove(placeholderIndexIndex);
            itemViewInfos.get(placeholderIndex).status = newStatus;
            notifyDataSetChanged();
        } else {
            addNewChatItemViewInfo(new ChatEventViewInfo(message, Direction.OUTGOING, newStatus),
                    addToEnd);
        }
    }

    /**
     * Adds a new ChatEventViewInfo based on createdAt time, updates
     * placeholder message indexes.
     */
    private void addNewChatItemViewInfo(ChatEventViewInfo viewInfo, boolean addToEnd) {
        int indexOfNewViewInfo = -1;

        if (addToEnd) {
            for (int i = itemViewInfos.size() - 1; i >= 0; i--) {
                ChatEventViewInfo otherInfo = getItem(i);
                // Don't add duplicate ChatEvents
                if (viewInfo.isSameObject(otherInfo)) return;
                if (viewInfo.getDate().after(otherInfo.getDate())) {
                    indexOfNewViewInfo = i + 1;
                    break;
                }
            }
            if (indexOfNewViewInfo == -1)
                indexOfNewViewInfo = 0; // Fell out of loop without assigning.
        } else {
            for (int i = 0; i < itemViewInfos.size(); i++) {
                ChatEventViewInfo otherInfo = getItem(i);
                // Don't add duplicate ChatEvents
                if (viewInfo.isSameObject(otherInfo)) return;
                if (viewInfo.getDate().before(otherInfo.getDate())) {
                    indexOfNewViewInfo = i;
                    break;
                }
            }
            if (indexOfNewViewInfo == -1)
                indexOfNewViewInfo = itemViewInfos.size(); // Fell out of loop.
        }

        itemViewInfos.add(indexOfNewViewInfo, viewInfo);

        // Update placeholder message indexes.
        for (int j = placeholderMessageIndexes.size() - 1; j >= 0; j--) {
            int placeholderIndex = placeholderMessageIndexes.get(j);
            if (placeholderIndex >= indexOfNewViewInfo) {
                placeholderMessageIndexes.set(j, placeholderIndex + 1);
            } else {
                break;
            }
        }
    }

    /**
     * Find the oldest ChatEventViewInfo that has |body| as its message body.
     * NOTE: Returns an index into placeholderMessages, NOT the index
     * into itemViewInfos.
     */
    private int findPlaceholderMessageIndexIndex(String body) {
        for (int i = 0; i < placeholderMessageIndexes.size(); i++) {
            int index = placeholderMessageIndexes.get(i);
            if (getItem(index).chatEvent.body().equals(body)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Creates a placeholder ChatEventViewInfo and adds it to the adapter.
     */
    public void addPlaceholderMessage(ChatEvent placeholder) {
        ChatEventViewInfo info = new ChatEventViewInfo(placeholder, Direction.OUTGOING, Status.SENDING);
        itemViewInfos.add(info);
        placeholderMessageIndexes.add(itemViewInfos.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return itemViewInfos.size();
    }

    @Override
    public ChatEventViewInfo getItem(int position) {
        return itemViewInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        ChatEventViewInfo info = getItem(position);
        if (info.chatEvent.type().equals(ChatEvent.ChatEventType.MESSAGE)) {
            return info.direction == Direction.INCOMING ? MESSAGE_LEFT : MESSAGE_RIGHT;
        } else {
            return PRESENCE;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);

        ChatEventViewInfo info = getItem(position);
        ChatEventViewInfo prevInfo = position - 1 >= 0 ? getItem(position - 1) : null;
        ChatEventViewInfo nextInfo = position + 1 < itemViewInfos.size() ? getItem(position + 1) : null;

        if (convertView == null) {
            switch (viewType) {
                case MESSAGE_LEFT:
                    convertView = IncomingMessageView_.build(parent.getContext());
                    break;
                case MESSAGE_RIGHT:
                    convertView = OutgoingMessageView_.build(parent.getContext());
                    break;
                case PRESENCE:
                    convertView = PresenceView_.build(parent.getContext());
                    break;
                default:
                    convertView = new View(parent.getContext());
            }
        }

        switch (viewType) {
            case MESSAGE_LEFT:
            case MESSAGE_RIGHT:
                MessageView messageView = (MessageView) convertView;
                messageView.setMessageInfo(info, prevInfo, nextInfo);
                convertView.setVisibility(View.VISIBLE);
                break;
            case PRESENCE:
                PresenceView presenceView = (PresenceView) convertView;
                presenceView.setPresenceInfo(info);
                convertView.setVisibility(View.VISIBLE);
                break;
            default:
                convertView.setVisibility(View.GONE);
        }

        return convertView;
    }

}
