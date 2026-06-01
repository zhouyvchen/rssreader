package com.yosuii.rssreader.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.yosuii.rssreader.model.ArticleEntity;

import java.util.List;

@Dao
public interface ArticleDao {
    @Query("SELECT * FROM articles WHERE feedId = :feedId ORDER BY publishedAt" +
            " DESC, id DESC")
    List<ArticleEntity> getByFeed(long feedId);

    @Query("SELECT * FROM articles WHERE isFavorite = 1 ORDER BY publishedAt " +
            "DESC, id DESC")
    List<ArticleEntity> getFavorites();

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    ArticleEntity getById(long id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<ArticleEntity> articles);

    @Query("UPDATE articles SET isFavorite = :favorite WHERE id = :id")
    void setFavorite(long id, boolean favorite);

    @Query("SELECT * FROM articles ORDER BY publishedAt DESC, id DESC")
    List<ArticleEntity> getAll();

    @Query("UPDATE articles SET isRead = :read WHERE id = :id")
    void setRead(long id, boolean read);
}
