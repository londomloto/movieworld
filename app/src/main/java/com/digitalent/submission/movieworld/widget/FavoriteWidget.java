package com.digitalent.submission.movieworld.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.digitalent.submission.movieworld.R;

/**
 * Implementation of App Widget functionality.
 */
public class FavoriteWidget extends AppWidgetProvider {
    public static final String EXTRA_TITLE = "com.digitalent.submission.movieworld.EXTRA_TITLE";
    private static final String UPDATE_ACTION = "com.digitalent.submission.movieworld.UPDATE_ACTION";
    private static final String TOAST_ACTION = "com.digitalent.submission.movieworld.TOAST_ACTION";

    public static void refresh(Context context) {
        Context appctx = context.getApplicationContext();
        Intent intent = new Intent(appctx, FavoriteWidget.class);
        intent.setAction(FavoriteWidget.UPDATE_ACTION);

        int[] appWidgetIds = AppWidgetManager.getInstance(appctx)
                .getAppWidgetIds(new ComponentName(appctx, FavoriteWidget.class));

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(intent);
    }

    @SuppressWarnings("WeakerAccess")
    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {

        Intent intent = new Intent(context, StackWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.favorite_widget);
        views.setRemoteAdapter(R.id.stack_view, intent);
        views.setEmptyView(R.id.stack_view, R.id.empty_view);

        Intent toastIntent = new Intent(context, FavoriteWidget.class);
        toastIntent.setAction(FavoriteWidget.TOAST_ACTION);
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setPendingIntentTemplate(R.id.stack_view, toastPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction() != null) {
            if (intent.getAction().equals(TOAST_ACTION)) {
                String title = intent.getStringExtra(EXTRA_TITLE);
                Toast.makeText(context, title, Toast.LENGTH_SHORT).show();
            }

            if (intent.getAction().equals(UPDATE_ACTION)) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName componentName = new ComponentName(context, FavoriteWidget.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.stack_view);
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDisabled(Context context) {

    }
}

