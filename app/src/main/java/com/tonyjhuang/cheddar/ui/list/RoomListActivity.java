package com.tonyjhuang.cheddar.ui.list;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.ui.customviews.AliasDisplayView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

/**
 * Displays the list of chatrooms the user is currently in.
 */
@EActivity(R.layout.activity_list)
public class RoomListActivity extends CheddarActivity {

    @ViewById
    Toolbar toolbar;

    @ViewById(R.id.list_view)
    ListView listView;

    private RoomListAdapter adapter;

    @AfterViews
    public void afterViews() {
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.list_title);

        adapter = new RoomListAdapter();
        listView.setAdapter(adapter);
    }

}
