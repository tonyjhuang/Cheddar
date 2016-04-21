package com.tonyjhuang.cheddar.ui.list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;

import java.util.List;

/**
 * Created by tonyjhuang on 4/5/16.
 */
public class ChatRoomListAdapter extends BaseAdapter {

    List<ChatRoomInfo> infoList;

    public ChatRoomListAdapter(List<ChatRoomInfo> infoList) {
        this.infoList = infoList;
    }

    @Override
    public int getCount() {
        return infoList.size();
    }

    @Override
    public ChatRoomInfo getItem(int position) {
        return infoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ChatRoomItemView_.build(parent.getContext());
        }
        ChatRoomItemView view = (ChatRoomItemView) convertView;
        view.setChatRoomInfo(getItem(position));
        return view;
    }
}
