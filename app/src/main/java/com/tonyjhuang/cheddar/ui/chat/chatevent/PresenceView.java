package com.tonyjhuang.cheddar.ui.chat.chatevent;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.utils.TimestampUtils;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;

/**
 * View representation of a Presence ChatEvent.
 */
@EViewGroup(R.layout.row_chat_presence)
public class PresenceView extends LinearLayout implements ChatEventView{

    @ViewById
    TextView timestamp;
    @ViewById
    TextView text;

    public PresenceView(Context context) {
        super(context);
    }

    public PresenceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PresenceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setChatEventViewInfos(ChatEventViewInfo info, ChatEventViewInfo prev, ChatEventViewInfo next) {
        text.setText(info.chatEvent.body().toUpperCase());

        DateTime timestampThreshold = new DateTime().minusMinutes(20);
        boolean shouldShowTimestamp = prev == null || new DateTime(prev.getDate()).isBefore(timestampThreshold);
        if(shouldShowTimestamp) {
            timestamp.setText(TimestampUtils.formatDate(info.getDate(), true));
            timestamp.setVisibility(VISIBLE);
        } else {
            timestamp.setVisibility(GONE);
        }
    }
}
