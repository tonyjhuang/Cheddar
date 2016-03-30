package com.tonyjhuang.cheddar.ui.chat;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;

import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.CheddarMetricTracker;
import com.tonyjhuang.cheddar.api.models.Alias;
import com.tonyjhuang.cheddar.api.models.ChatEvent;
import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.ui.customviews.ClickableTitleToolbar;
import com.tonyjhuang.cheddar.ui.customviews.LoadingDialog;
import com.tonyjhuang.cheddar.ui.customviews.PreserveScrollStateListView;
import com.tonyjhuang.cheddar.ui.main.MainActivity_;
import com.tonyjhuang.cheddar.ui.utils.FeedbackDialogHelper;
import com.tonyjhuang.cheddar.ui.utils.StringUtils;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;

/**
 * Created by tonyjhuang on 2/18/16.
 */
@EActivity(R.layout.activity_chat)
public class ChatActivity extends CheddarActivity implements ChatRoomView {

    private static final String TAG = ChatActivity.class.getSimpleName();

    @ViewById(R.id.toolbar)
    ClickableTitleToolbar toolbar;

    @ViewById(R.id.message_list_view)
    PreserveScrollStateListView chatEventListView;

    @ViewById(R.id.message_input)
    EditText messageInput;

    @ViewById(R.id.send_message_container)
    SendMessageImageOverlay sendMessageView;

    @Bean(ChatRoomPresenterImpl.class)
    ChatRoomPresenter presenter;

    @Extra
    String aliasId;

    @Bean
    CheddarApi api;

    @Pref
    CheddarPrefs_ prefs;

    /**
     * Data adapter for our ChatEvents.
     */
    private ChatEventListAdapter adapter;

    /**
     * Have we retrieved our initial list of ChatEvents yet?
     */
    private boolean firstLoad = true;

    /**
     * The current list of active Aliases in this Chat Room.
     */
    private List<Alias> activeAliases;
    private String currentUserId;

    /**
     * Loading dialog for when the user is leaving the chatroom.
     */
    private LoadingDialog leaveChatRoomDialog;

    @AfterInject
    public void afterInject() {
        presenter.setView(this);
        presenter.setAliasId(aliasId);
        presenter.loadMoreMessages();
    }

    @AfterViews
    public void updateViews() {
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.chat_title_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void displayActiveAliasesDialog() {
        if (activeAliases != null) {
            AliasListDialog.show(this, activeAliases, currentUserId);
        }
    }

    @Override
    public void displayActiveAliases(List<Alias> aliases, String currentUserId) {
        assert getSupportActionBar() != null;
        ActionBar actionBar = getSupportActionBar();

        int numAliases = aliases.size();
        if (numAliases == 0) {
            actionBar.setSubtitle(null);
        } else if (numAliases == 1) {
            actionBar.setSubtitle(getString(R.string.chat_no_members));
            toolbar.getSubtitleTextView().setOnClickListener(v -> showToast(R.string.chat_waiting));
        } else {
            actionBar.setSubtitle(getString(R.string.chat_members, numAliases));
            toolbar.getTitleTextView().setOnClickListener(v -> displayActiveAliasesDialog());
            toolbar.getSubtitleTextView().setOnClickListener(v -> displayActiveAliasesDialog());
        }
        activeAliases = aliases;
        this.currentUserId = currentUserId;
    }

    @Click(R.id.send_message)
    public void onSendMessageClick() {
        sendMessage();
    }

    @AfterTextChange(R.id.message_input)
    void onMessageInputTextChange(Editable text) {
        sendMessageView.setDisabled(text.length() == 0);
    }

    @EditorAction(R.id.message_input)
    boolean onMessageInputEditorAction(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_NULL) {
            sendMessage();
            return true;
        }
        return false;
    }

    /**
     * Do basic message validation before handing off to presenter.
     */
    private void sendMessage() {
        String body = messageInput.getText().toString().trim();
        if (body.isEmpty()) return;

        messageInput.setText("");
        presenter.sendMessage(body);
        Log.e(TAG, "sending message: " + body);
    }

