package com.tonyjhuang.cheddar.ui.joinchat;

import android.content.res.AssetManager;
import android.support.v7.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.models.value.ValueTypeAdapterFactory;
import com.tonyjhuang.cheddar.utils.Scheduler;
import com.tonyjhuang.cheddar.utils.VersionChecker;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.subjects.AsyncSubject;
import timber.log.Timber;

@EActivity(R.layout.activity_join_chat)
public class JoinChatActivity extends CheddarActivity {

    @ViewById
    Toolbar toolbar;

    @Bean
    VersionChecker versionChecker;

    @Extra
    TopicTree topicTree;

    /**
     * Subject that emits the list of topics.
     */
    private AsyncSubject<TopicTree> topicSubject = AsyncSubject.create();
    private Subscription topicSubscription; // For sending topics to UI.
    private boolean firstLoad = true;

    @AfterInject
    public void afterInject() {
        if (topicTree != null) {
            topicSubject.onNext(TopicTree.create(new ArrayList<>()));
            topicSubject.onCompleted();
        } else {
            loadTopics().compose(Scheduler.defaultSchedulers())
                    .subscribe(topicSubject);
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
    private Observable<TopicTree> loadTopics() {
        return Observable.defer(() -> {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapterFactory(ValueTypeAdapterFactory.create())
                    .create();
            AssetManager assetManager = getAssets();
            try {
                // Unhardcode pls
                InputStream ims = assetManager.open("neu_topics.json");
                Reader reader = new InputStreamReader(ims);
                return Observable.just(gson.fromJson(reader, TopicTree.class));
            } catch (IOException e) {
                return Observable.error(e);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForUpdate(versionChecker);
        if (firstLoad) {
            topicSubscription = topicSubject.subscribe(
                    this::displayTopics, e -> Timber.e(e, "couldn't get topics"));
        }
    }

    private void displayTopics(TopicTree topicTree) {
        firstLoad = false;
        Timber.d(topicTree.toString());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (topicSubscription != null) topicSubscription.unsubscribe();
    }
}
