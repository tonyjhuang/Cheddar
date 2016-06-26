package com.tonyjhuang.cheddar.ui.welcome;

import com.tonyjhuang.cheddar.ui.presenter.Presenter;

/**
 * Presents to an WelcomeView.
 */
public interface WelcomePresenter extends Presenter<WelcomeView> {

    void onResume();

    void onPause();

    void onDestroy();
}
