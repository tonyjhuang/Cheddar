package com.tonyjhuang.cheddar.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.utils.Scheduler;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import rx.Observable;
import rx.Subscription;
import timber.log.Timber;

/**
 * Created by tonyjhuang on 3/25/16.
 */
public class LoadingDialog extends AlertDialog {

    private static final String LOADING_GIF = "loading.gif";

    ViewGroup container;
    GifImageView image;
    ImageView imageBackup;
    TextView label;
    private String labelText;

    private Subscription loadLoadingGifSubscription;

    protected LoadingDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    private LoadingDialog(Context context, String labelText) {
        this(context, false, null);
        this.labelText = labelText;
    }

    public static LoadingDialog show(Context context, String labelText) {
        LoadingDialog loadingDialog = new LoadingDialog(context, labelText);
        loadingDialog.show();
        return loadingDialog;
    }

    public static LoadingDialog show(Context context, int labelTextId) {
        return show(context, context.getString(labelTextId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        container = (ViewGroup) findViewById(R.id.container);
        image = (GifImageView) findViewById(R.id.image);
        imageBackup = (ImageView) findViewById(R.id.image_backup);
        label = (TextView) findViewById(R.id.label);
        label.setText(labelText);

        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                getWindow().setLayout(container.getWidth(), container.getHeight());
            }
        });

        loadLoadingGifSubscription = loadLoadingImage().compose(Scheduler.defaultSchedulers())
                .subscribe(gifDrawable -> {
                    image.setImageDrawable(gifDrawable);
                    image.setVisibility(View.VISIBLE);
                    imageBackup.setVisibility(View.GONE);
                }, e -> Timber.e(e, "couldn't load gif."));
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (loadLoadingGifSubscription != null) {
            loadLoadingGifSubscription.unsubscribe();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (loadLoadingGifSubscription != null) {
            loadLoadingGifSubscription.unsubscribe();
        }
    }

    private Observable<GifDrawable> loadLoadingImage() {
        return Observable.create(subscriber -> {
            try {
                subscriber.onNext(new GifDrawable(getContext().getAssets(), LOADING_GIF));
                subscriber.onCompleted();
            } catch (IOException e) {
                subscriber.onError(e);
            }
        });
    }
}
