package com.tonyjhuang.cheddar.ui.joinchat;

import android.support.v7.widget.Toolbar;

import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.R;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import rx.subjects.AsyncSubject;
import timber.log.Timber;

@EActivity(R.layout.activity_join_chat)
public class JoinChatActivity extends CheddarActivity {

    @ViewById
    Toolbar toolbar;

    @Extra
    TopicTree topicTree;

    /**
     * Subject that emits the list of topics.
     */
    private AsyncSubject<TopicTree> topicSubject = AsyncSubject.create();



    @AfterInject
    public void afterInject() {
        if (topicTree != null) {
            topicSubject.onNext(topicTree);
            topicSubject.onCompleted();
            topicSubject.subscribe(topics -> Timber.d(topics.toString()));
        } else {
            loadTopics();
        }
    }

    @AfterViews
    public void afterViews() {
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.join_chat_title);
    }

    /**
     * Load topics from disk and emit through topicSubject.
     */
    private void loadTopics() {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
