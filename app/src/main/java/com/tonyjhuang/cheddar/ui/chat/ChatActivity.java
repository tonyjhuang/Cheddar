package com.tonyjhuang.cheddar.ui.chat;

import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;

import com.parse.ParseUser;
import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.models.Alias;
import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.MessageEvent;
import com.tonyjhuang.cheddar.api.models.SendMessageImageOverlay;
import com.tonyjhuang.cheddar.ui.main.MainActivity_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import rx.Observable;

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
    ListView messageListView;

    @ViewById(R.id.message_input)
    EditText messageInput;

    @ViewById(R.id.send_message_container)
    SendMessageImageOverlay sendMessageView;

    @Extra
    String aliasId;

    @Bean
    CheddarApi api;

    private MessageEventListAdapter adapter;

    private Alias currentAlias;

    private boolean reachedEndOfMessages = false;
    private boolean loadingMoreMessages = false;

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

    private void loadMoreMessages() {
        if (loadingMoreMessages || reachedEndOfMessages) return;
        loadingMoreMessages = true;
        subscribe(api.replayMessageEvents(currentAlias.getObjectId(), REPLAY_COUNT),
                messageEvents -> {
                    reachedEndOfMessages = messageEvents.size() < REPLAY_COUNT;
                    loadingMoreMessages = false;
                    for (MessageEvent messageEvent : messageEvents) {
                        // We should have an addToEnd here.
                        adapter.addOrUpdateMessageEvent(messageEvent);
                    }
                },
                error -> {
                    Log.e(TAG, error.toString());
                    showToast("Failed to load more messages");
                    reachedEndOfMessages = true;
                    loadingMoreMessages = false;
                },
                () -> loadingMoreMessages = false);
    }

    private void sendMessage() {
        String body = messageInput.getText().toString().trim();

        if (body.isEmpty()) {
            return;
        }
        messageInput.setText("");

        Log.e(TAG, "sending message: " + body);

        subscribe(getCurrentAlias(), alias -> {
            Log.e(TAG, "got alias, creating placeholder");
            Message placeholder = Message.createPlaceholderMessage(alias, body);
            adapter.addPlaceholderMessage(placeholder);
        });

        subscribe(api.sendMessage(aliasId, body),
                aVoid -> {
                },
                (throwable) -> subscribe(getCurrentAlias(), alias -> {
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
                Log.d(TAG, "home");
                return true;
            case R.id.action_feedback:
                Log.d(TAG, "feedback");
                return true;
            case R.id.action_report:
                Log.d(TAG, "report");
                return true;
            case R.id.action_leave:
                subscribe(leaveChatRoom(), alias -> {
                    MainActivity_.intent(this).start();
                });
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    // Removes user from the current ChatRoom and unregisters the device for push notifications.
    private Observable<Alias> leaveChatRoom() {
        return getGcmRegistrationToken()
                .flatMap(token -> api.unregisterForPushNotifications(currentAlias.getObjectId(), token))
                .flatMap(success -> api.endMessageStream(aliasId))
                .flatMap(success -> api.leaveChatRoom(currentAlias.getObjectId()));
    }
}
