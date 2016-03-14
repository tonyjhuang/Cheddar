package com.tonyjhuang.cheddar.ui.chat;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.MessageEvent;
import com.tonyjhuang.cheddar.api.models.Presence;
import com.tonyjhuang.cheddar.ui.chat.ChatItemViewInfo.Direction;
import com.tonyjhuang.cheddar.ui.chat.ChatItemViewInfo.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the displaying of MessageEvent objects in a list.
 */
public class MessageEventListAdapter extends BaseAdapter {

    private static final String TAG = MessageEventListAdapter.class.getSimpleName();

    /**
     * Constants for determining item view type.
     */
    private static final int MESSAGE_LEFT = 0;
    private static final int MESSAGE_RIGHT = 1;
    private static final int PRESENCE = 2;

    List<ChatItemViewInfo> itemViewInfos = new ArrayList<>();

    // Holds indices to all of the placeholder messagess in itemViewInfos.
    List<Integer> placeholderMessageIndexes = new ArrayList<>();

    // Used for determining the direction of new Messages.
    private String currentUserId;

    public MessageEventListAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    /**
     * Either creates a new ChatItemViewInfo for |messageEvent| or updates
     * a matching, existing one (placeholder) if it exists.
     * TODO: Either add a boolean flag here or a new method to
     * TODO: denote adding to the end of the boolean. This currently
     * TODO: loops through itemViewInfos from youngest to oldest
     * TODO: and is extremely inefficient for adding replayed MessageEvents.
     */
    public void addOrUpdateMessageEvent(MessageEvent messageEvent) {
        addOrUpdateMessageEvent(messageEvent, true);
    }

    /**
     * Adds a MessageEvent to this adapter by order of date. If addToEnd is true
     * then this method will attempt to add it to the end of the list (most recent) and
     * search for the proper state backwards.
     */
    public void addOrUpdateMessageEvent(MessageEvent messageEvent, boolean addToEnd) {
        switch (messageEvent.getType()) {
            case MESSAGE:
                addOrUpdateMessage((Message) messageEvent, addToEnd);
                break;
            case PRESENCE:
                addPresence((Presence) messageEvent, addToEnd);
                break;
            default:
                Log.e(TAG, "Encountered unrecognized MessageEvent: " + messageEvent.toString());
        }
    }

    /**
     * Adds |presence| to the adapter by creating a new ChatItemViewInfo.
     */
    private void addPresence(Presence presence, boolean addToEnd) {
        addNewChatItemViewInfo(new ChatItemViewInfo(presence), addToEnd);
    }

    /**
     * Adds a Message to this adapter by either creating a new ChatItemViewInfo
     * or by updating an existing placeholder message.
     */
    private void addOrUpdateMessage(Message message, boolean addToEnd) {
        Direction direction = message.getAlias().getUserId().equals(currentUserId) ?
                ChatItemViewInfo.Direction.OUTGOING : Direction.INCOMING;
        if (direction == Direction.OUTGOING) {
            updateOutgoingMessage(message, Status.SENT, addToEnd);
        } else {
            addNewChatItemViewInfo(new ChatItemViewInfo(message, direction, Status.SENT), addToEnd);
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
     * |newStatus|. If there is no existing ChatItemViewInfo that matches |message|, then
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
            addNewChatItemViewInfo(new ChatItemViewInfo(message, Direction.OUTGOING, newStatus),
                    addToEnd);
        }
    }

    /**
     * Adds a new ChatItemViewInfo based on createdAt time, updates
     * placeholder message indexes.
     */
    private void addNewChatItemViewInfo(ChatItemViewInfo viewInfo, boolean addToEnd) {
        int indexOfNewViewInfo = 0;

        if (addToEnd) {
            for (int i = itemViewInfos.size() - 1; i >= 0; i--) {
                if (viewInfo.getDate().after(getItem(i).getDate())) {
                    indexOfNewViewInfo = i + 1;
                    break;
                }
            }
        } else {
            for (int i = 0; i < itemViewInfos.size(); i++) {
                if (viewInfo.getDate().before(getItem(i).getDate())) {
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
     * Find the oldest ChatItemViewInfo that has |body| as its message body.
     * NOTE: Returns an index into placeholderMessages, NOT the index
     * into itemViewInfos.
     */
    private int findPlaceholderMessageIndexIndex(String body) {
        for (int i = 0; i < placeholderMessageIndexes.size(); i++) {
            int index = placeholderMessageIndexes.get(i);
            ChatItemViewInfo info = getItem(index);
            if (info.message.getBody().equals(body)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Creates a placeholder ChatItemViewInfo and adds it to the adapter.
     */
    public void addPlaceholderMessage(Message placeholder) {
        ChatItemViewInfo info = new ChatItemViewInfo(placeholder, Direction.OUTGOING, Status.SENDING);
        itemViewInfos.add(info);
        placeholderMessageIndexes.add(itemViewInfos.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return itemViewInfos.size();
    }

    @Override
    public ChatItemViewInfo getItem(int position) {
        return itemViewInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        ChatItemViewInfo info = getItem(position);
        if (info.hasMessage()) {
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

        ChatItemViewInfo info = getItem(position);
        ChatItemViewInfo prevInfo = position - 1 >= 0 ? getItem(position - 1) : null;
        ChatItemViewInfo nextInfo = position + 1 < itemViewInfos.size() ? getItem(position + 1) : null;

        if (convertView == null) {
            switch (viewType) {
                case MESSAGE_LEFT:
                case MESSAGE_RIGHT:
                    convertView = MessageView_.build(parent.getContext(), info.direction);
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
                MessageView messageView = (MessageView) convertView;
                messageView.setInfo(info, prevInfo, nextInfo);
                break;
            case PRESENCE:
                String presenceText = info.presence.alias.getName();
                presenceText += info.presence.action == Presence.Action.JOIN ?
                        " has joined" : " has left";
                ((TextView) convertView).setText(presenceText.toUpperCase());
                break;
        }

        return convertView;
    }

}
