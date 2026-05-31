package com.yosuii.rssreader.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yosuii.rssreader.R;
import com.yosuii.rssreader.model.FeedEntity;

import java.util.ArrayList;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    public interface OnFeedClickListener {
        void onFeedClick(FeedEntity feed);
    }

    private final List<FeedEntity> feeds = new ArrayList<>();
    private final OnFeedClickListener listener;

    public FeedAdapter(OnFeedClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<FeedEntity> newFeeds) {
        feeds.clear();
        feeds.addAll(newFeeds);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedEntity feed = feeds.get(position);
        holder.titleTextView.setText(feed.title);
        holder.urlTextView.setText(feed.url);
        holder.itemView.setOnClickListener(v -> listener.onFeedClick(feed));
    }

    @Override
    public int getItemCount() {
        return feeds.size();
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView urlTextView;

        FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.feedTitleTextView);
            urlTextView = itemView.findViewById(R.id.feedUrlTextView);
        }
    }
}
