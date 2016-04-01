package com.tonyjhuang.cheddar.ui.chat.chateventview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.models.Presence;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * View representation of a Presence ChatEvent.
 */
@EViewGroup(R.layout.stub_presence_view)
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

    public void setPresenceInfo(PresenceChatEventViewInfo info) {
        Presence presence = info.getPresence();
        String aliasName = presence.getAlias().getName();
        String presenceText;

        switch (presence.getAction()) {
            case JOIN:
                presenceText = getContext().getString(R.string.chat_presence_joined, aliasName);
                text.setVisibility(View.VISIBLE);
                break;
            case LEAVE:
                presenceText = getContext().getString(R.string.chat_presence_left, aliasName);
                text.setVisibility(View.VISIBLE);
                break;
            default:
                presenceText = "";
                text.setVisibility(GONE);
        }
        text.setText(presenceText.toUpperCase());
    }
}
