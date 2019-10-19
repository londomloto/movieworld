package com.digitalent.submission.movieworld.vm;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.digitalent.submission.movieworld.api.Tmdb;
import com.digitalent.submission.movieworld.model.Discover;
import com.digitalent.submission.movieworld.model.Movie;

import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<Movie>> movies = new MutableLiveData<>();
    private final MutableLiveData<String> moviesError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> moviesLoading = new MutableLiveData<>();

    private final MutableLiveData<Movie> favorited = new MutableLiveData<>();

    private final ArrayList<Movie> empty = new ArrayList<>();

    public void loadMovies(String type, String language) {
        moviesLoading.postValue(true);

        Callback<Discover> callback = new Callback<Discover>() {
            @Override
            public void onResponse(@NonNull Call<Discover> call, Response<Discover> response) {
                moviesLoading.postValue(false);

                if (response.isSuccessful()) {
                    moviesError.postValue(null);
                    Discover body = response.body();
                    if (body != null) {
                        movies.postValue(body.getResults());
                    } else {
                        movies.postValue(empty);
                    }
                } else {
                    movies.postValue(empty);
                    try {
                        ResponseBody body = response.errorBody();
                        if (body != null) {
                            JSONObject json = new JSONObject(body.string());
                            moviesError.postValue(json.getString("status_message"));
                        }
                    } catch (Exception e) {
                        moviesError.postValue(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Discover> call, Throwable t) {
                moviesLoading.postValue(false);
                moviesError.postValue(t.getMessage());
                movies.postValue(empty);
            }
        };

        Tmdb.getApi().discover(type, Tmdb.API_KEY, language).enqueue(callback);
    }

    public void findMovies(String type, String language, String query) {
        moviesLoading.postValue(true);

        Callback<Discover> callback = new Callback<Discover>() {
            @Override
            public void onResponse(@NonNull Call<Discover> call, @NonNull Response<Discover> response) {
                moviesLoading.postValue(false);

                if (response.isSuccessful()) {
                    moviesError.postValue(null);
                    Discover body = response.body();
                    if (body != null) {
                        movies.postValue(body.getResults());
                    } else {
                        movies.postValue(empty);
                    }
                } else {
                    movies.postValue(empty);
                    try {
                        ResponseBody body = response.errorBody();
                        if (body != null) {
                            JSONObject json = new JSONObject(body.string());
                            moviesError.postValue(json.getString("status_message"));
                        }
                    } catch (Exception e) {
                        moviesError.postValue(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Discover> call, Throwable t) {
                moviesLoading.postValue(false);
                moviesError.postValue(t.getMessage());
                movies.postValue(empty);
            }
        };

        Tmdb.getApi().search(type, Tmdb.API_KEY, language, query).enqueue(callback);
    }

    public LiveData<ArrayList<Movie>> getMovies() { return movies; }
    public LiveData<String> getMoviesError() { return moviesError; }
    public LiveData<Boolean> getMoviesLoading() { return moviesLoading; }
    public LiveData<Movie> getFavorited() { return favorited; }

    public void postFavorited(Movie movie) {
        favorited.postValue(movie);
    }

    public void onFavoriteClick(Movie movie) {
        postFavorited(movie);
    }

}
