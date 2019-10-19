package com.digitalent.submission.movieworld.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.digitalent.submission.movieworld.R;
import com.digitalent.submission.movieworld.model.Favorite;
import com.digitalent.submission.movieworld.model.Movie;
import com.digitalent.submission.movieworld.provider.FavoriteProvider;
import com.digitalent.submission.movieworld.util.FavoriteHelper;

@SuppressWarnings("WeakerAccess")
public class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final Uri FAVORITE_URI = Uri.parse(FavoriteProvider.CONTENT_URI.toString())
            .buildUpon()
            .clearQuery()
            .appendQueryParameter("type", Movie.TYPE_MOVIE)
            .build();

    private final Context context;
    private Cursor cursor;

    StackRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (cursor != null) {
            cursor.close();
        }

        final long token = Binder.clearCallingIdentity();

        cursor = context.getContentResolver().query(FAVORITE_URI, null, null, null, null);

        Binder.restoreCallingIdentity(token);
    }

    @Override
    public void onDestroy() {
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public int getCount() {
        if (cursor != null) {
            return cursor.getCount();
        }
        return 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.favorite_widget_item);

        if (cursor.moveToPosition(position)) {
            Favorite favorite = FavoriteHelper.cursorToItem(cursor);

            try {
                Bitmap bitmap = Glide.with(context)
                        .asBitmap()
                        .load(favorite.getPoster())
                        .submit(154, 231)
                        .get();

                views.setImageViewBitmap(R.id.image_widget, bitmap);
            } catch (Exception e){
                e.printStackTrace();
            }

            Bundle extras = new Bundle();
            extras.putString(FavoriteWidget.EXTRA_TITLE, favorite.getTitle());
            Intent intent = new Intent();
            intent.putExtras(extras);

            views.setOnClickFillInIntent(R.id.item_widget, intent);
        }



        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (cursor.moveToPosition(position)) {
            return cursor.getInt(cursor.getColumnIndex("id"));
        }
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
