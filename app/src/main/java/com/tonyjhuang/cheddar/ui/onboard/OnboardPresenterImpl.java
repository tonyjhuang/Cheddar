package com.tonyjhuang.cheddar.ui.onboard;

import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.CheddarMetrics;
import com.tonyjhuang.cheddar.utils.Scheduler;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by tonyjhuang on 4/28/16.
 */
@EBean
public class OnboardPresenterImpl implements OnboardPresenter {

    @Bean
    CheddarApi api;

    /**
     * The view we're presenting to.
     */
    private OnboardView view;

    @Override
    public void setView(OnboardView view) {
        this.view = view;
    }

    @Subscribe
    public void onJoinChatClicked(OnboardActivity.AlphaWarningFragment.JoinChatEvent event) {
        if(view != null) view.showJoinChatLoadingDialog();
        api.joinGroupChatRoom().compose(Scheduler.defaultSchedulers())
                .subscribe(alias -> {
                    CheddarMetrics.trackJoinChatRoom(alias.chatRoomId());
                    if(view != null) view.navigateToChatView(alias.objectId());
                }, error -> {
                    if(view != null) view.showJoinChatFailed();
                });
    }

    @Override
    public void onResume() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        view = null;
    }
}
