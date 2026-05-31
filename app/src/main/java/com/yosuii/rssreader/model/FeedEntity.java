package com.yosuii.rssreader.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "feeds",
        indices = {@Index(value = {"url"}, unique = true)}
)
public class FeedEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public String url;
    public String siteUrl;
    public String description;
    public long updatedAt;

    public FeedEntity(String title, String url, String siteUrl, String description, long updatedAt) {
        this.title = title;
        this.url = url;
        this.siteUrl = siteUrl;
        this.description = description;
        this.updatedAt = updatedAt;
    }
}
