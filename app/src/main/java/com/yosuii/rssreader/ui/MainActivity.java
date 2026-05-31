package com.yosuii.rssreader.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yosuii.rssreader.R;
import com.yosuii.rssreader.data.RssRepository;
import com.yosuii.rssreader.model.ArticleEntity;
import com.yosuii.rssreader.model.FeedEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int REQUEST_ARTICLE_DETAIL = 1;
    private static final int MODE_FEEDS = 1;
    private static final int MODE_ARTICLES = 2;
    private static final int MODE_ALL_ARTICLES = 3;
    private static final int MODE_FAVORITES = 4;

    private EditText feedUrlEditText;
    private TextView titleTextView;
    private RecyclerView recyclerView;
    private RssRepository repository;
    private FeedAdapter feedAdapter;
    private ArticleAdapter articleAdapter;
    private FeedEntity selectedFeed;
    private int currentMode = MODE_FEEDS;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new RssRepository(this);
        bindViews();
        setupList();
        setupClicks();
        loadFeeds();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ARTICLE_DETAIL && resultCode == RESULT_OK) {
            refreshCurrentList();
        }
    }

    private void bindViews() {
        feedUrlEditText = findViewById(R.id.feedUrlEditText);
        titleTextView = findViewById(R.id.titleTextView);
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void setupList() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedAdapter = new FeedAdapter(feed -> {
            selectedFeed = feed;
            loadArticles(feed);
        });
        articleAdapter = new ArticleAdapter(this::openArticle);
    }

    private void setupClicks() {
        Button addFeedButton = findViewById(R.id.addFeedButton);
        Button feedsButton = findViewById(R.id.feedsButton);
        Button articlesButton = findViewById(R.id.articlesButton);
        Button favoritesButton = findViewById(R.id.favoritesButton);
        Button feedStreamButton = findViewById(R.id.feedStreamButton);

        addFeedButton.setOnClickListener(v -> addFeed());
        feedsButton.setOnClickListener(v -> loadFeeds());
        articlesButton.setOnClickListener(v -> {
            if (selectedFeed == null) {
                toast("请先选择一个订阅源");
            } else {
                loadArticles(selectedFeed);
            }
        });
        feedStreamButton.setOnClickListener(v -> {
            loadAllArticles();
        });
        favoritesButton.setOnClickListener(v -> loadFavorites());
    }

    private void addFeed() {
        String url = feedUrlEditText.getText().toString().trim();
        if (url.isEmpty()) {
            toast("请输入订阅源地址");
            return;
        }

        titleTextView.setText("正在添加...");
        executor.execute(() -> {
            try {
                repository.addFeed(url);
                mainHandler.post(() -> {
                    feedUrlEditText.setText("");
                    toast("添加成功");
                    loadFeeds();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    titleTextView.setText("订阅源");
                    toast("添加失败: " + e.getMessage());
                });
            }
        });
    }

    private void loadFeeds() {
        currentMode = MODE_FEEDS;
        titleTextView.setText("订阅源");
        executor.execute(() -> {
            List<FeedEntity> feeds = repository.getFeeds();
            mainHandler.post(() -> {
                recyclerView.setAdapter(feedAdapter);
                feedAdapter.submitList(feeds);
            });
        });
    }

    private void loadArticles(FeedEntity feed) {
        currentMode = MODE_ARTICLES;
        titleTextView.setText(feed.title);
        executor.execute(() -> {
            List<ArticleEntity> articles = repository.getArticles(feed.id);
            mainHandler.post(() -> {
                recyclerView.setAdapter(articleAdapter);
                articleAdapter.submitList(articles);
            });
        });
    }

    private void loadAllArticles() {
        currentMode = MODE_ALL_ARTICLES;
        titleTextView.setText("聚合文章流");
        executor.execute(() -> {
            if (repository.feedIsEmpty()) {
                mainHandler.post(() -> {
                    toast("请先添加一个订阅");
                });
                return;
            }
            List<ArticleEntity> articles = repository.getAllFeedArticles();
            mainHandler.post(() -> {
                recyclerView.setAdapter(articleAdapter);
                articleAdapter.submitList(articles);
            });
        });
    }

    private void loadFavorites() {
        currentMode = MODE_FAVORITES;
        titleTextView.setText("收藏");
        executor.execute(() -> {
            List<ArticleEntity> favorites = repository.getFavorites();
            mainHandler.post(() -> {
                recyclerView.setAdapter(articleAdapter);
                articleAdapter.submitList(favorites);
            });
        });
    }

    private void openArticle(ArticleEntity article) {
        Intent intent = new Intent(this, ArticleDetailActivity.class);
        intent.putExtra(ArticleDetailActivity.EXTRA_ARTICLE_ID, article.id);
        startActivityForResult(intent, REQUEST_ARTICLE_DETAIL);
    }

    private void refreshCurrentList() {
        if (currentMode == MODE_FAVORITES) {
            loadFavorites();
        } else if (currentMode == MODE_ALL_ARTICLES) {
            loadAllArticles();
        } else if (currentMode == MODE_ARTICLES && selectedFeed != null) {
            loadArticles(selectedFeed);
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
