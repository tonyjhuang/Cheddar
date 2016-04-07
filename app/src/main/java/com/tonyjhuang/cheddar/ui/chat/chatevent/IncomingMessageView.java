package com.tonyjhuang.cheddar.ui.chat.chatevent;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;

import com.tonyjhuang.cheddar.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.res.ColorRes;

/**
 * Created by tonyjhuang on 4/1/16.
 */
@EViewGroup(R.layout.row_chat_message_left)
public class IncomingMessageView extends MessageView {

    @ColorRes(R.color.chat_text_background_incoming)
    int incomingTextBackgroundColor;
    @ColorRes(R.color.chat_text_incoming)
    int incomingTextColor;
    @ColorRes(R.color.chat_author_text_incoming)
    int incomingAuthorTextColor;
    @ColorRes(R.color.chat_author_background_incoming)
    int incomingAuthorBackgroundColor;

    public IncomingMessageView(Context context) {
        super(context);
    }

    @Override
    public void updateViews() {
        super.updateViews();

        authorDisplayView.setColor(incomingAuthorBackgroundColor);
        authorDisplayView.setTextColor(incomingAuthorTextColor);

        ((GradientDrawable) bodyView.getBackground()).setColor(incomingTextBackgroundColor);
        bodyView.setTextColor(incomingTextColor);
    }
}
