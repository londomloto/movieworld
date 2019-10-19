package com.digitalent.submission.movieworld;

import android.app.Application;
import android.content.Context;

import com.digitalent.submission.movieworld.util.LocaleHelper;

@SuppressWarnings("WeakerAccess")
public class BaseApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }
}
