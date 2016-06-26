package com.tonyjhuang.cheddar.ui.chat;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tonyjhuang.cheddar.R;

/**
 * Wrapper for dialog to get a new ChatRoom name.
 */
public class GetChatRoomNameDialog {

    public static void show(Context context, String currentName, Callback callback) {
        View view = View.inflate(context, R.layout.dialog_chat_room_name, null);
        EditText nameInput = (EditText) view.findViewById(R.id.name);
        nameInput.setText(currentName);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.chat_name_dialog_title)
                .setView(view)
                .setPositiveButton(R.string.chat_name_dialog_ok, (dialog, which) -> {
                    String name = nameInput.getText().toString();
                    if (name.isEmpty()) {
                        Toast.makeText(context, R.string.chat_name_dialog_error_empty, Toast.LENGTH_SHORT).show();
                    } else if (!name.equals(currentName)) {
                        callback.onSuccess(name);
                    }
                })
                .setNegativeButton(R.string.common_cancel, null);
        builder.show();
    }

    public interface Callback {
        void onSuccess(String name);
    }
}
