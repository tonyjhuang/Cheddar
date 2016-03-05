package com.tonyjhuang.cheddar.ui.chat;

import android.app.ProgressDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;

import com.parse.ParseUser;
import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.CheddarPrefs_;
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
import org.androidannotations.annotations.sharedpreferences.Pref;

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

    @Pref
    CheddarPrefs_ prefs;

    private MessageEventListAdapter adapter;
    private Alias currentAlias;
    private boolean reachedEndOfMessages = false;
    private boolean loadingMoreMessages = false;
    private boolean firstLoad = true;

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

    /*
      // We need to save the current scroll position. Since the view's height
                    // might change after new items are added to the adapter, we need to listen
                    // to layout changes on the view and update the top when it does.
                    int savedListIndex = messageListView.getFirstVisiblePosition();
                    int newIndex = savedListIndex + messageEvents.size();
                    View child = messageListView.getChildAt(savedListIndex);
                    int childHeight = child == null ? 0 : child.getHeight();
                    int top = child == null ? 0 : child.getTop();
  if (child != null) {
                        // See http://stackoverflow.com/q/19320214/1476372
                        messageListView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                if (messageListView.getFirstVisiblePosition() == newIndex) {
                                    messageListView.getViewTreeObserver().removeOnPreDrawListener(this);
                                    return true;
                                }
                                return false;
                            }
                        });
                        child.post(() -> {
                            int newTop;
                            View newChild = adapter.getView(newIndex, null, messageListView);
                            newChild.measure(0, 0);

                            int newChildHeight = newChild.getMeasuredHeight();
                            int childHeightDifference = newChildHeight - childHeight;

                            if (childHeightDifference + top < 0) {
                                // listview item has shrunk to the point where the previous item
                                // is now visible..
                                if (newIndex == 0) {
                                    // ...but there are no items previous.
                                    newTop = 0;
                                } else {
                                    // ...so we need to update the index.
                                    View prevChild = adapter.getView(newIndex, null, messageListView);
                                    prevChild.measure(0, 0);
                                    int prevChildHeight = prevChild.getMeasuredHeight();
                                    newTop = prevChildHeight + childHeightDifference + top;
                                }
                            } else {
                                // listview item has grown or stayed the same height, update top.
                                newTop = top;
                            }
                            Log.e(TAG, "newIndex: " + newIndex + ",newTop: " + newTop);
                            messageListView.setSelectionFromTop(newIndex, newTop);
                        });
                    }

     */

    /**
     * Retrieve older messages from the api and display in them in the list.
     */
    private void loadMoreMessages() {
        if (loadingMoreMessages || reachedEndOfMessages || currentAlias == null) return;
        loadingMoreMessages = true;

        subscribe(api.replayMessageEvents(currentAlias.getObjectId(), REPLAY_COUNT),
                messageEvents -> {
                    reachedEndOfMessages = messageEvents.size() < REPLAY_COUNT;

                    int currentIndex = messageListView.getFirstVisiblePosition();
                    View view = messageListView.getChildAt(currentIndex);
                    int currentIndexTop = view == null ? 0 : view.getTop();

                    for (MessageEvent messageEvent : messageEvents) {
                        adapter.addOrUpdateMessageEvent(messageEvent, false);
                    }

                    if (firstLoad) {
                        // On first load we should scroll to the very bottom of the messages.
                        firstLoad = false;
                        messageListView.animate().alpha(1f).setDuration(250);
                        messageListView.post(() -> loadingMoreMessages = false);
                        messageListView.setSelection(adapter.getCount());
                    } else {
                        // Stop the ListView from drawing until its position is restored.
                        messageListView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                int index = messageListView.getFirstVisiblePosition();
                                int targetIndex = currentIndex + messageEvents.size();
                                // We want to either restore the list index to the last known one
                                // or one lower if the view at that index shrank and the previous item
                                // is showing.
                                if (index == targetIndex || index == targetIndex - 1) {
                                    messageListView.getViewTreeObserver().removeOnPreDrawListener(this);
                                    return true;
                                }
                                return false;
                            }
                        });
                        // Give the adapter the chance to populate all of our items before
                        // loading more messages.
                        messageListView.postDelayed(() -> loadingMoreMessages = false, 250);
                        messageListView.post(() -> {
                            messageListView.setSelectionFromTop(currentIndex + messageEvents.size(), currentIndexTop);
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

        subscribe(getCurrentAlias(), alias -> {
            Log.e(TAG, "got alias, creating placeholder");
            Message placeholder = Message.createPlaceholderMessage(alias, body);
            adapter.addPlaceholderMessage(placeholder);
            messageListView.post(() -> messageListView.setSelection(adapter.getCount()));
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

    @Override
    protected void onPause() {
        super.onPause();
        subscribe(api.endMessageStream(aliasId), aVoid -> Log.e(TAG, "unsubscribe"));
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
        ProgressDialog leaveChatRoomDialog;
        switch (id) {
            case android.R.id.home:
                leaveChatRoomDialog = ProgressDialog.show(this, "Leaving Chat..", "", false);
                subscribe(leaveChatRoom(), alias -> returnToMain(leaveChatRoomDialog));
                return true;
            case R.id.action_feedback:
                Log.d(TAG, "feedback");
                return true;
            case R.id.action_report:
                Log.d(TAG, "report");
                return true;
            case R.id.action_leave:
                leaveChatRoomDialog = ProgressDialog.show(this, "Leaving Chat..", "", false);
                subscribe(leaveChatRoom(), alias -> returnToMain(leaveChatRoomDialog));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void returnToMain(ProgressDialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
        }
        prefs.activeAlias().put(null);
        MainActivity_.intent(this).start();
    }

    // Removes user from the current ChatRoom and unregisters the device for push notifications.
    private Observable<Alias> leaveChatRoom() {
        api.resetReplayMessageEvents();
        return getGcmRegistrationToken()
                .flatMap(token -> api.unregisterForPushNotifications(currentAlias.getObjectId(), token))
                .flatMap(success -> api.endMessageStream(aliasId))
                .flatMap(success -> api.leaveChatRoom(currentAlias.getObjectId()));
    }

    private static class SimpleOnLayoutChangeListener implements View.OnLayoutChangeListener {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            Log.d(TAG, "onLayoutChange top: " + top + ", oldTop: " + oldTop + "," + bottom + "," + oldBottom);
            onLayoutChange(v);
        }

        public void onLayoutChange(View v) {
            // Custom logic here.
        }
    }
}
