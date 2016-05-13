package com.tonyjhuang.cheddar.ui.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.CheddarMetrics;
import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.ui.chat.chatevent.ChatEventViewInfo;
import com.tonyjhuang.cheddar.ui.customviews.ClickableTitleToolbar;
import com.tonyjhuang.cheddar.ui.customviews.PreserveScrollStateListView;
import com.tonyjhuang.cheddar.ui.dialog.FeedbackDialog;
import com.tonyjhuang.cheddar.ui.dialog.LoadingDialog;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;

import timber.log.Timber;

/**
 * Created by tonyjhuang on 2/18/16.
 */
@EActivity(R.layout.activity_chat)
public class ChatActivity extends CheddarActivity implements ChatRoomView {

    /**
     * Max number of characters allowed per message.
     */
    public static final int CHARACTER_COUNT_MAX = 250;
    @ViewById(R.id.toolbar)
    ClickableTitleToolbar toolbar;

    @ViewById(R.id.message_loading)
    ProgressBar messageLoadingView;

    @ViewById(R.id.message_list_view)
    PreserveScrollStateListView chatEventListView;

    @ViewById(R.id.message_input)
    EditText messageInput;

    @ViewById(R.id.send_message_container)
    SendMessageImageOverlay sendMessageView;

    @ViewById(R.id.network_connection_error)
    View networkConnectErrorView;

    @ViewById
    TextView version;

    @ViewById(R.id.character_count)
    TextView inputCharacterCount;

    @ViewById(R.id.new_messages)
    View newMessagesIndicator;
    @Bean(ChatRoomPresenterImpl.class)
    ChatRoomPresenter presenter;
    @Extra
    String aliasId;
    @Bean
    CheddarApi api;
    @Pref
    CheddarPrefs_ prefs;
    @SystemService
    ClipboardManager clipboardManager;

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

    /**
     * Header view for our chatEventListView that denotes new messages are
     * being loaded in from history.
     */
    private View listLoadingView;

    /**
     * The name of this ChatRoom.
     */
    private String chatRoomName;

    @AfterInject
    public void afterInject() {
        Timber.v("afterInject");
        presenter.setView(this);
        presenter.setAliasId(aliasId);
    }

    @AfterViews
    public void afterViews() {
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.chat_title_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        presenter.loadMoreMessages();
        version.setText(getVersionName());
        messageLoadingView.postDelayed(() -> messageLoadingView.animate().alpha(1).setDuration(250), 500);
    }

