package com.tonyjhuang.cheddar.ui.chat.chatevent;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;

import com.tonyjhuang.cheddar.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.res.ColorRes;

/**
 * Created by tonyjhuang on 4/1/16.
 */
@EViewGroup(R.layout.row_chat_message_right)
public class OutgoingMessageView extends MessageView {

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

    public OutgoingMessageView(Context context) {
        super(context);
    }

    @Override
    public void updateViews() {
        super.updateViews();

        int textBackgroundColor;
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
    }
}
