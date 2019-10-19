package com.digitalent.submission.movieworld.vm;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.digitalent.submission.movieworld.api.Tmdb;
import com.digitalent.submission.movieworld.model.Movie;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailViewModel extends ViewModel {
    private final MutableLiveData<Movie> movie = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Movie> favoriteRequest = new MutableLiveData<>();

    public LiveData<Movie> getMovie() {
        return movie;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Movie> getFavoriteRequest() {
        return favoriteRequest;
    }

    public void loadMovie(int id, String type, String language) {

        loading.postValue(true);

        Callback<Movie> callback = new Callback<Movie>() {
            @Override
            public void onResponse(@NonNull Call<Movie> call, Response<Movie> response) {
                loading.postValue(false);

                if (response.isSuccessful()) {
                    movie.postValue(response.body());
                    error.postValue(null);
                } else {
                    movie.postValue(null);
                    try {
                        ResponseBody body = response.errorBody();
                        if (body != null) {
                            JSONObject json = new JSONObject(body.string());
                            error.postValue(json.getString("status_message"));
                        }
                    } catch (Exception e) {
                        error.postValue(e.getMessage());
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<Movie> call, Throwable t) {
                loading.postValue(false);
                error.postValue(t.getMessage());
                movie.postValue(null);
            }
        };

        Tmdb.getApi().fetch(type, id, Tmdb.API_KEY, language).enqueue(callback);
    }

    public void postFavoriteRequest(Movie movie) {
        favoriteRequest.postValue(movie);
    }

    public void onFavoritedClick(Movie movie) {
        postFavoriteRequest(movie);
    }
}
