package com.digitalent.submission.movieworld.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.digitalent.submission.movieworld.model.Favorite;

@Database(entities = Favorite.class, exportSchema = false, version = 3)
public abstract class Moviedb extends RoomDatabase {
    private static final String DB_NAME = "favorite_db";
    private static Moviedb instance;

    public static synchronized Moviedb getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    Moviedb.class,
                    DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract FavoriteDao favoriteDao();
}
