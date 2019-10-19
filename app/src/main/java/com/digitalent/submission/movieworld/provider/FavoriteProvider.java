package com.digitalent.submission.movieworld.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.digitalent.submission.movieworld.database.FavoriteDao;
import com.digitalent.submission.movieworld.database.Moviedb;
import com.digitalent.submission.movieworld.model.Favorite;
import com.digitalent.submission.movieworld.util.JavaHelper;

public class FavoriteProvider extends ContentProvider {

    private static final String AUTHORITY = "com.digitalent.submission.movieworld";
    public static final Uri CONTENT_URI = new Uri.Builder()
            .scheme("content")
            .authority(AUTHORITY)
            .appendPath(Favorite.TABLE_NAME)
            .build();

    private static final int TYPE_DIR = 1;
    private static final int TYPE_ITEM = 2;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, Favorite.TABLE_NAME, TYPE_DIR);
        URI_MATCHER.addURI(AUTHORITY, Favorite.TABLE_NAME + "/#", TYPE_ITEM);
    }

    public FavoriteProvider() {
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int deleted;
        if (URI_MATCHER.match(uri) == TYPE_ITEM) {
            final Context context = getContext();
            if (context == null) {
                deleted = 0;
            } else {
                deleted = Moviedb.getInstance(context.getApplicationContext())
                        .favoriteDao()
                        .deleteById(ContentUris.parseId(uri));
                context.getContentResolver().notifyChange(CONTENT_URI, null);
            }
        } else {
            deleted = 0;
        }

        return deleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case TYPE_DIR:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + Favorite.TABLE_NAME;
            case TYPE_ITEM:
                return "vnd.android.cursor.item/" + AUTHORITY + "." + Favorite.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int created;

        if (URI_MATCHER.match(uri) == TYPE_DIR) {
            final Context context = getContext();
            if (context == null) {
                created = 0;
            } else {
                Moviedb.getInstance(context.getApplicationContext())
                        .favoriteDao()
                        .insert(Favorite.createFormContentValues(values));
                created = values.getAsInteger("id");
                context.getContentResolver().notifyChange(CONTENT_URI, null);
            }
        } else {
            created = 0;
        }

        return Uri.parse(CONTENT_URI + "/" + created);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final int type = URI_MATCHER.match(uri);

        if (type == TYPE_DIR || type == TYPE_ITEM) {
            final Context context = getContext();

            if (context == null) {
                return null;
            }

            FavoriteDao dao = Moviedb.getInstance(context.getApplicationContext()).favoriteDao();
            final Cursor cursor;

            if (type == TYPE_DIR) {
                cursor = dao.queryByType(uri.getQueryParameter("type"));
            } else {
                cursor = dao.queryById(ContentUris.parseId(uri));
            }
            cursor.setNotificationUri(context.getContentResolver(), uri);
            return cursor;
        } else {
            return null;
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int updated;
        if (URI_MATCHER.match(uri) == TYPE_ITEM) {
            final Context context = getContext();
            if (context == null) {
                updated = 0;
            } else {
                final Favorite favorite = Favorite.createFormContentValues(values);
                final int id = JavaHelper.long2int(ContentUris.parseId(uri));
                favorite.setId(id);

                Moviedb.getInstance(context.getApplicationContext())
                        .favoriteDao()
                        .update(favorite);
                updated = 1;
                context.getContentResolver().notifyChange(CONTENT_URI, null);
            }
        } else {
            updated = 0;
        }

        return updated;
    }
}
