package com.tonyjhuang.cheddar.ui.chat;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.models.ChatEvent;
import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.Presence;
import com.tonyjhuang.cheddar.ui.chat.ChatEventViewInfo.Direction;
import com.tonyjhuang.cheddar.ui.chat.ChatEventViewInfo.Status;

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

    List<ChatEventViewInfo> itemViewInfos = new ArrayList<>();

    // Holds indices to all of the placeholder messages in itemViewInfos.
    // INVARIANT: These indices MUST point to MessageChatItemViewInfos.
    List<Integer> placeholderMessageIndexes = new ArrayList<>();

    // Used for determining the direction of new Messages.
    private String currentUserId;

    public ChatEventListAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    /**
     * Either creates a new ChatEventViewInfo for |chatEvent| or updates
     * a matching, existing one (placeholder) if it exists.
     * TODO: Either add a boolean flag here or a new method to
     * TODO: denote adding to the end of the boolean. This currently
     * TODO: loops through itemViewInfos from youngest to oldest
     * TODO: and is extremely inefficient for adding replayed MessageEvents.
     */
    public void addOrUpdateMessageEvent(ChatEvent chatEvent) {
        addOrUpdateMessageEvent(chatEvent, true);
    }

    /**
     * Adds a ChatEvent to this adapter by order of date. If addToEnd is true
     * then this method will attempt to add it to the end of the list (most recent) and
     * search for the proper state backwards.
     */
    public void addOrUpdateMessageEvent(ChatEvent chatEvent, boolean addToEnd) {
        switch (chatEvent.getType()) {
            case MESSAGE:
                addOrUpdateMessage((Message) chatEvent, addToEnd);
                break;
            case PRESENCE:
                addPresence((Presence) chatEvent, addToEnd);
                break;
            default:
                Log.e(TAG, "Encountered unrecognized ChatEvent: " + chatEvent.toString());
        }
    }

    /**
     * Adds |presence| to the adapter by creating a new ChatEventViewInfo.
     */
    private void addPresence(Presence presence, boolean addToEnd) {
        addNewChatItemViewInfo(new PresenceChatEventViewInfo(presence), addToEnd);
    }

    /**
     * Adds a Message to this adapter by either creating a new ChatEventViewInfo
     * or by updating an existing placeholder message.
     */
    private void addOrUpdateMessage(Message message, boolean addToEnd) {
        Direction direction = message.getAlias().getUserId().equals(currentUserId) ?
                ChatEventViewInfo.Direction.OUTGOING : Direction.INCOMING;
        if (direction == Direction.OUTGOING) {
            updateOutgoingMessage(message, Status.SENT, addToEnd);
        } else {
            addNewChatItemViewInfo(new MessageChatEventViewInfo(message, direction, Status.SENT), addToEnd);
        }
    }

    /**
     * Notifies this adapter that the passed in message should be marked as FAILED.
     */
    public void notifyFailed(Message message) {
        updateOutgoingMessage(message, Status.FAILED, true);
    }

    /**
     * Updates the oldest existing adapter that shares the same body as |message| with
     * |newStatus|. If there is no existing ChatEventViewInfo that matches |message|, then
     * a new one is added to the adapter with |newStatus|.
     */
    private void updateOutgoingMessage(Message message, Status newStatus, boolean addToEnd) {
        // If we have a placeholder, update that, otherwise add as a new outgoing message.
        int placeholderIndexIndex = findPlaceholderMessageIndexIndex(message.getBody());
        if (placeholderIndexIndex != -1) {
            int placeholderIndex = placeholderMessageIndexes.get(placeholderIndexIndex);
            placeholderMessageIndexes.remove(placeholderIndexIndex);
            itemViewInfos.get(placeholderIndex).status = newStatus;
            notifyDataSetChanged();
        } else {
            addNewChatItemViewInfo(new MessageChatEventViewInfo(message, Direction.OUTGOING, newStatus),
                    addToEnd);
        }
    }

    /**
     * Adds a new ChatEventViewInfo based on createdAt time, updates
     * placeholder message indexes.
     */
    private void addNewChatItemViewInfo(ChatEventViewInfo viewInfo, boolean addToEnd) {
        int indexOfNewViewInfo = 0;

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
        notifyDataSetChanged();
    }

    /**
     * Find the oldest ChatEventViewInfo that has |body| as its message body.
     * NOTE: Returns an index into placeholderMessages, NOT the index
     * into itemViewInfos.
     */
    private int findPlaceholderMessageIndexIndex(String body) {
        for (int i = 0; i < placeholderMessageIndexes.size(); i++) {
            int index = placeholderMessageIndexes.get(i);
            Message placeholder = getItem(index).getMessage();
            if (placeholder.getBody().equals(body)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Creates a placeholder ChatEventViewInfo and adds it to the adapter.
     */
    public void addPlaceholderMessage(Message placeholder) {
        MessageChatEventViewInfo info = new MessageChatEventViewInfo(placeholder, Direction.OUTGOING, Status.SENDING);
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
        if (info.getMessage() != null) {
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
                case MESSAGE_RIGHT:
                    convertView = ChatEventView_.build(parent.getContext(), info.direction);
                    break;
                case PRESENCE:
                    convertView = View.inflate(parent.getContext(), R.layout.stub_presence_view, null);
                    break;
                default:
                    convertView = new View(parent.getContext());
            }
        }

        switch (viewType) {
            case MESSAGE_LEFT:
            case MESSAGE_RIGHT:
                ChatEventView chatEventView = (ChatEventView) convertView;
                chatEventView.setMessageInfo((MessageChatEventViewInfo) info, prevInfo, nextInfo);
                break;
            case PRESENCE:
                Presence presence = info.getPresence();
                String presenceText = presence.getAlias().getName();
                switch (presence.getAction()) {
                    case JOIN:
                        presenceText += " has joined";
                        convertView.setVisibility(View.VISIBLE);
                        break;
                    case LEAVE:
                        presenceText += " has left";
                        convertView.setVisibility(View.VISIBLE);
                        break;
                    default:
                        convertView.setVisibility(View.GONE);
                }
                ((TextView) convertView).setText(presenceText.toUpperCase());
                break;
        }

        return convertView;
    }

}
