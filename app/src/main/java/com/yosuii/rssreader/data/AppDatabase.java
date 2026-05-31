package com.yosuii.rssreader.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.yosuii.rssreader.model.ArticleEntity;
import com.yosuii.rssreader.model.FeedEntity;

@Database(entities = {FeedEntity.class, ArticleEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract FeedDao feedDao();

    public abstract ArticleDao articleDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "rss_reader.db"
                            )
                            .build();
                }
            }
        }
        return instance;
    }
}
