package com.tonyjhuang.cheddar.ui.chat;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;

import com.parse.ParseUser;
import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.CheddarMetricTracker;
import com.tonyjhuang.cheddar.api.CheddarParser;
import com.tonyjhuang.cheddar.api.models.Alias;
import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.MessageEvent;
import com.tonyjhuang.cheddar.api.models.Presence;
import com.tonyjhuang.cheddar.api.models.SendMessageImageOverlay;
import com.tonyjhuang.cheddar.background.CheddarGcmListenerService;
import com.tonyjhuang.cheddar.background.UnreadMessagesCounter;
import com.tonyjhuang.cheddar.ui.customviews.PreserveScrollStateListView;
import com.tonyjhuang.cheddar.ui.main.MainActivity_;
import com.tonyjhuang.cheddar.ui.utils.FeedbackDialogHelper;

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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import rx.Observable;

import static com.tonyjhuang.cheddar.api.CheddarMetricTracker.MessageLifecycle;

/**
 * Created by tonyjhuang on 2/18/16.
 */
@EActivity(R.layout.activity_chat)
public class ChatActivity extends CheddarActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();

    // Number of itemViewInfos to fetch per replay request.
    private static final int REPLAY_COUNT = 20;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @ViewById(R.id.message_list_view)
    PreserveScrollStateListView messageListView;

    @ViewById(R.id.message_input)
    EditText messageInput;

    @ViewById(R.id.send_message_container)
    SendMessageImageOverlay sendMessageView;

    @Extra
    String aliasId;

    @Bean
    CheddarApi api;

    @Bean
    FeedbackDialogHelper feedbackHelper;

    @Bean
    UnreadMessagesCounter unreadMessagesCounter;

    @Pref
    CheddarPrefs_ prefs;

    private MessageEventListAdapter adapter;
    private Alias currentAlias;
    private boolean reachedEndOfMessages = false;
    private boolean loadingMoreMessages = false;
    private boolean firstLoad = true;

    /**
     * Catches com.tonyjhuang.cheddar.MESSAGE_EVENT broadcasts and aborts
     * them if they match this activity's chatroom id.
     */
    private BroadcastReceiver gcmBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "activity on receive!");
            try {
                MessageEvent messageEvent = CheddarParser.parseMessageEvent(
                        new JSONObject(intent.getStringExtra("payload")));
                switch (messageEvent.getType()) {
                    case MESSAGE:
                        handleMessage((Message) messageEvent);
                        break;
                    case PRESENCE:
                        handlePresence((Presence) messageEvent);
                        break;
                }
            } catch (JSONException | CheddarParser.UnrecognizedParseException e) {
                Log.e(TAG, "couldn't parse gcm payload into MessageEvent: " + intent.getStringExtra("payload"));
                abortBroadcast();
            }
        }

        private void handlePresence(Presence presence) {
            if (currentAlias != null &&
                    presence.getAlias().getChatRoomId().equals(currentAlias.getChatRoomId())) {
                Log.d(TAG, "message event matches current chatroom id");
                abortBroadcast();
            }
        }

        private void handleMessage(Message message) {
            if (currentAlias != null &&
                    message.getAlias().getChatRoomId().equals(currentAlias.getChatRoomId())) {
                Log.d(TAG, "message event matches current chatroom id");
                abortBroadcast();
            }
        }
    };

    @AfterViews
    public void updateViews() {
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.chat_title_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        subscribe(api.getAlias(aliasId)
                .doOnNext(alias -> currentAlias = alias)
                .flatMap(alias -> api.getCurrentUser())
                .map(ParseUser::getObjectId), id -> {
            adapter = new MessageEventListAdapter(id);
            messageListView.setAdapter(adapter);
            loadMoreMessages();
        });

        messageListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    loadMoreMessages();
                }
            }
        });
    }

    @AfterInject
    public void init() {
        // Register for push notifications
        if (checkPlayServices()) {
            subscribe(getGcmRegistrationToken()
                            .flatMap((token) -> api.registerForPushNotifications(aliasId, token)),
                    (response) -> Log.d(TAG, response.toString()),
                    (error) -> {
                        Log.e(TAG, error.toString());
                        showToast(R.string.chat_gcm_registration_failed);
                    });
        } else {
            showToast(R.string.chat_play_services_missing);
        }
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
     * Retrieve older messages from the api and display in them in the list.
     */
    private void loadMoreMessages() {
        if (loadingMoreMessages || reachedEndOfMessages || currentAlias == null) return;
        loadingMoreMessages = true;

        subscribe(api.replayMessageEvents(currentAlias.getObjectId(), REPLAY_COUNT),
                messageEvents -> {
                    reachedEndOfMessages = messageEvents.size() < REPLAY_COUNT;

                    messageListView.saveScrollStateAndPauseDrawing();
                    for (MessageEvent messageEvent : messageEvents) {
                        adapter.addOrUpdateMessageEvent(messageEvent, false);
                    }

                    if (firstLoad) {
                        // On first load we should scroll to the very bottom of the messages.
                        firstLoad = false;
                        messageListView.animate().alpha(1f).setDuration(250);
                        messageListView.post(() -> {
                            messageListView.setSelection(adapter.getCount());
                            messageListView.restoreScrollStateAndResumeDrawing();
                            loadingMoreMessages = false;
                        });
                    } else {
                        Log.d(TAG, "adapter count: " + adapter.getCount());
                        // Give the adapter the chance to populate all of our items before
                        // loading more messages.
                        messageListView.post(() -> {
                            messageListView.restoreScrollStateAndResumeDrawing();
                            loadingMoreMessages = false;
                        });
                    }
                },
                error -> {
                    Log.e(TAG, error.toString());
                    showToast("Failed to load more messages");
                    reachedEndOfMessages = true;
                    loadingMoreMessages = false;
                    messageListView.setAlpha(1f);
                });
    }

    private void sendMessage() {
        String body = messageInput.getText().toString().trim();

        if (body.isEmpty()) {
            return;
        }
        messageInput.setText("");

        Log.e(TAG, "sending message: " + body);

        /**
         * TODO: Need synchronous network requests. Sometimes, user tries to send a message before
         * TODO: we're subsribed to the message stream
         */
        subscribe(getCurrentAlias(), alias -> {
            Log.e(TAG, "got alias, creating placeholder");
            Message placeholder = Message.createPlaceholderMessage(alias, body);
            adapter.addPlaceholderMessage(placeholder);
            messageListView.post(() -> messageListView.setSelection(adapter.getCount()));
            CheddarMetricTracker.trackSendMessage(alias.getChatRoomId(), MessageLifecycle.SENT);
        });

        subscribe(api.sendMessage(aliasId, body),
                aVoid -> CheddarMetricTracker.trackSendMessage(currentAlias.getChatRoomId(), MessageLifecycle.DELIVERED),
                (throwable) -> subscribe(getCurrentAlias(), alias -> {
                    CheddarMetricTracker.trackSendMessage(currentAlias.getChatRoomId(), MessageLifecycle.FAILED);
                    Message placeholder = Message.createPlaceholderMessage(alias, body);
                    adapter.notifyFailed(placeholder);
                    Log.e(TAG, throwable.toString());
                    showToast(R.string.chat_message_failed);
                }));
    }

    private Observable<Alias> getCurrentAlias() {
        if (currentAlias != null) {
            Log.e(TAG, "cached alias");
            return Observable.just(currentAlias);
        } else {
            Log.e(TAG, "get alias from api");
            return api.getAlias(aliasId).doOnNext(alias -> currentAlias = alias);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resubscribe to message stream since all subscriptions are canceled
        // in onPause.
        subscribeToMessageStream();

        IntentFilter intentFilter = new IntentFilter(CheddarGcmListenerService.MESSAGE_EVENT_ACTION);
        intentFilter.setPriority(100);
        registerReceiver(gcmBroadcastReceiver, intentFilter);

        // If we haven't completed loading the initial messages by this point (possibly because the
        // user moved our app to the background while they were loading) retry here.
        if (firstLoad) {
            loadMoreMessages();
        }

        // Clear unread messages for this chatroom.
        getCurrentAlias().map(Alias::getChatRoomId)
                .doOnNext(unreadMessagesCounter::clear)
                .publish().connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        subscribe(api.endMessageStream(aliasId), aVoid -> Log.e(TAG, "unsubscribe"));
        unregisterReceiver(gcmBroadcastReceiver);
        loadingMoreMessages = false;
    }

    private void subscribeToMessageStream() {
        subscribe(api.getMessageStream(aliasId).retry(), this::handleMessageEvent,
                error -> {
                    Log.e(TAG, "Message stream error! " + error.toString());
                    subscribeToMessageStream();
                });
    }

    private void handleMessageEvent(MessageEvent messageEvent) {
        if (adapter != null) {
            adapter.addOrUpdateMessageEvent(messageEvent);
        }
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
                if (currentAlias != null) {
                    subscribe(feedbackHelper.show(this,
                            currentAlias.getUserId(), currentAlias.getChatRoomId()),
                    string -> Log.d(TAG, "success! " + string));
                }
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
        ProgressDialog leaveChatRoomDialog = ProgressDialog.show(this, "Leaving Chat..", "", false);
        api.resetReplayMessageEvents();
        subscribe(getGcmRegistrationToken()
                        .flatMap(token -> api.unregisterForPushNotifications(currentAlias.getObjectId(), token))
                        .flatMap(success -> api.endMessageStream(aliasId))
                        .flatMap(success -> api.leaveChatRoom(currentAlias.getObjectId())),
                alias -> {
                    long lengthOfStay = new Date().getTime() - alias.getCreatedAt().getTime();
                    CheddarMetricTracker.trackLeaveChatRoom(alias.getChatRoomId(), lengthOfStay);
                    leaveChatRoomDialog.dismiss();
                    prefs.activeAlias().put(null);
                    MainActivity_.intent(this).start();
                });
    }
}
