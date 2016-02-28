package com.tonyjhuang.chatly.ui.chat;

import android.text.Editable;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.tonyjhuang.chatly.CheddarActivity;
import com.tonyjhuang.chatly.R;
import com.tonyjhuang.chatly.api.CheddarApi;
import com.tonyjhuang.chatly.api.models.Alias;
import com.tonyjhuang.chatly.api.models.Message;
import com.tonyjhuang.chatly.api.models.SendMessageImageOverlay;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * Created by tonyjhuang on 2/18/16.
 */
@EActivity(R.layout.activity_chat)
public class ChatActivity extends CheddarActivity {

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

    private MessageListAdapter adapter;

    private Alias currentAlias;

    @AfterViews
    public void updateViews() {
        subscribe(api.getCurrentUserId(), id -> {
            adapter = new MessageListAdapter(id);
            messageListView.setAdapter(adapter);
        });
    }

    @AfterInject
    public void init() {
        subscribe(api.getCurrentAlias(), (alias) -> currentAlias = alias);
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

    private void sendMessage() {
        String body = messageInput.getText().toString().trim();

        if (body.isEmpty()) {
            return;
        }

        Message placeholder = Message.createPlaceholderMessage(currentAlias, body);
        messageInput.setText("");
        adapter.addPlaceholderMessage(placeholder);
        subscribe(api.sendMessage(aliasId, body),
                message -> {
                },
                (throwable) -> {
                    adapter.notifyFailed(placeholder);
                    Log.e("Chat", throwable.toString());
                    Toast.makeText(this, "failed to deliver message", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscribe(api.getMessageStream(aliasId), (message) -> {
            if (adapter != null)
                adapter.addOrUpdateMessage(message);
        });
    }
}
