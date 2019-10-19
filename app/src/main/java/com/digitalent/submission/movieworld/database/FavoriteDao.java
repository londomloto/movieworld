package com.digitalent.submission.movieworld.database;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.digitalent.submission.movieworld.model.Favorite;

import java.util.List;

@SuppressWarnings("unused")
@Dao
public interface FavoriteDao {
    @Query("SELECT * FROM favorite")
    List<Favorite> find();

    @Query("SELECT * FROM favorite WHERE type = :type")
    List<Favorite> findByType(String type);

    @Query("SELECT * FROM favorite WHERE type = :type")
    Cursor queryByType(String type);

    @Query("SELECT * FROM favorite WHERE id = :id")
    Favorite findFirst(int id);

    @Query("SELECT * FROM favorite WHERE id = :id")
    Cursor queryById(long id);

    @Insert
    void insert(Favorite favorite);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertBatch(Favorite... favorites);

    @Update
    void update(Favorite favorite);

    @Delete
    void delete(Favorite favorite);

    @Query("DELETE FROM favorite WHERE id = :id")
    int deleteById(long id);

}
