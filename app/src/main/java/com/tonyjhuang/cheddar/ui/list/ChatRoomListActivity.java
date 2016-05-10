package com.tonyjhuang.cheddar.ui.list;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.ui.chat.ChatActivity_;
import com.tonyjhuang.cheddar.ui.dialog.LoadingDialog;
import com.tonyjhuang.cheddar.ui.onboard.OnboardActivity_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
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

    @Bean
    ChatRoomListAdapter adapter;
    /**
     * Loading indicator to display while joining a new ChatRoom.
     */
    private LoadingDialog loadingDialog;

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
    public void displayList(List<ChatRoomInfo> infoList, String currentUserId) {
        adapter.setCurrentUserId(currentUserId);
        if (listView.getAdapter() == null) {
            listView.setAdapter(adapter);
        }
        adapter.setInfoList(infoList);

    }

    @ItemClick(R.id.room_list_view)
    public void onChatRoomItemLongClick(ChatRoomInfo info) {
        navigateToChatView(info.alias().objectId());
    }

    @Override
    public void navigateToChatView(String aliasId) {
        if (loadingDialog != null) loadingDialog.dismiss();
        ChatActivity_.intent(this).aliasId(aliasId).start();
    }

    @Override
    public void showJoinChatError() {
        if (loadingDialog != null) loadingDialog.dismiss();
        showToast(R.string.list_error_join_chat);
    }

    @Override
    public void showGetListError() {
        showToast(R.string.list_error_get_list);
    }

    @Override
    public void navigateToSignUpView() {
        OnboardActivity_.intent(this).start();
        finish();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_join:
                loadingDialog = LoadingDialog.show(this, R.string.chat_join_chat);
                presenter.onJoinChatRoomClicked();
                return true;
            case R.id.action_clear:
                presenter.debugReset();
        }
        return super.onOptionsItemSelected(item);
    }


}
