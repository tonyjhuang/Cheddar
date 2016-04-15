package com.tonyjhuang.cheddar.ui.list;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ocpsoft.pretty.time.PrettyTime;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.models.ChatRoomInfo;
import com.tonyjhuang.cheddar.ui.customviews.AliasDisplayView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

/**
 * Created by tonyjhuang on 4/5/16.
 */

@EViewGroup(R.layout.row_list_room)
public class ChatRoomItemView extends RelativeLayout {

    private static final PrettyTime prettyTime = new PrettyTime();

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

    private ChatRoomInfo info;

    public ChatRoomItemView(Context context) {
        super(context);
    }

    @AfterViews
    public void afterViews() {
        aliasDisplayView.setColor(aliasDisplayColor);
    }

    public void setChatRoomInfo(ChatRoomInfo info) {
        this.info = info;
        recentMessageView.setText(info.chatEvent.getBody());
        aliasDisplayView.setAliasName(info.alias.getName());
        timestampView.setText(prettyTime.format(info.chatEvent.getUpdatedAt()));
    }
}
