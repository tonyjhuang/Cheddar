package com.tonyjhuang.cheddar.ui.list;

import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.models.ChatRoomInfo;
import com.tonyjhuang.cheddar.ui.chat.ChatActivity_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Displays the list of chatrooms the user is currently in.
 */
@EActivity(R.layout.activity_list)
public class ChatRoomListActivity extends CheddarActivity implements ChatRoomListView {

    @ViewById
    Toolbar toolbar;

    @ViewById(R.id.room_list_view)
    ListView listView;

    @Bean(ChatRoomListPresenterImpl.class)
    ChatRoomListPresenter presenter;

    private ChatRoomListAdapter adapter;

    @AfterInject
    public void afterInject() {
        presenter.setView(this);
    }

    @AfterViews
    public void afterViews() {
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.list_title);

    }

    @Override
    public void displayList(List<ChatRoomInfo> infoList) {
        if (adapter == null) {
            adapter = new ChatRoomListAdapter(infoList);
            listView.setAdapter(adapter);
        }
    }

    @ItemClick(R.id.room_list_view)
    public void onChatRoomItemLongClick(ChatRoomInfo info) {
        navigateToChatView(info.alias.getObjectId());
    }

    private void navigateToChatView(String aliasId) {
        ChatActivity_.intent(this).aliasId(aliasId).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
