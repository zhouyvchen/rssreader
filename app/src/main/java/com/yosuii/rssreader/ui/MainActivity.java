package com.yosuii.rssreader.ui;

import android.app.Activity;
import android.app.AlertDialog;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
    private static final int MODE_ALL_ARTICLES = 2;
    private static final int MODE_FAVORITES = 3;

    private EditText feedUrlEditText;
    private TextView titleTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private Button feedsButton;
    private Button feedStreamButton;
    private Button favoritesButton;
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
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        feedsButton = findViewById(R.id.feedsButton);
        feedStreamButton = findViewById(R.id.feedStreamButton);
        favoritesButton = findViewById(R.id.favoritesButton);
    }

    private void setupList() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary,
                R.color.color_accent);
        feedAdapter = new FeedAdapter(feed -> {
            selectedFeed = feed;
            loadAllArticles();
        });
        articleAdapter = new ArticleAdapter(this::openArticle);
    }

    private void setupClicks() {
        Button addFeedButton = findViewById(R.id.addFeedButton);

        addFeedButton.setOnClickListener(v -> addFeed());
        feedsButton.setOnClickListener(v -> loadFeeds());
        feedStreamButton.setOnClickListener(v -> {
            selectedFeed = null;
            loadAllArticles();
        });
        favoritesButton.setOnClickListener(v -> loadFavorites());
        swipeRefreshLayout.setOnRefreshListener(this::refreshByPull);
        titleTextView.setOnClickListener(v -> {
            if (currentMode == MODE_ALL_ARTICLES) {
                showFeedSwitcher();
            }
        });
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
        updateTabs();
        setTitleText("订阅源", false);
        executor.execute(() -> {
            List<FeedEntity> feeds = repository.getFeeds();
            mainHandler.post(() -> {
                recyclerView.setAdapter(feedAdapter);
                feedAdapter.submitList(feeds);
            });
        });
    }

    private void loadAllArticles() {
        currentMode = MODE_ALL_ARTICLES;
        updateTabs();
        setTitleText(buildArticleStreamTitle(), true);
        executor.execute(() -> {
            if (repository.feedIsEmpty()) {
                mainHandler.post(() -> {
                    toast("请先添加一个订阅");
                });
                return;
            }
            List<ArticleEntity> articles = selectedFeed == null
                    ? repository.getAllFeedArticles()
                    : repository.getArticles(selectedFeed.id);
            mainHandler.post(() -> {
                recyclerView.setAdapter(articleAdapter);
                articleAdapter.submitList(articles);
            });
        });
    }

    private void loadFavorites() {
        currentMode = MODE_FAVORITES;
        updateTabs();
        setTitleText("收藏", false);
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
        }
    }

    private void refreshByPull() {
        if (currentMode == MODE_ALL_ARTICLES && selectedFeed != null) {
            refreshSelectedFeedByPull();
        } else if (currentMode == MODE_ALL_ARTICLES) {
            refreshAllFeedsByPull();
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void refreshSelectedFeedByPull() {
        executor.execute(() -> {
            try {
                repository.refreshFeed(selectedFeed);
                mainHandler.post(() -> {
                    toast("刷新成功");
                    swipeRefreshLayout.setRefreshing(false);
                    refreshCurrentList();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    toast("刷新失败: " + e.getMessage());
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        });
    }

    private void refreshAllFeedsByPull() {
        executor.execute(() -> {
            if (repository.feedIsEmpty()) {
                mainHandler.post(() -> {
                    toast("请先添加一个订阅");
                    swipeRefreshLayout.setRefreshing(false);
                });
                return;
            }

            try {
                repository.refreshAllFeeds();
                mainHandler.post(() -> {
                    toast("刷新成功");
                    swipeRefreshLayout.setRefreshing(false);
                    refreshCurrentList();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    toast("刷新失败: " + e.getMessage());
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        });
    }

    private void updateTabs() {
        setTabSelected(feedsButton, currentMode == MODE_FEEDS);
        setTabSelected(feedStreamButton, currentMode == MODE_ALL_ARTICLES);
        setTabSelected(favoritesButton, currentMode == MODE_FAVORITES);
        swipeRefreshLayout.setEnabled(currentMode == MODE_ALL_ARTICLES);
    }

    private void showFeedSwitcher() {
        executor.execute(() -> {
            List<FeedEntity> feeds = repository.getFeeds();
            mainHandler.post(() -> {
                if (feeds.isEmpty()) {
                    toast("请先添加一个订阅");
                    return;
                }

                String[] names = new String[feeds.size() + 1];
                names[0] = "全部文章";
                for (int i = 0; i < feeds.size(); i++) {
                    names[i + 1] = feeds.get(i).title;
                }

                new AlertDialog.Builder(this)
                        .setTitle("切换文章源")
                        .setItems(names, (dialog, which) -> {
                            selectedFeed = which == 0 ? null : feeds.get(which - 1);
                            loadAllArticles();
                        })
                        .show();
            });
        });
    }

    private String buildArticleStreamTitle() {
        String source = selectedFeed == null ? "全部" : selectedFeed.title;
        return "聚合文章流 · " + source + " ▾";
    }

    private void setTitleText(String text, boolean canSwitchSource) {
        titleTextView.setText(text);
        titleTextView.setClickable(canSwitchSource);
        titleTextView.setTextColor(getColor(canSwitchSource
                ? R.color.color_primary
                : R.color.text_primary));
    }

    private void setTabSelected(Button button, boolean selected) {
        button.setSelected(selected);
        button.setTextColor(getColor(selected ? R.color.color_primary : R.color.text_secondary));
        button.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
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
