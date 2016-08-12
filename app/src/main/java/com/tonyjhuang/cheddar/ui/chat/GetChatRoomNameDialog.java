package com.tonyjhuang.cheddar.ui.chat;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tonyjhuang.cheddar.R;

/**
 * Wrapper for dialog to get a new ChatRoom name.
 */
public class GetChatRoomNameDialog {

    private static final int MAX_ROOM_NAME_LENGTH = 30;

    private static float getLimitViewAlpha(int roomNameLength) {
        return roomNameLength >= MAX_ROOM_NAME_LENGTH - 5 ? 1 : 0;
    }

    public static void show(Context context, String currentName, Callback callback) {
        View view = View.inflate(context, R.layout.dialog_chat_room_name, null);
        EditText nameInput = (EditText) view.findViewById(R.id.name);
        nameInput.setText(currentName);

        TextView limitView = (TextView) view.findViewById(R.id.limit);
        limitView.setText(String.valueOf(MAX_ROOM_NAME_LENGTH - currentName.length()));
        limitView.setAlpha(getLimitViewAlpha(currentName.length()));
        nameInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int len = s.length();
                limitView.setText(String.valueOf(MAX_ROOM_NAME_LENGTH - len));
                limitView.animate().alpha(getLimitViewAlpha(len));
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.chat_name_dialog_title)
                .setView(view)
                .setPositiveButton(R.string.chat_name_dialog_ok, null)
                .setNegativeButton(R.string.common_cancel, null);

        AlertDialog dialog = builder.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(context, R.string.chat_name_dialog_error_empty, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else if (name.length() > MAX_ROOM_NAME_LENGTH) {
                Toast.makeText(context, R.string.chat_name_dialog_error_max_length, Toast.LENGTH_SHORT).show();
            } else if (!name.equals(currentName)) {
                callback.onSuccess(name);
                dialog.dismiss();
            }
        });
    }

    public interface Callback {
        void onSuccess(String name);
    }

    private static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
