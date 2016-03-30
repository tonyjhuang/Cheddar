package com.tonyjhuang.cheddar.ui.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.CheddarMetricTracker;

import org.androidannotations.annotations.EBean;

/**
 * Wrapper for customized feedback AlertDialog.
 */
@EBean
public class FeedbackDialogHelper {

    public static void getFeedback(Context context, Callback callback) {
        View view = View.inflate(context, R.layout.dialog_feedback, null);
        EditText nameInput = (EditText) view.findViewById(R.id.name);
        EditText feedbackInput = (EditText) view.findViewById(R.id.feedback_input);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.feedback_title)
                .setMessage(R.string.feedback_message)
                .setView(view)
                .setPositiveButton(R.string.feedback_confirm, (dialog, which) -> {
                    String feedback = feedbackInput.getText().toString();
                    if (feedback.isEmpty()) {
                        Toast.makeText(context, R.string.feedback_empty, Toast.LENGTH_SHORT).show();
                    } else {
                        callback.onFeedback(nameInput.getText().toString(), feedback);
                    }
                })
                .setNegativeButton(R.string.feedback_cancel, null);
        builder.show();
        CheddarMetricTracker.trackFeedback(CheddarMetricTracker.FeedbackLifecycle.OPENED);
    }

    public interface Callback {
        void onFeedback(String name, String feedback);
    }
}