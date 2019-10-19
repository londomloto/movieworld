package com.digitalent.submission.movieworld.util;

import android.database.Cursor;

import com.digitalent.submission.movieworld.model.Favorite;

@SuppressWarnings("WeakerAccess")
public class FavoriteHelper {

    public static Favorite cursorToItem(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));

        String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
        String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        String releaseDate = cursor.getString(cursor.getColumnIndexOrThrow("release_date"));
        String overview = cursor.getString(cursor.getColumnIndexOrThrow("overview"));
        String poster = cursor.getString(cursor.getColumnIndexOrThrow("poster"));

        return new Favorite(id, type, title, releaseDate, overview, poster);
    }

}
