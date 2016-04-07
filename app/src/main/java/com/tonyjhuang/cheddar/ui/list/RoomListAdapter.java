package com.tonyjhuang.cheddar.ui.list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tonyjhuang.cheddar.R;

/**
 * Created by tonyjhuang on 4/5/16.
 */
public class RoomListAdapter extends BaseAdapter {
    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            return RoomListItemView_.build(parent.getContext());
        } else {
            return convertView;
        }
    }
}
