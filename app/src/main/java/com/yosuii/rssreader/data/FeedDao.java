package com.yosuii.rssreader.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.yosuii.rssreader.model.FeedEntity;

import java.util.List;

@Dao
public interface FeedDao {
    @Query("SELECT * FROM feeds ORDER BY updatedAt DESC")
    List<FeedEntity> getAll();

    @Query("SELECT * FROM feeds WHERE url = :url LIMIT 1")
    FeedEntity getByUrl(String url);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(FeedEntity feed);

    @Update
    void update(FeedEntity feed);
}
