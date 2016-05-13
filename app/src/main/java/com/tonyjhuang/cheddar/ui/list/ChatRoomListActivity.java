package com.tonyjhuang.cheddar.ui.list;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

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

    @ViewById(R.id.debug_email)
    TextView debugEmailView;

    @Bean(ChatRoomListPresenterImpl.class)
    ChatRoomListPresenter presenter;

    @Bean
    ChatRoomListAdapter adapter;

    /**
     * Loading indicator to display while performing a long running process.
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

    @Override
    public void displayUserEmail(String email) {
        debugEmailView.setText(email);
    }

    @ItemClick(R.id.room_list_view)
    public void onChatRoomItemLongClick(ChatRoomInfo info) {
        navigateToChatView(info.alias().objectId());
    }

    @Override
    public void navigateToChatView(String aliasId) {
        dismissLoadingDialog();
        ChatActivity_.intent(this).aliasId(aliasId).start();
    }

    @Override
    public void showJoinChatError() {
        dismissLoadingDialog();
        showToast(R.string.list_error_join_chat);
    }

    @Override
    public void showGetListError() {
        showToast(R.string.list_error_get_list);
    }

    @Override
    public void showLogoutError() {
        dismissLoadingDialog();
        showToast(R.string.list_error_logout);
    }

    @Override
    public void navigateToSignUpView() {
        OnboardActivity_.intent(this).start();
        finish();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null) loadingDialog.dismiss();

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
            case R.id.action_logout:
                loadingDialog = LoadingDialog.show(this, R.string.list_logout);
                presenter.logout();
                return true;
            case R.id.action_clear:
                presenter.debugReset();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
