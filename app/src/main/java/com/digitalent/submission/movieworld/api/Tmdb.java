package com.digitalent.submission.movieworld.api;

import com.digitalent.submission.movieworld.BuildConfig;
import com.digitalent.submission.movieworld.model.Discover;
import com.digitalent.submission.movieworld.model.Movie;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class Tmdb {
    public static final String API_KEY = BuildConfig.TMDB_API_KEY;
    public static final String BASE_IMAGE_URL = "https://image.tmdb.org/t/p/";
    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    private static ApiInterface api;

    public static ApiInterface getApi() {
        if (api == null) {
            OkHttpClient client = new OkHttpClient.Builder().build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            api = retrofit.create(ApiInterface.class);
        }

        return api;
    }

    public interface ApiInterface {
        @GET("discover/{type}")
        Call<Discover> discover(
                @Path("type") String type,
                @Query("api_key") String apiKey,
                @Query("language") String language);

        @GET("discover/movie")
        Call<Discover> discoverRelease(
                @Query("api_key") String apiKey,
                @Query("primary_release_date.gte") String fromDate,
                @Query("primary_release_date.lte") String toDate);

        @GET("search/{type}")
        Call<Discover> search(
                @Path("type") String type,
                @Query("api_key") String apiKey,
                @Query("language") String language,
                @Query("query") String query);

        @GET("{type}/{id}")
        Call<Movie> fetch(
                @Path("type") String type,
                @Path("id") int id,
                @Query("api_key") String apiKey,
                @Query("language") String language);


    }
}

