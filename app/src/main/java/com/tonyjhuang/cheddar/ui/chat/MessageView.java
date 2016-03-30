package com.tonyjhuang.cheddar.ui.chat;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.ui.chat.ChatEventViewInfo.Direction;
import com.tonyjhuang.cheddar.ui.utils.TimeHelper;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.DimensionPixelSizeRes;

/**
 * Can't use @EViewGroup(layoutId) here since the
 * layout is set programmatically based on the Message direction.
 */
@EViewGroup
public class MessageView extends RelativeLayout {

    private static final String TAG = MessageView.class.getSimpleName();

    RelativeLayout container;
    AliasDisplayView authorDisplayView;
    TextView authorFullNameView;
    TextView bodyView;

    @ColorRes(R.color.chat_text_background_outgoing)
    int outgoingSentTextBackgroundColor;
    @ColorRes(R.color.chat_text_background_outgoing_sending)
    int outgoingSendingTextBackgroundColor;
    @ColorRes(R.color.chat_text_background_outgoing_failed)
    int outgoingFailedTextBackgroundColor;
    @ColorRes(R.color.chat_text_outgoing)
    int outgoingTextColor;
    @ColorRes(R.color.chat_author_text_outgoing)
    int outgoingAuthorTextColor;
    @ColorRes(R.color.chat_author_background_outgoing)
    int outgoingAuthorBackgroundColor;

    @ColorRes(R.color.chat_text_background_incoming)
    int incomingTextBackgroundColor;
    @ColorRes(R.color.chat_text_incoming)
    int incomingTextColor;
    @ColorRes(R.color.chat_author_text_incoming)
    int incomingAuthorTextColor;
    @ColorRes(R.color.chat_author_background_incoming)
    int incomingAuthorBackgroundColor;

    @DimensionPixelSizeRes(R.dimen.chat_bubble_padding)
    int containerPadding;
    @DimensionPixelSizeRes(R.dimen.chat_bubble_padding_minimized)
    int containerPaddingMinimized;
    @DimensionPixelSizeRes(R.dimen.chat_bubble_padding)
    int containerPaddingExpanded;

    private ChatEventViewInfo info, prevInfo, nextInfo;

    public MessageView(Context context) {
        super(context);
    }

    public MessageView(Context context, Direction direction) {
        super(context);
        int layout = direction == Direction.OUTGOING ?
                R.layout.stub_chat_view_right : R.layout.stub_chat_view_left;
        inflate(getContext(), layout, this);

        container = (RelativeLayout) findViewById(R.id.message_container);
        authorFullNameView = (TextView) findViewById(R.id.author_full_name);
        authorDisplayView = (AliasDisplayView) findViewById(R.id.author_display);
        bodyView = (TextView) findViewById(R.id.body);
    }

    public void setMessageInfo(MessageChatEventViewInfo info, ChatEventViewInfo prev, ChatEventViewInfo next) {
        this.info = info;
        prevInfo = prev;
        nextInfo = next;
        updateViews();
    }

    public void updateViews() {
        String aliasName = info.getMessage().getAlias().getName();
        authorFullNameView.setText(aliasName);
        authorDisplayView.setAliasName(aliasName);
        bodyView.setText(info.getMessage().getBody());

        int textBackgroundColor;

        if (info.direction == Direction.OUTGOING) {
            switch (info.status) {
                case SENDING:
                    textBackgroundColor = outgoingSendingTextBackgroundColor;
                    break;
                case SENT:
                    textBackgroundColor = outgoingSentTextBackgroundColor;
                    break;
                case FAILED:
                    textBackgroundColor = outgoingFailedTextBackgroundColor;
                    break;
                default:
                    textBackgroundColor = outgoingSendingTextBackgroundColor;
            }

            authorDisplayView.setColor(outgoingAuthorBackgroundColor);
            authorDisplayView.setTextColor(outgoingAuthorTextColor);

            ((GradientDrawable) bodyView.getBackground()).setColor(textBackgroundColor);
            bodyView.setTextColor(outgoingTextColor);
        } else {
            authorDisplayView.setColor(incomingAuthorBackgroundColor);
            authorDisplayView.setTextColor(incomingAuthorTextColor);

            ((GradientDrawable) bodyView.getBackground()).setColor(incomingTextBackgroundColor);
            bodyView.setTextColor(incomingTextColor);
        }

        setPosition(getPosition());
    }

    private int getTimeSensitiveTopPadding() {
        if (TimeHelper.isOlderThanBy(info.getDate(), prevInfo.getDate(), 2 * TimeHelper.MINUTE)) {
            return containerPaddingExpanded;
        } else {
            return containerPaddingMinimized;
        }
    }

    private void setContainerTopAndBottomPadding(int topPadding, int bottomPadding) {
        container.setPadding(container.getPaddingLeft(),
                topPadding,
                container.getPaddingRight(),
                bottomPadding);
    }

    private Position getPosition() {
        Position position;
        if (prevInfo == null) {
            if (nextInfo == null) {
                // prev: null, next: null
                position = Position.ONLY;
            } else {
                if (info.hasSameAuthor(nextInfo)) {
                    // prev: null, next: same
                    position = Position.TOP;
                } else {
                    // prev: null, next: diff
                    position = Position.ONLY;
                }
            }
        } else {
            if (nextInfo == null) {
                if (info.hasSameAuthor(prevInfo)) {
                    // prev: same, next: null
                    position = Position.BOTTOM;
                } else {
                    // prev: diff, next: null
                    position = Position.ONLY;
                }
            } else {
                if (info.hasSameAuthor(prevInfo)) {
                    if (info.hasSameAuthor(nextInfo)) {
                        // prev: same, next: same
                        position = Position.MIDDLE;
                    } else {
                        // prev: same, next: diff
                        position = Position.BOTTOM;
                    }
                } else {
                    if (info.hasSameAuthor(nextInfo)) {
                        // prev: diff, next: same
                        position = Position.TOP;
                    } else {
                        // prev: diff, next: diff
                        position = Position.ONLY;
                    }
                }
            }
        }
        return position;
    }

    private void setPosition(Position position) {
        switch (position) {
            case TOP:
                authorFullNameView.setVisibility(VISIBLE);
                authorDisplayView.setVisibility(INVISIBLE);
                setContainerTopAndBottomPadding(containerPadding, containerPaddingMinimized);
                break;
            case MIDDLE:
                authorFullNameView.setVisibility(GONE);
                authorDisplayView.setVisibility(INVISIBLE);
                setContainerTopAndBottomPadding(getTimeSensitiveTopPadding(), containerPaddingMinimized);
                break;
            case BOTTOM:
                authorFullNameView.setVisibility(GONE);
                authorDisplayView.setVisibility(VISIBLE);
                setContainerTopAndBottomPadding(getTimeSensitiveTopPadding(), containerPadding);
                break;
            case ONLY:
                authorFullNameView.setVisibility(VISIBLE);
                authorDisplayView.setVisibility(VISIBLE);
                setContainerTopAndBottomPadding(containerPadding, containerPadding);
        }
    }

    // Where does this MessageView lay in relation to other Messages sent by the author?
    // Example 1: if the user sends four itemViewInfos, the positions will be:
    // TOP, MIDDLE, MIDDLE, BOTTOM.
    // Example 2: if the user sends two itemViewInfos:
    // TOP, BOTTOM.
    // Example 3: if the user sends one message:
    // ONLY
    public enum Position {
        TOP, MIDDLE, BOTTOM, ONLY
    }
}