    @Override
    public void displayNewChatEvents(String currentUserId, List<ChatEvent> chatEvents) {
        setUpChatEventListView(currentUserId);
        chatEventListView.pauseDrawing();
        for (ChatEvent chatEvent : chatEvents) {
            adapter.addOrUpdateMessageEvent(chatEvent, true);
        }

        chatEventListView.animate().alpha(1f).setDuration(250);
        chatEventListView.post(() -> {
            chatEventListView.setSelection(adapter.getCount());
            chatEventListView.resumeDrawing();
        });
    }

    @Override
    public void displayOldChatEvents(String currentUserId, List<ChatEvent> chatEvents) {
        setUpChatEventListView(currentUserId);
        chatEventListView.saveScrollStateAndPauseDrawing();
        for (ChatEvent chatEvent : chatEvents) {
            adapter.addOrUpdateMessageEvent(chatEvent, false);
        }
        chatEventListView.post(() -> {
            if (firstLoad) {
                // Scroll to the bottom of the list on first load to show
                // the newest messages first.
                firstLoad = false;
                chatEventListView.setSelection(adapter.getCount() - 1);
                chatEventListView.resumeDrawing();
            } else {
                chatEventListView.restoreScrollStateAndResumeDrawing();
            }
            chatEventListView.animate().alpha(1f).setDuration(250);
        });
    }

    /**
     * Basic set up for our ChatEvent views.
     */
    private void setUpChatEventListView(String currentUserId) {
        if (adapter != null) return;
        adapter = new ChatEventListAdapter(currentUserId);
        chatEventListView.setAdapter(adapter);
        chatEventListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) presenter.loadMoreMessages();
            }
        });
    }

    @Override
    public void displayLoadHistoryChatEventsError() {
        showToast("Failed to load more messages");
        chatEventListView.setAlpha(1f);
    }

    @Override
    public void displayPlaceholderMessage(Message message) {
        adapter.addPlaceholderMessage(message);
        chatEventListView.post(() -> chatEventListView.setSelection(adapter.getCount() - 1));
    }

    @Override
    public void notifyPlaceholderMessageFailed(Message placeholder) {
        adapter.notifyFailed(placeholder);
        showToast(R.string.chat_message_failed);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                /**
                 * TODO: REMOVE THIS FOR FINAL RELEASE. IN BETA, THE BACK BUTTON WILL CAUSE
                 * TODO: THE USER TO LEAVE THE CHATROOM.
                 */
                promptToLeaveChatRoom();
                return true;
            case R.id.action_feedback:
                FeedbackDialogHelper.getFeedback(this, (name, feedback) -> {
                    if (feedback != null && !feedback.isEmpty()) {
                        showToast(R.string.feedback_thanks);
                        presenter.sendFeedback(name, feedback);
                    }
                });
                return true;
            case R.id.action_report:
                showToast(R.string.report_coming_soon);
                CheddarMetricTracker.trackReportUser(CheddarMetricTracker.ReportUserLifecycle.CLICKED);
                Log.d(TAG, "report");
                return true;
            case R.id.action_leave:
                promptToLeaveChatRoom();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void promptToLeaveChatRoom() {
        new AlertDialog.Builder(this)
                .setTitle("Leave group chat")
                .setMessage("Are you sure?")
                .setPositiveButton(R.string.chat_leave_confirm, ((dialog, which) -> leaveChatRoom()))
                .setNegativeButton(R.string.chat_leave_cancel, null)
                .show();
    }

    /**
     * Removes the user from this ChatRoom.
     */
    private void leaveChatRoom() {
        presenter.leaveChatRoom(this);
        leaveChatRoomDialog = LoadingDialog.show(this, R.string.chat_leave_chat);
    }

    @Override
    public void navigateToListView() {
        if (leaveChatRoomDialog != null) {
            leaveChatRoomDialog.dismiss();
        }
        MainActivity_.intent(this).start();
        finish();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }
}
