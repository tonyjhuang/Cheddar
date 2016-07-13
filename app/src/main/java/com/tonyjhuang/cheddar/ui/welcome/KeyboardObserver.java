package com.tonyjhuang.cheddar.ui.welcome;

import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by tonyjhuang on 7/12/16.
 */
public class KeyboardObserver implements ViewTreeObserver.OnGlobalLayoutListener {

    private List<KeyboardListener> listeners = new ArrayList<>();
    private int lastMaxHeight;
    private View observableLayout;

    public void setObservableLayout(View observableLayout) {
        this.observableLayout = observableLayout;
    }

    @Override
    public void onGlobalLayout() {
        if (observableLayout == null) return;
        int newHeight = observableLayout.getHeight();
        if (newHeight >= lastMaxHeight) {
            Timber.i("layout is settling... %d", newHeight);
            lastMaxHeight = newHeight;
            for (KeyboardListener listener : listeners) {
                listener.onKeyboardHidden();
            }
        } else {
            Timber.d("keyboard opened? %d", newHeight);
            for (KeyboardListener listener : listeners) {
                listener.onKeyboardShown();
            }
        }
    }

    public void addListener(KeyboardListener listener) {
        listeners.add(listener);
    }

    interface KeyboardListener {
        void onKeyboardShown();

        void onKeyboardHidden();
    }
}
