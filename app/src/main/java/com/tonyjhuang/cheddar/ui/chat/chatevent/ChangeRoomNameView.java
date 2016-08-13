package com.tonyjhuang.cheddar.ui.chat.chatevent;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * View representation of a CHANGE_ROOM_NAME ChatEvent.
 */
@EViewGroup(R.layout.row_chat_change_room_name)
public class ChangeRoomNameView extends FrameLayout implements ChatEventView{

    @ViewById
    TextView text;

    public ChangeRoomNameView(Context context) {
        super(context);
    }

    public ChangeRoomNameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChangeRoomNameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setChatEventViewInfos(ChatEventViewInfo info, ChatEventViewInfo prev, ChatEventViewInfo next) {
        String display = info.chatEvent.body().toUpperCase();
        String roomName = info.chatEvent.roomName();

        if (roomName != null) {
            roomName = roomName.toUpperCase();
            int indexOfName = display.indexOf(roomName);
            if (indexOfName != -1) {
                display = addNewline(display, indexOfName);
            }
            text.setText(colorSubstring(display, roomName, getResources().getColor(R.color.colorAccent)));
        } else {
            text.setText(display);
        }
    }

    public String addNewline(String string, int index) {
        if (index != -1) {
            return new StringBuilder(string).insert(index, "\n").toString();
        } else {
            return string;
        }
    }

    public Spannable colorSubstring(String string, String substring, int color) {
        SpannableStringBuilder sb = new SpannableStringBuilder(string);
        int index = string.indexOf(substring);

        if (index != -1) {
            ForegroundColorSpan fcs = new ForegroundColorSpan(color);
            sb.setSpan(fcs, index, index + substring.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        return sb;
    }
}
