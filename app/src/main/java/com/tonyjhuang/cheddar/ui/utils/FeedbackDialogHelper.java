package com.tonyjhuang.cheddar.ui.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.CheddarMetricTracker;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Wrapper for customized feedback AlertDialog.
 */
@EBean
public class FeedbackDialogHelper {

    @Bean
    CheddarApi api;

    private AlertDialog dialog;

    private Subscriber<? super String> subscriber;

    /**
     * Displays the dialog and allows users to send in feedback.
     * Returns an Observable that contains the result of sending feedback.
     * To dismiss the dialog call FeedbackDialogHelper#dismiss
     */
    public Observable<String> show(Context context, String userId, String chatRoomId) {
        View view = View.inflate(context, R.layout.stub_feedback_input, null);
        EditText input = (EditText) view.findViewById(R.id.feedback_input);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.feedback_title)
                .setMessage(R.string.feedback_message)
                .setView(view)
                .setPositiveButton(R.string.feedback_confirm, (dialog, which) -> {
                    String feedback = input.getText().toString();
                    if (feedback.isEmpty()) {
                        Toast.makeText(context, R.string.feedback_empty, Toast.LENGTH_SHORT).show();
                    } else {
                        if (subscriber != null) {
                            CheddarMetricTracker.trackFeedback(CheddarMetricTracker.FeedbackLifecycle.SENT);
                            Toast.makeText(context, R.string.feedback_thanks, Toast.LENGTH_SHORT).show();
                            api.sendFeedback(userId, chatRoomId, feedback)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(subscriber);
                        }
                    }
                })
                .setNegativeButton(R.string.feedback_cancel, (dialog, which) -> {
                    if (this.subscriber != null) {
                        this.subscriber.onCompleted();
                    }
                    this.subscriber = null;
                });
        dialog = builder.show();
        CheddarMetricTracker.trackFeedback(CheddarMetricTracker.FeedbackLifecycle.OPENED);

        return Observable.create(subscriber -> this.subscriber = subscriber);
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
        if (subscriber != null) {
            subscriber.onCompleted();
            subscriber = null;
        }
    }
}