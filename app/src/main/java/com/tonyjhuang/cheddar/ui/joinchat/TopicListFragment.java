package com.tonyjhuang.cheddar.ui.joinchat;

import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tonyjhuang.cheddar.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

/**
 * Created by tonyjhuang on 9/1/16.
 */
@EFragment(R.layout.fragment_topic_list)
public class TopicListFragment extends Fragment{
    @ViewById(R.id.topic_view)
    RecyclerView topicView;

    @Extra
    TopicTree topicTree;

    @AfterViews
    public void afterViews() {
        topicView.setLayoutManager(new LinearLayoutManager(getContext()));
        topicView.setItemAnimator(new DefaultItemAnimator());
        TopicAdapter adapter = new TopicAdapter(topicTree);
        topicView.setAdapter(adapter);
        adapter.getOnClickObservable().subscribe(
                topic -> EventBus.getDefault().post(new OnTopicClickedEvent(topic)),
                error -> Timber.e(error, "???"));
    }

    public static class OnTopicClickedEvent {
        public Topic topic;

        public OnTopicClickedEvent(Topic topic) {
            this.topic = topic;
        }
    }
}
