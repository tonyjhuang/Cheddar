package com.tonyjhuang.cheddar.ui.onboard;

import com.tonyjhuang.cheddar.ui.presenter.Presenter;

/**
 * Presents to an OnboardView.
 */
public interface OnboardPresenter extends Presenter<OnboardView> {

    void onResume();
    void onPause();
    void onDestroy();
}
