package com.tonyjhuang.cheddar.ui.chat.chatevent;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.ui.customviews.AliasDisplayView;
import com.tonyjhuang.cheddar.utils.TimestampUtils;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DimensionPixelSizeRes;
import org.joda.time.DateTime;

/**
 * View representation of a Message ChatEvent.
 */
@EViewGroup
public abstract class MessageView extends LinearLayout implements ChatEventView {

    /**
     * The ChatEventViewInfos that this MessageView is bound to.
     */
    protected ChatEventViewInfo info, prevInfo, nextInfo;
    @ViewById(R.id.container)
    LinearLayout container;
    @ViewById(R.id.timestamp)
    TextView timestampView;
    @ViewById(R.id.author_display)
    AliasDisplayView authorDisplayView;
    @ViewById(R.id.author_full_name)
    TextView authorFullNameView;
    @ViewById(R.id.body)
    TextView bodyView;

    int noContainerPadding = 0;
    @DimensionPixelSizeRes(R.dimen.chat_bubble_padding_minimized)
    int containerPaddingMinimized;
    @DimensionPixelSizeRes(R.dimen.chat_bubble_padding)
    int containerPaddingExpanded;

    private Position position;

    public MessageView(Context context) {
        super(context);
    }

    public void setChatEventViewInfos(ChatEventViewInfo info, ChatEventViewInfo prev, ChatEventViewInfo next) {
        this.info = info;
        this.prevInfo = prev;
        this.nextInfo = next;
        position = getPosition(info, prev, next);
        updateViews();
    }

    public void updateViews() {
        authorFullNameView.setText(info.chatEvent.alias().displayName());
        bodyView.setText(info.chatEvent.body());
        setPosition(position);

        DateTime timestampThreshold = new DateTime(info.getDate()).minusMinutes(20);
        boolean shouldShowTimestamp = this.prevInfo == null || new DateTime(this.prevInfo.getDate()).isBefore(timestampThreshold);
        if (shouldShowTimestamp) {
            timestampView.setText(TimestampUtils.formatDate(info.getDate(), true));
            timestampView.setVisibility(VISIBLE);
        } else {
            timestampView.setVisibility(GONE);
        }
    }

    private Position getPosition(ChatEventViewInfo info, ChatEventViewInfo prev, ChatEventViewInfo next) {
        Position position;
        if (prev == null) {
            if (next == null) {
                // prev: null, next: null
                position = Position.ONLY;
            } else {
                if (info.hasSameAuthor(next) && info.isSameType(next)) {
                    // prev: null, next: same
                    position = Position.TOP;
                } else {
                    // prev: null, next: diff
                    position = Position.ONLY;
                }
            }
        } else {
            if (next == null) {
                if (info.hasSameAuthor(prev) && info.isSameType(prev)) {
                    // prev: same, next: null
                    position = Position.BOTTOM;
                } else {
                    // prev: diff, next: null
                    position = Position.ONLY;
                }
            } else {
                if (info.hasSameAuthor(prev) && info.isSameType(prev)) {
                    if (info.hasSameAuthor(next) && info.isSameType(next)) {
                        // prev: same, next: same
                        position = Position.MIDDLE;
                    } else {
                        // prev: same, next: diff
                        position = Position.BOTTOM;
                    }
                } else {
                    if (info.hasSameAuthor(next) && info.isSameType(next)) {
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
                setContainerTopAndBottomPadding(noContainerPadding, containerPaddingMinimized);
                break;
            case MIDDLE:
                authorFullNameView.setVisibility(GONE);
                authorDisplayView.setVisibility(INVISIBLE);
                setContainerTopAndBottomPadding(getTimeSensitiveTopPadding(), containerPaddingMinimized);
                break;
            case BOTTOM:
                authorFullNameView.setVisibility(GONE);
                authorDisplayView.setVisibility(VISIBLE);
                setContainerTopAndBottomPadding(getTimeSensitiveTopPadding(), noContainerPadding);
                break;
            case ONLY:
                authorFullNameView.setVisibility(VISIBLE);
                authorDisplayView.setVisibility(VISIBLE);
                setContainerTopAndBottomPadding(containerPaddingExpanded, containerPaddingExpanded);
        }
    }

    /**
     * If the previous message sent was greater than 2 minutes ago,
     * return a larger amount of top padding.
     */
    private int getTimeSensitiveTopPadding() {
        DateTime twoMinutesAgo = new DateTime(info.getDate()).minusMinutes(2);
        if (new DateTime(prevInfo.getDate()).isBefore(twoMinutesAgo)) {
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
