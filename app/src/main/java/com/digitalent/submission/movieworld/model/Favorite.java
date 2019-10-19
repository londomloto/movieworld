package com.digitalent.submission.movieworld.model;

import android.content.ContentValues;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.digitalent.submission.movieworld.util.DateHelper;
import com.digitalent.submission.movieworld.util.ViewHelper;

@SuppressWarnings("WeakerAccess")
@Entity(tableName = Favorite.TABLE_NAME)
public class Favorite {

    public static final String TABLE_NAME = "favorite";

    @PrimaryKey()
    private int id;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name="title")
    private String title;

    @ColumnInfo(name="release_date")
    private String release_date;

    @ColumnInfo(name = "overview")
    private String overview;

    @ColumnInfo(name = "poster")
    private String poster;

    @Ignore
    private String language;

    @Ignore
    public String getLanguage() {
        return language;
    }

    @Ignore
    public void setLanguage(String language) {
        this.language = language;
    }

    @Ignore
    public Favorite() {}

    public Favorite(int id, String type, String title, String release_date, String overview, String poster) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.release_date = release_date;
        this.overview = overview;
        this.poster = poster;
    }

    @Ignore
    public static Favorite createFromMovie(Movie movie) {
        Favorite favorite = new Favorite();

        favorite.setId(movie.getId());
        favorite.setType(movie.getType());
        favorite.setTitle(movie.getCaption());

        if (movie.isMovie()) {
            favorite.setRelease_date(movie.getRelease_date());
        } else {
            favorite.setRelease_date(movie.getFirst_air_date());
        }

        favorite.setOverview(movie.getOverview());
        favorite.setPoster(movie.getPoster());

        return favorite;
    }

    @Ignore
    public static Favorite createFormContentValues(ContentValues values) {
        final Favorite favorite = new Favorite();

        if (values.containsKey("id")) {
            favorite.setId(values.getAsInteger("id"));
        }

        if (values.containsKey("type")) {
            String type = values.getAsString("type");
            favorite.setType(type);

            if (type.equals(Movie.TYPE_MOVIE)) {
                if (values.containsKey("release_date")) {
                    favorite.setRelease_date(values.getAsString("release_date"));
                }
            } else {
                if (values.containsKey("first_air_date")) {
                    favorite.setRelease_date(values.getAsString("first_air_date"));
                }
            }
        }

        if (values.containsKey("title")) {
            favorite.setTitle(values.getAsString("title"));
        }

        if (values.containsKey("overview")) {
            favorite.setOverview(values.getAsString("overview"));
        }

        if (values.containsKey("poster")) {
            favorite.setPoster(values.getAsString("poster"));
        }

        return favorite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    @BindingAdapter({"favoritePoster"})
    public static void renderPoster(ImageView view, String poster) {
        Glide.with(view)
                .load(poster)
                .apply(new RequestOptions()
                        .override(100, 150)
                        .placeholder(ViewHelper.createLoading(view.getContext())))
                .into(view);
    }

    @BindingAdapter({"favoriteRelease"})
    public static void renderRelease(TextView view, Favorite favorite) {
        view.setText(DateHelper.formatDate(favorite.release_date, favorite.getLanguage()));
    }
}
