package com.tonyjhuang.cheddar.ui.list;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

@EBean
public class ChatRoomListAdapter extends RecyclerView.Adapter<ChatRoomListAdapter.ViewHolder> {

    // See http://stackoverflow.com/a/24933117
    private final PublishSubject<ChatRoomInfo> onClickSubject = PublishSubject.create();
    private List<ChatRoomInfo> infoList = new ArrayList<>();
    private String currentUserId;

    public ChatRoomListAdapter() {
        super();
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ChatRoomItemView_.build(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.view.setChatRoomInfo(infoList.get(position), currentUserId);
        holder.view.setOnClickListener(view -> onClickSubject.onNext(infoList.get(position)));
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    @Override
    public long getItemId(int position) {
        return infoList.get(position).alias().objectId().hashCode();
    }

    public Observable<ChatRoomInfo> getOnClickObservable() {
        return onClickSubject.asObservable();
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setInfoList(List<ChatRoomInfo> newInfoList) {
        infoList = newInfoList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ChatRoomItemView view;

        public ViewHolder(ChatRoomItemView itemView) {
            super(itemView);
            view = itemView;
        }
    }
}
