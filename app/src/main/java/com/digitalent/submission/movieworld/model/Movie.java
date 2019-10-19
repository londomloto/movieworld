package com.digitalent.submission.movieworld.model;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.digitalent.submission.movieworld.R;
import com.digitalent.submission.movieworld.api.Tmdb;
import com.digitalent.submission.movieworld.util.DateHelper;
import com.digitalent.submission.movieworld.util.ViewHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Movie extends BaseObservable {
    public static final String TYPE_MOVIE = "movie";
    public static final String TYPE_TV = "tv";

    private int id;
    private String title;
    private String name;
    private String overview;
    private String poster_path;
    private String backdrop_path;
    private String release_date;
    private String first_air_date;
    private String language;
    private Boolean favorited;

    public Boolean isMovie() {
        return name == null;
    }

    public String getType() {
        return isMovie() ? TYPE_MOVIE : TYPE_TV;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getCaption() {
        return isMovie() ? title : name;
    }

    public String getOverview() {
        return overview;
    }
    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPoster_path() {
        return poster_path;
    }
    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getPoster() {
        return String.format("%sw154%s", Tmdb.BASE_IMAGE_URL, this.poster_path);
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public void setBackdrop_path(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public String getBackdrop() {
        return String.format("%sw780%s", Tmdb.BASE_IMAGE_URL, this.backdrop_path);
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getFirst_air_date() {
        return first_air_date;
    }

    public void setFirst_air_date(String first_air_date) {
        this.first_air_date = first_air_date;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Bindable
    public Boolean getFavorited() {
        return favorited;
    }

    public void setFavorited(Boolean favorited) {
        this.favorited = favorited;
        notifyPropertyChanged(com.digitalent.submission.movieworld.BR.favorited);
    }

    @BindingAdapter({"movieRelease"})
    public static void renderRelease(TextView view, Movie movie) {
        if (movie == null) return;
        String date = movie.isMovie() ? movie.getRelease_date() : movie.getFirst_air_date();
        view.setText(DateHelper.formatDate(date, movie.getLanguage()));
    }

    @BindingAdapter({"moviePoster"})
    public static void renderMoviePoster(ImageView view, String poster) {
        Glide.with(view)
                .load(poster)
                .apply(new RequestOptions()
                        .override(100, 150)
                        .placeholder(ViewHelper.createLoading(view.getContext())))
                .into(view);
    }

    @BindingAdapter({"movieBackdrop"})
    public static void renderMovieBackdrop(ImageView view, String poster) {
        Glide.with(view)
                .load(poster)
                .into(view);
    }

    @BindingAdapter({"movieFavorited"})
    public static void updateBackground(View button, Boolean favorited) {
        if (favorited == null) return;

        FloatingActionButton fab = (FloatingActionButton) button;
        Resources res = button.getResources();

        if (favorited) {
            fab.setBackgroundTintList(ColorStateList.valueOf(res.getColor(R.color.colorPrimary)));
        } else {
            fab.setBackgroundTintList(ColorStateList.valueOf(res.getColor(R.color.colorAccent)));
        }

    }

}
