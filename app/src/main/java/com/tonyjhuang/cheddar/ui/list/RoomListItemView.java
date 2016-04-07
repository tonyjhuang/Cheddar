package com.tonyjhuang.cheddar.ui.list;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.ui.customviews.AliasDisplayView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

/**
 * Created by tonyjhuang on 4/5/16.
 */

@EViewGroup(R.layout.row_list_room)
public class RoomListItemView extends RelativeLayout {

    @ViewById(R.id.alias_display)
    AliasDisplayView aliasDisplayView;

    @ViewById(R.id.group_name)
    TextView groupNameView;

    @ViewById(R.id.timestamp)
    TextView timestampView;

    @ViewById(R.id.recent_message)
    TextView recentMessageView;

    @ViewById(R.id.user_alias_name)
    TextView userAliasNameView;

    @ColorRes(R.color.chat_author_background_incoming)
    int aliasDisplayColor;

    public RoomListItemView(Context context) {
        super(context);
    }

    @AfterViews
    public void afterViews() {
        aliasDisplayView.setColor(aliasDisplayColor);
    }
}
