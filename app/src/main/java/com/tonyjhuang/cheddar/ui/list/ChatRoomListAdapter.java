package com.tonyjhuang.cheddar.ui.list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

@EBean
public class ChatRoomListAdapter extends BaseAdapter {

    private List<ChatRoomInfo> infoList = new ArrayList<>();
    private String currentUserId;

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setInfoList(List<ChatRoomInfo> infoList) {
        this.infoList = infoList;
        notifyDataSetChanged();
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
        view.setChatRoomInfo(getItem(position), currentUserId);
        return view;
    }
}