    @Override
    public void displayChatRoomName(String chatRoomName) {
        this.chatRoomName = chatRoomName;
        assert getSupportActionBar() != null;
        if (chatRoomName != null && !chatRoomName.isEmpty()) {
            getSupportActionBar().setTitle(chatRoomName);
        }
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
        if (messageInput.getText().length() <= CHARACTER_COUNT_MAX) {
            sendMessage();
        }
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

    @AfterTextChange(R.id.message_input)
    void afterMessageInputTextChanged(Editable text) {
        int textLength = text.length();
        inputCharacterCount.setText(String.valueOf(textLength));
        if (textLength >= 225) {
            inputCharacterCount.animate().alpha(1).setDuration(100);
            if (textLength >= CHARACTER_COUNT_MAX) {
                inputCharacterCount.setTextColor(getResources().getColor(R.color.colorAccent));
            } else {
                inputCharacterCount.setTextColor(getResources().getColor(R.color.text_secondary));
            }
        } else {
            inputCharacterCount.animate().alpha(0).setDuration(100);
        }
    }

    /**
     * Do basic message validation before handing off to presenter.
     */
    private void sendMessage() {
        String body = messageInput.getText().toString().trim();
        if (body.isEmpty()) return;

        messageInput.setText("");
        presenter.sendMessage(body);
    }

    @Override
    public void displayNewChatEvents(String currentUserId, List<ChatEvent> chatEvents) {
        setUpChatEventListView(currentUserId);

        boolean isBottomAnchored = adapter.getCount() == 0 ||
                chatEventListView.getLastVisiblePosition() == adapter.getCount() - 1 + chatEventListView.getHeaderViewsCount();

        chatEventListView.pauseDrawing();
        adapter.addOrUpdateChatEvents(chatEvents, true);

        showListView(true);
        chatEventListView.post(() -> {
            if (isBottomAnchored) {
                chatEventListView.setSelection(adapter.getCount() - 1);
            } else {
                showNewMessagesIndicator(true);
            }
            chatEventListView.resumeDrawing();
        });
        showListView(true);
    }

    @Override
    public void displayOldChatEvents(String currentUserId, List<ChatEvent> chatEvents) {
        setUpChatEventListView(currentUserId);
        if (chatEvents.size() == 0) return;
        chatEventListView.post(() -> {
            chatEventListView.saveScrollStateAndPauseDrawing();
            adapter.addOrUpdateChatEvents(chatEvents, false);
            chatEventListView.post(() -> {
                if (firstLoad) {
                    // Scroll to the bottom of the list on first load to show
                    // the newest messages first.
                    firstLoad = false;
                    chatEventListView.setSelection(adapter.getCount() - 1 + chatEventListView.getHeaderViewsCount());
                    chatEventListView.resumeDrawing();
                    showListView(true);
                } else {
                    chatEventListView.restoreScrollStateAndResumeDrawing();
                }
                showListView(true);
            });
        });
    }

    private void showListView(boolean show) {
        chatEventListView.animate().alpha(show ? 1 : 0).setDuration(150);
        messageLoadingView.animate().alpha(0).setDuration(50);
    }

    @Override
    public void notifyEndOfChatEvents() {
        showListLoadingView(false);
    }

    private void showListLoadingView(boolean show) {
        if (listLoadingView != null) {
            listLoadingView.post(() -> listLoadingView.setVisibility(show ? View.VISIBLE : View.GONE));
        }
    }

    /**
     * Basic set up for our ChatEvent views.
     */
    private void setUpChatEventListView(String currentUserId) {
        if (adapter != null) return;

        View listLoadingHeaderView = View.inflate(this, R.layout.row_loading, null);
        chatEventListView.addHeaderView(listLoadingHeaderView);
        listLoadingView = listLoadingHeaderView.findViewById(R.id.loading);
        showListLoadingView(true);

        adapter = new ChatEventListAdapter(currentUserId);
        chatEventListView.setAdapter(adapter);
        chatEventListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            /**
             * Keep track of our last firstVisibleItem. This is two cover the
             * following two cases:
             * 1. The user is at the top of the list and scrolls around in small increments.
             *    We don't want to spam our presenter with requests and perform extraneous actions.
             * 2. The user is at the top of the list and the presenter loads more items in.
             *    Even if the new items don't change the list, we can expect our adapter to
             *    redraw. On this redraw, onScroll will be called. We don't want to cause an
             *    infinite loop, where the presenter is constantly feeding us stale data and
             *    we're constantly asking for the same stale data over and over.
             */
            private int lastFirstVisibleItem = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (lastFirstVisibleItem == firstVisibleItem) return;
                if (firstVisibleItem == 0) {
                    presenter.loadMoreMessages();
                }
                if (view.getLastVisiblePosition() == adapter.getCount() - 1 + chatEventListView.getHeaderViewsCount()
                        && newMessagesIndicator.getAlpha() > 0) {
                    showNewMessagesIndicator(false);
                }
                lastFirstVisibleItem = firstVisibleItem;
            }
        });
    }

    /**
     * Show or hide the new messages indicator.
     */
    private void showNewMessagesIndicator(boolean show) {
        newMessagesIndicator.animate().alpha(show ? .9f : 0);
    }

    @ItemLongClick(R.id.message_list_view)
    public void onChatEventItemLongClick(ChatEventViewInfo info) {
        if (info.chatEvent.type().equals(ChatEvent.ChatEventType.MESSAGE)) {
            ChatEvent message = info.chatEvent;
            ClipData clip = ClipData.newPlainText(message.alias().name() + " said:", message.body());
            clipboardManager.setPrimaryClip(clip);
            showToast(R.string.chat_copied_to_clipboard);
        }
    }

    @Override
    public void displayLoadHistoryChatEventsError() {
        showToast(R.string.chat_error_load_messages_failed);
        chatEventListView.setAlpha(1f);
    }

    @Override
    public void displayPlaceholderMessage(ChatEvent message) {
        adapter.addPlaceholderMessage(message);
        chatEventListView.post(() -> chatEventListView.setSelection(adapter.getCount() - 1));
    }

    @Override
    public void notifyPlaceholderMessageFailed(ChatEvent placeholder) {
        adapter.notifyFailed(placeholder);
        showToast(R.string.chat_error_message_failed);
    }

    @Click(R.id.new_messages)
    public void onNewMessagesClick() {
        chatEventListView.smoothScrollToPosition(adapter.getCount() - 1 + chatEventListView.getHeaderViewsCount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
        showChangeLog(prefs);
        if (chatEventListView != null &&
                adapter != null &&
                chatEventListView.getLastVisiblePosition() == adapter.getCount() - 1) {
            showNewMessagesIndicator(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
    }

    @Override
    public void displayNetworkConnectionError() {
        networkConnectErrorView.animate().alpha(1);
        showListView(true);
    }

    @Override
    public void hideNetworkConnectionError() {
        networkConnectErrorView.animate().alpha(0);
        if (firstLoad) {
            // If we're just now getting network and we haven't loaded the first
            // set of messages yet, load them now.
            presenter.loadMoreMessages();
            showListView(false);
        }
    }

    @Override
    public void displayChatRoomNameChangeError() {
        showToast(R.string.chat_error_change_chat_room_name_failed);
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
                navigateToListView();
                return true;
            case R.id.action_feedback:
                FeedbackDialog.getFeedback(this, (name, feedback) -> {
                    if (feedback != null && !feedback.isEmpty()) {
                        showToast(R.string.feedback_thanks);
                        presenter.sendFeedback(name, feedback);
                    }
                });
                return true;
            case R.id.action_name_room:
                GetChatRoomNameDialog.show(this, chatRoomName, presenter::updateChatRoomName);
                return true;
            case R.id.action_report:
                showToast(R.string.report_coming_soon);
                CheddarMetrics.trackReportUser(CheddarMetrics.ReportUserLifecycle.CLICKED);
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
        presenter.leaveChatRoom();
        leaveChatRoomDialog = LoadingDialog.show(this, R.string.chat_leave_chat);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(toolbar.getWindowToken(), 0);
    }

    @Override
    public void navigateToListView() {
        if (leaveChatRoomDialog != null) {
            leaveChatRoomDialog.dismiss();
        }
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }
}
