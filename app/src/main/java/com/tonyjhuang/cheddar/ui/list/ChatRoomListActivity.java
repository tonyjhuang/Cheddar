package com.tonyjhuang.cheddar.ui.list;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.tonyjhuang.cheddar.BuildConfig;
import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.ui.chat.ChatActivity_;
import com.tonyjhuang.cheddar.ui.dialog.FeedbackDialog;
import com.tonyjhuang.cheddar.ui.dialog.LoadingDialog;
import com.tonyjhuang.cheddar.ui.welcome.WelcomeActivity_;
import com.tonyjhuang.cheddar.utils.Scheduler;
import com.tonyjhuang.cheddar.utils.VersionChecker;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;

import rx.Subscription;

/**
 * Displays the list of chatrooms the user is currently in.
 */
@EActivity(R.layout.activity_list)
public class ChatRoomListActivity extends CheddarActivity implements ChatRoomListView {

    @ViewById
    Toolbar toolbar;

    @ViewById(R.id.room_list_view)
    RecyclerView roomRecyclerView;

    @ViewById(R.id.debug_email)
    TextView debugEmailView;

    @Bean(ChatRoomListPresenterImpl.class)
    ChatRoomListPresenter presenter;

    @Bean
    ChatRoomListAdapter adapter;

    @Bean
    VersionChecker versionChecker;

    @Pref
    CheddarPrefs_ prefs;

    /**
     * Loading indicator to display while performing a long running process.
     */
    private LoadingDialog loadingDialog;

    private Subscription onClickSubscription;

    @AfterInject
    public void afterInject() {
        presenter.setView(this);
    }

    @AfterViews
    public void afterViews() {
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.list_title);
        roomRecyclerView.setHasFixedSize(true);
        roomRecyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        };
        roomRecyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void displayList(List<ChatRoomInfo> infoList, String currentUserId) {
        adapter.setCurrentUserId(currentUserId);
        if (roomRecyclerView.getAdapter() == null) {
            roomRecyclerView.setAdapter(adapter);
            onClickSubscription = adapter.getOnClickObservable()
                    .compose(Scheduler.defaultSchedulers())
                    .subscribe(chatRoomInfo -> navigateToChatView(chatRoomInfo.alias().objectId()));
        }
        adapter.setInfoList(infoList);
        invalidateOptionsMenu();
    }

    @Override
    public void displayUserEmail(String email) {
        debugEmailView.setText(email);
    }

    @Override
    public void navigateToChatView(String aliasId) {
        dismissLoadingDialog();
        ChatActivity_.intent(this).aliasId(aliasId).start();
    }

    @Override
    public void showJoinChatError() {
        dismissLoadingDialog();
        showToast(R.string.list_error_join_chat_failed);
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
        WelcomeActivity_.intent(this).start();
        finish();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null) loadingDialog.dismiss();

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForUpdate(versionChecker);
        presenter.onResume();
        showChangeLog(prefs);
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
        if (onClickSubscription != null)
            onClickSubscription.unsubscribe();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem joinChatRoomView = menu.findItem(R.id.action_join);
        // set your desired icon here based on a flag if you like
        if (adapter != null && adapter.getItemCount() >= CheddarApi.MAX_CHAT_ROOMS) {
            joinChatRoomView.setIcon(getResources().getDrawable(R.drawable.action_join_disabled));
        } else {
            joinChatRoomView.setIcon(getResources().getDrawable(R.drawable.action_join));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        boolean isDebug = BuildConfig.BUILD_TYPE.equals("debug");
        menu.findItem(R.id.action_debug_clear).setVisible(isDebug);
        return true;
    }

    private void joinNewChatRoom() {
        //JoinChatActivity_.intent(this).start();
        loadingDialog = LoadingDialog.show(this, R.string.chat_join_chat);
        presenter.onJoinChatRoomClicked();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_feedback:
                FeedbackDialog.getFeedback(this, feedback -> {
                    showToast(R.string.feedback_thanks);
                    presenter.sendFeedback(feedback);
                });
                return true;
            case R.id.action_join:
                if (adapter != null) {
                    if (adapter.getItemCount() < CheddarApi.MAX_CHAT_ROOMS) {
                        joinNewChatRoom();
                    } else {
                        showToast(R.string.list_error_join_chat_too_many);
                    }
                }
                return true;
            case R.id.action_logout:
                loadingDialog = LoadingDialog.show(this, R.string.list_logout);
                presenter.logout();
                return true;
            case R.id.action_debug_clear:
                presenter.debugReset();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
