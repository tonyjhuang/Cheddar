package com.tonyjhuang.cheddar.ui.customviews;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;

/**
 * Created by tonyjhuang on 3/25/16.
 */
public class LoadingDialog extends AlertDialog {

    ViewGroup container;
    ImageView image;
    TextView label;
    private String labelText;

    protected LoadingDialog(Context context) {
        super(context);
    }

    protected LoadingDialog(Context context, int theme) {
        super(context, theme);
    }

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
        label = (TextView) findViewById(R.id.label);
        label.setText(labelText);

        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                getWindow().setLayout(container.getWidth(), container.getHeight());
            }
        });
    }
}
