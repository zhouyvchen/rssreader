package com.yosuii.rssreader.ui;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yosuii.rssreader.R;
import com.yosuii.rssreader.model.ArticleEntity;

import java.util.ArrayList;
import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {
    public interface OnArticleClickListener {
        void onArticleClick(ArticleEntity article);
    }

    private final List<ArticleEntity> articles = new ArrayList<>();
    private final OnArticleClickListener listener;

    public ArticleAdapter(OnArticleClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ArticleEntity> newArticles) {
        articles.clear();
        articles.addAll(newArticles);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        ArticleEntity article = articles.get(position);
        holder.titleTextView.setText(article.title);
        holder.summaryTextView.setText(Html.fromHtml(article.summary, Html.FROM_HTML_MODE_COMPACT));
        holder.itemView.setOnClickListener(v -> listener.onArticleClick(article));
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView summaryTextView;

        ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.articleTitleTextView);
            summaryTextView = itemView.findViewById(R.id.articleSummaryTextView);
        }
    }
}
