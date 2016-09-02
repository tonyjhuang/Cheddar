package com.tonyjhuang.cheddar.ui.joinchat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Data adapter for a list of chat room topics
 */
public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {

    // See http://stackoverflow.com/a/24933117
    private final PublishSubject<Topic> onClickSubject = PublishSubject.create();

    /**
     * TopicTree that we're displaying.
     */
    private TopicTree topicTree;

    public TopicAdapter(TopicTree topicTree) {
        this.topicTree = topicTree;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_topic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Topic topic = topicTree.topics().get(position);
        holder.nameView.setText(topic.name());
        holder.container.setOnClickListener(view -> onClickSubject.onNext(topic));
    }

    @Override
    public int getItemCount() {
        return topicTree.topics().size();
    }

    public Observable<Topic> getOnClickObservable() {
        return onClickSubject.asObservable();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewGroup container;
        public TextView nameView;
        public TextView selectorView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.name);
            selectorView = (TextView) itemView.findViewById(R.id.selector);
            container = (ViewGroup) itemView.findViewById(R.id.container);
        }
    }
}
