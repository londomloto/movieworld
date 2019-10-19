package com.digitalent.submission.movieworld.receiver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.digitalent.submission.movieworld.MainActivity;
import com.digitalent.submission.movieworld.R;
import com.digitalent.submission.movieworld.api.Tmdb;
import com.digitalent.submission.movieworld.model.Discover;
import com.digitalent.submission.movieworld.model.Movie;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TYPE_DAILY = "TYPE_DAILY";
    private static final String TYPE_RELEASE = "TYPE_RELEASE";

    private static final String EXTRA_TYPE = "EXTRA_TYPE";
    private static final String DAILY_CHANNEL_ID = "CHANNEL_DAILY";
    private static final String DAILY_CHANNEL_NAME = "Daily Reminder";

    private static final String RELEASE_CHANNEL_NAME = "Release Reminder";
    private static final String RELEASE_CHANNEL_ID = "CHANNEL_INBOX";
    private static final String RELEASE_GROUP_NAME = "com.digitalent.submission.movieworld.RELEASE_GROUP";

    private final int ID_DAILY = 500;
    private final int ID_RELEASE = 501;

    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra(EXTRA_TYPE);

        if (type.equalsIgnoreCase(TYPE_DAILY)) {
            showDailyNotification(context);
        } else if (type.equalsIgnoreCase(TYPE_RELEASE)) {
            showReleaseNotification(context);
        }
    }

    public void setupDailyReminder(Context context) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(EXTRA_TYPE, TYPE_DAILY);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ID_DAILY, intent, 0);

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    public void cancelDailyReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ID_DAILY, intent, 0);
        pendingIntent.cancel();

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    public void setupReleaseReminder(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(EXTRA_TYPE, TYPE_RELEASE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ID_RELEASE, intent, 0);

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent);
        }
    }

    public void cancelReleaseReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ID_RELEASE, intent, 0);
        pendingIntent.cancel();

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void showDailyNotification(Context context) {
        final String title = context.getString(R.string.daily_reminder);
        final String message = context.getString(R.string.pref_daily_summary);

        Intent notifyIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DAILY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_access_alarm_black)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_access_alarm_black_48dp))
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setChannelId(DAILY_CHANNEL_ID)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    DAILY_CHANNEL_ID,
                    DAILY_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);

            builder.setChannelId(DAILY_CHANNEL_ID);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        Notification notification = builder.build();

        if (notificationManager != null) {
            notificationManager.notify(ID_DAILY, notification);
        }
    }

    private void showReleaseNotification(final Context context) {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(date);

        Callback<Discover> callback = new Callback<Discover>() {
            @Override
            public void onResponse(@NonNull Call<Discover> call, Response<Discover> response) {
                if (response.isSuccessful()) {
                    Discover body = response.body();
                    if (body != null) {
                        ArrayList<Movie> movies = body.getResults();

                        for (int i = 0; i < movies.size(); i++) {
                            showStackNotification(context, movies.get(i), i);
                        }

                        showGroupNotification(context, movies);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Discover> call, @NonNull Throwable t) {
                // Do nothing
            }
        };

        Tmdb.getApi().discoverRelease(Tmdb.API_KEY, today, today).enqueue(callback);
    }

    private void showStackNotification(Context context, Movie movie, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_movie_black_48dp);

        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, RELEASE_CHANNEL_ID)
                .setContentTitle(movie.getCaption())
                .setContentText(context.getString(R.string.released_today))
                .setSmallIcon(R.drawable.ic_movie_black)
                .setLargeIcon(largeIcon)
                .setGroup(RELEASE_GROUP_NAME)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    RELEASE_CHANNEL_ID,
                    RELEASE_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);

            builder.setChannelId(RELEASE_CHANNEL_ID);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        Notification notification = builder.build();

        if (notificationManager != null) {
            notificationManager.notify(id, notification);
        }
    }

    private void showGroupNotification(Context context, ArrayList<Movie> movies) {
        final Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_movie_black_48dp);
        final String summaryTitle = context.getString(R.string.release_reminder);
        final String summaryText = movies.size() + " " + context.getString(R.string.movies_released_today);

        final Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .setBigContentTitle(summaryTitle)
                .setSummaryText(summaryText);

        for (int i = 0; i < movies.size(); i++) {
            inboxStyle.addLine(movies.get(i).getCaption());
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, RELEASE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_movie_black)
                .setLargeIcon(largeIcon)
                .setAutoCancel(true)
                .setContentTitle(summaryTitle)
                .setContentText(summaryText)
                .setGroup(RELEASE_GROUP_NAME)
                .setGroupSummary(true)
                .setContentIntent(pendingIntent)
                .setStyle(inboxStyle);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    RELEASE_CHANNEL_ID,
                    RELEASE_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);

            builder.setChannelId(RELEASE_CHANNEL_ID);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        Notification notification = builder.build();

        if (notificationManager != null) {
            notificationManager.notify(0, notification);
        }

    }
}
