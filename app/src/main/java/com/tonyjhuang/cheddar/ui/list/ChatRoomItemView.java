package com.tonyjhuang.cheddar.ui.list;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.ui.customviews.AliasDisplayView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.joda.time.DateTime;

import java.util.Date;


/**
 * Created by tonyjhuang on 4/5/16.
 */

@EViewGroup(R.layout.row_list_room)
public class ChatRoomItemView extends RelativeLayout {

    @ViewById(R.id.alias_display)
    AliasDisplayView aliasDisplayView;

    @ViewById(R.id.group_name)
    TextView groupNameView;

    @ViewById(R.id.timestamp)
    TextView timestampView;

    @ViewById(R.id.recent_message)
    TextView recentMessageView;

    @ColorRes(R.color.chat_author_background_incoming)
    int aliasDisplayColor;

    public ChatRoomItemView(Context context) {
        super(context);
    }

    @AfterViews
    public void afterViews() {
        aliasDisplayView.setColor(aliasDisplayColor);
    }

    public void setChatRoomInfo(ChatRoomInfo info) {
        recentMessageView.setText(info.chatEvent().displayBody());
        aliasDisplayView.setAliasName(info.alias().name());
        timestampView.setText(formatDate(info.chatEvent().updatedAt()));
    }

    /**
     * Turn a date into its string representation. Rules:
     * - If a date is part of today, display hour:minute.
     * - If a date is within 3 days of today, display the day of week.
     * - Otherwise, display month day
     */
    private String formatDate(Date date) {
        DateTime dateTime = new DateTime(date);
        DateTime midnight = new DateTime().withTimeAtStartOfDay();
        if (dateTime.isAfter(midnight)) {
            // 4:30 PM
            return removeLeadingZero(dateTime.toString("hh:mm a"));
        } else {
            DateTime threeDaysAgo = midnight.minusDays(3);
            if (dateTime.isAfter(threeDaysAgo)) {
                // Wed
                return dateTime.toString("EEE");
            } else {
                // Jan 7
                return dateTime.toString("MMM ") + removeLeadingZero(dateTime.toString("dd"));
            }
        }
    }

    private String removeLeadingZero(String string) {
        if (string.substring(0, 1).equals("0")) {
            string = string.substring(1, string.length());
        }
        return string;
    }
}
