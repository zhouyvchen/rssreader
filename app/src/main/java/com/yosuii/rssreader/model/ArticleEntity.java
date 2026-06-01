package com.yosuii.rssreader.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "articles",
        foreignKeys = @ForeignKey(
                entity = FeedEntity.class,
                parentColumns = "id",
                childColumns = "feedId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("feedId"),
                @Index(value = {"link"}, unique = true),
                @Index("isFavorite"),
                @Index("isRead")
        }
)
public class ArticleEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long feedId;
    public String title;
    public String link;
    public String summary;
    public String content;
    public String author;
    public long publishedAt;
    public boolean isFavorite;
    public boolean isRead;

    public ArticleEntity(long feedId, String title, String link, String summary, String content,
                         String author, long publishedAt, boolean isFavorite,boolean isRead) {
        this.feedId = feedId;
        this.title = title;
        this.link = link;
        this.summary = summary;
        this.content = content;
        this.author = author;
        this.publishedAt = publishedAt;
        this.isFavorite = isFavorite;
        this.isRead = isRead;
    }
}
