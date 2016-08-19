package com.tonyjhuang.cheddar.ui.list;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.background.UnreadMessagesCounter;
import com.tonyjhuang.cheddar.ui.customviews.AliasDisplayView;
import com.tonyjhuang.cheddar.utils.TimestampUtils;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;


/**
 * Created by tonyjhuang on 4/5/16.
 */

@EViewGroup(R.layout.row_list_room)
public class ChatRoomItemView extends RelativeLayout {

    private static Typeface readTypeface;
    private static Typeface unreadTypeface;

    @ViewById(R.id.alias_display)
    AliasDisplayView aliasDisplayView;

    @ViewById(R.id.group_name)
    TextView groupNameView;

    @ViewById(R.id.timestamp)
    TextView timestampView;

    @ViewById(R.id.recent_message)
    TextView recentMessageView;

    @Bean
    UnreadMessagesCounter unreadMessagesCounter;

    public ChatRoomItemView(Context context) {
        super(context);
        if (readTypeface == null || unreadTypeface == null) {
            readTypeface = Typeface.createFromAsset(context.getAssets(), "Effra-Regular.ttf");
            unreadTypeface = Typeface.createFromAsset(context.getAssets(), "Effra-Medium.ttf");
        }
    }

    public void setChatRoomInfo(ChatRoomInfo info, String currentUserId) {
        recentMessageView.setText(info.chatEvent().displayBody());
        timestampView.setText(TimestampUtils.formatDate(info.chatEvent().updatedAt()));
        groupNameView.setText(info.chatRoom().displayName());

        aliasDisplayView.setAlias(info.chatEvent().alias(),
                currentUserId.equals(info.chatEvent().alias().userId()));

        if (unreadMessagesCounter.get(info.chatRoom().objectId()) != 0) {
            aliasDisplayView.showUnreadMessageIndicator(true);
            groupNameView.setTypeface(unreadTypeface);
        } else {
            aliasDisplayView.showUnreadMessageIndicator(false);
            groupNameView.setTypeface(readTypeface);
        }
    }

}
