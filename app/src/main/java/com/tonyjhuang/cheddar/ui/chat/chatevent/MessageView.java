package com.tonyjhuang.cheddar.ui.chat.chatevent;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.ui.customviews.AliasDisplayView;
import com.tonyjhuang.cheddar.utils.TimeUtils;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DimensionPixelSizeRes;
import org.apache.commons.lang3.text.WordUtils;

/**
 * View representation of a Presence ParseChatEvent.
 */
@EViewGroup
public abstract class MessageView extends RelativeLayout {

    /**
     * The ChatEventViewInfos that this MessageView is bound to.
     */
    protected ChatEventViewInfo info, prevInfo;
    @ViewById(R.id.message_container)
    RelativeLayout container;
    @ViewById(R.id.author_display)
    AliasDisplayView authorDisplayView;
    @ViewById(R.id.author_full_name)
    TextView authorFullNameView;
    @ViewById(R.id.body)
    TextView bodyView;
    @DimensionPixelSizeRes(R.dimen.chat_bubble_padding)
    int containerPadding;
    @DimensionPixelSizeRes(R.dimen.chat_bubble_padding_minimized)
    int containerPaddingMinimized;
    @DimensionPixelSizeRes(R.dimen.chat_bubble_padding)
    int containerPaddingExpanded;

    private Position position;

    public MessageView(Context context) {
        super(context);
    }

    public void setMessageInfo(ChatEventViewInfo info, ChatEventViewInfo prev, ChatEventViewInfo next) {
        this.info = info;
        this.prevInfo = prev;
        position = getPosition(info, prev, next);
        updateViews();
    }

    public void updateViews() {
        String aliasName = WordUtils.capitalizeFully(info.chatEvent.alias().name());
        authorFullNameView.setText(aliasName);
        authorDisplayView.setAliasName(aliasName);
        bodyView.setText(info.chatEvent.body());
        setPosition(position);
    }

    private Position getPosition(ChatEventViewInfo info, ChatEventViewInfo prev, ChatEventViewInfo next) {
        Position position;
        if (prev == null) {
            if (next == null) {
                // prev: null, next: null
                position = Position.ONLY;
            } else {
                if (info.hasSameAuthor(next)) {
                    // prev: null, next: same
                    position = Position.TOP;
                } else {
                    // prev: null, next: diff
                    position = Position.ONLY;
                }
            }
        } else {
            if (next == null) {
                if (info.hasSameAuthor(prev)) {
                    // prev: same, next: null
                    position = Position.BOTTOM;
                } else {
                    // prev: diff, next: null
                    position = Position.ONLY;
                }
            } else {
                if (info.hasSameAuthor(prev)) {
                    if (info.hasSameAuthor(next)) {
                        // prev: same, next: same
                        position = Position.MIDDLE;
                    } else {
                        // prev: same, next: diff
                        position = Position.BOTTOM;
                    }
                } else {
                    if (info.hasSameAuthor(next)) {
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

    private int getTimeSensitiveTopPadding() {
        if (TimeUtils.isOlderThanBy(info.getDate(), prevInfo.getDate(), 2 * TimeUtils.MINUTE)) {
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
