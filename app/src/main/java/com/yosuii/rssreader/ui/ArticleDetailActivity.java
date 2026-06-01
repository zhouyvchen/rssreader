package com.yosuii.rssreader.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.yosuii.rssreader.R;
import com.yosuii.rssreader.data.RssRepository;
import com.yosuii.rssreader.model.ArticleEntity;
import com.yosuii.rssreader.util.HtmlUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArticleDetailActivity extends Activity {
    public static final String EXTRA_ARTICLE_ID = "article_id";

    private WebView webView;
    private Button favoriteButton;
    private RssRepository repository;
    private ArticleEntity article;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        webView = findViewById(R.id.articleWebView);
        favoriteButton = findViewById(R.id.favoriteButton);
        repository = new RssRepository(this);

        webView.setWebViewClient(new WebViewClient());
        favoriteButton.setOnClickListener(v -> toggleFavorite());
        loadArticle();
    }

    private void loadArticle() {
        long articleId = getIntent().getLongExtra(EXTRA_ARTICLE_ID, -1L);
        executor.execute(() -> {
            ArticleEntity loadedArticle = repository.getArticle(articleId);

            // 修改已读状态
            ArticleEntity loadArticle = repository.getArticle(articleId);
            if (loadArticle != null) {
                repository.setRead(articleId, true);
                loadArticle.isRead = true;
                setResult(RESULT_OK);
            }
            mainHandler.post(() -> showArticle(loadedArticle));
        });
    }

    private void showArticle(ArticleEntity loadedArticle) {
        if (loadedArticle == null) {
            Toast.makeText(this, "文章不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        article = loadedArticle;
        setTitle(article.title);
        updateFavoriteButton();

        String html = HtmlUtils.buildArticleHtml(article.title,
                article.content, article.link);
        webView.loadDataWithBaseURL(article.link, html, "text/html", "UTF-8",
                null);
    }

    private void toggleFavorite() {
        if (article == null) {
            return;
        }

        boolean newValue = !article.isFavorite;
        executor.execute(() -> {
            repository.setFavorite(article.id, newValue);
            article.isFavorite = newValue;
            mainHandler.post(() -> {
                updateFavoriteButton();

                Intent result = new Intent();
                result.putExtra("article_id", article.id);
                result.putExtra("is_favorite", article.isFavorite);
                setResult(RESULT_OK, result);
            });
        });
    }

    private void updateFavoriteButton() {
        favoriteButton.setText(article.isFavorite ? "取消收藏" : "收藏");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
        executor.shutdown();
    }
}
