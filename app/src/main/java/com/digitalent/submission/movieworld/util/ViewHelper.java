package com.digitalent.submission.movieworld.util;

import android.content.Context;

import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.digitalent.submission.movieworld.R;

public class ViewHelper {

    public static CircularProgressDrawable createLoading(Context context) {
        CircularProgressDrawable loading = new CircularProgressDrawable(context);

        loading.setStrokeWidth(5f);
        loading.setCenterRadius(20f);
        loading.setColorSchemeColors(
                context.getResources().getColor(R.color.colorAccent)
        );

        loading .start();
        return loading;
    }

}
