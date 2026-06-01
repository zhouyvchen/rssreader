package com.yosuii.rssreader.data;

import android.content.Context;

import com.yosuii.rssreader.model.ArticleEntity;
import com.yosuii.rssreader.model.FeedEntity;
import com.yosuii.rssreader.network.FeedFetcher;
import com.yosuii.rssreader.parser.FeedParser;
import com.yosuii.rssreader.parser.ParsedArticle;
import com.yosuii.rssreader.parser.ParsedFeed;

import java.util.ArrayList;
import java.util.List;

public class RssRepository {
    private final FeedDao feedDao;
    private final ArticleDao articleDao;
    private final FeedFetcher fetcher = new FeedFetcher();
    private final FeedParser parser = new FeedParser();

    public RssRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        feedDao = database.feedDao();
        articleDao = database.articleDao();
    }

    public void addFeed(String url) throws Exception {
        String xml = fetcher.fetch(url);
        ParsedFeed parsedFeed = parser.parse(xml);
        String title = parsedFeed.title.isEmpty() ? url : parsedFeed.title;

        FeedEntity feed = new FeedEntity(
                title,
                url,
                parsedFeed.siteUrl,
                parsedFeed.description,
                System.currentTimeMillis()
        );

        long feedId = feedDao.insert(feed);
        if (feedId == -1) {
            FeedEntity oldFeed = feedDao.getByUrl(url);
            oldFeed.title = feed.title;
            oldFeed.siteUrl = feed.siteUrl;
            oldFeed.description = feed.description;
            oldFeed.updatedAt = feed.updatedAt;
            feedDao.update(oldFeed);
            feedId = oldFeed.id;
        }

        List<ArticleEntity> articles = new ArrayList<>();
        for (ParsedArticle article : parsedFeed.articles) {
            String content = article.content.isEmpty() ? article.summary : article.content;
            articles.add(new ArticleEntity(
                    feedId,
                    article.title,
                    article.link,
                    article.summary,
                    content,
                    article.author,
                    article.publishedAt,
                    false
            ));
        }
        articleDao.insertAll(articles);
    }

    public void refreshFeed(FeedEntity feed) throws Exception {
        String xml = fetcher.fetch(feed.url);
        ParsedFeed parsedFeed = parser.parse(xml);

        feed.title = parsedFeed.title.isEmpty() ? feed.url : parsedFeed.title;
        feed.siteUrl = parsedFeed.siteUrl;
        feed.description = parsedFeed.description;
        feed.updatedAt = System.currentTimeMillis();
        feedDao.update(feed);

        List<ArticleEntity> articles = new ArrayList<>();
        for (ParsedArticle article : parsedFeed.articles) {
            String content = article.content.isEmpty() ? article.summary : article.content;
            articles.add(new ArticleEntity(
                    feed.id,
                    article.title,
                    article.link,
                    article.summary,
                    content,
                    article.author,
                    article.publishedAt,
                    false
            ));
        }
        articleDao.insertAll(articles);
    }

    public void refreshAllFeeds() throws Exception {
        List<FeedEntity> feeds = feedDao.getAll();
        for (FeedEntity feed : feeds) {
            refreshFeed(feed);
        }
    }

    public List<FeedEntity> getFeeds() {
        return feedDao.getAll();
    }

    public List<ArticleEntity> getArticles(long feedId) {
        return articleDao.getByFeed(feedId);
    }

    // 获取所有订阅源的所有文章
    public List<ArticleEntity> getAllFeedArticles(){
        return articleDao.getAll();
    }

    public List<ArticleEntity> getFavorites() {
        return articleDao.getFavorites();
    }

    public ArticleEntity getArticle(long id) {
        return articleDao.getById(id);
    }

    public void setFavorite(long id, boolean favorite) {
        articleDao.setFavorite(id, favorite);
    }

    public boolean feedIsEmpty(){
        return feedDao.getAll().isEmpty();
    }
}
