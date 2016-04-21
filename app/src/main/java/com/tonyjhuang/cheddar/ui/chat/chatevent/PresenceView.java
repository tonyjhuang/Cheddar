package com.tonyjhuang.cheddar.ui.chat.chatevent;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * View representation of a Presence ParseChatEvent.
 */
@EViewGroup(R.layout.row_chat_presence)
public class PresenceView extends FrameLayout {

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

    public void setPresenceInfo(ChatEventViewInfo info) {
        text.setText(info.chatEvent.body().toUpperCase());
    }
}
