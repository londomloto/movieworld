package com.digitalent.submission.movieworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.digitalent.submission.movieworld.database.Moviedb;
import com.digitalent.submission.movieworld.databinding.ActivityDetailBinding;
import com.digitalent.submission.movieworld.model.Favorite;
import com.digitalent.submission.movieworld.model.Movie;
import com.digitalent.submission.movieworld.provider.FavoriteProvider;
import com.digitalent.submission.movieworld.util.ExecutorHelper;
import com.digitalent.submission.movieworld.vm.DetailViewModel;
import com.digitalent.submission.movieworld.widget.FavoriteWidget;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

public class DetailActivity extends AppCompatActivity {
    public final static String EXTRA_MOVIE_ID = "MOVIE_ID";
    public final static String EXTRA_MOVIE_TYPE = "MOVIE_TYPE";
    public final static String EXTRA_FAVORITE_RELOAD = "FAVORITE_RELOAD";

    public final static int RESPONSE_FAVORITE_ACTION = 300;

    private ActivityDetailBinding binding;
    private DetailViewModel detailViewModel;
    private int movieId = 0;
    private String movieType;
    private Menu menu;
    private Moviedb db;
    private Movie currentMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);
        db = Moviedb.getInstance(getApplicationContext());

        binding.appBarDetail.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    showFavoriteMenu(true);
                } else if (isShow) {
                    isShow = false;
                    showFavoriteMenu(false);
                }
            }
        });

        Intent intent = getIntent();
        movieId = intent.getIntExtra(EXTRA_MOVIE_ID, 0);
        movieType = intent.getStringExtra(EXTRA_MOVIE_TYPE);

        setSupportActionBar(binding.toolbarDetail);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(movieType.equals(Movie.TYPE_MOVIE) ? R.string.title_detail_movie : R.string.title_detail_tv);
        }

        detailViewModel = ViewModelProviders.of(this).get(DetailViewModel.class);

        binding.setViewmodel(detailViewModel);
        binding.executePendingBindings();

        detailViewModel.getMovie().observe(this, new Observer<Movie>() {
            @Override
            public void onChanged(Movie movie) {
                currentMovie = null;
                if (movie == null) return;

                movie.setLanguage(getString(R.string.language_param));

                binding.setMovie(movie);
                binding.executePendingBindings();

                validateFavorite(movie);
                currentMovie = movie;
            }
        });

        detailViewModel.getError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String error) {
                if (error == null) return;
                showError(error);
            }
        });

        detailViewModel.getLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                if (loading == null) return;
                showContent(!loading);
                showLoading(loading);
            }
        });

        detailViewModel.getFavoriteRequest().observe(this, new Observer<Movie>() {
            @Override
            public void onChanged(Movie movie) {
                if (movie == null) return;
                detailViewModel.postFavoriteRequest(null);

                toggleFavorite(movie);
            }
        });

        if (savedInstanceState == null) {
            loadRecord();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        showFavoriteMenu(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_favorite) {
            if (currentMovie != null) {
                toggleFavorite(currentMovie);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadRecord() {
        if (movieId == 0) return;
        detailViewModel.loadMovie(movieId, movieType, getString(R.string.language_param));
    }

    private void showError(String message) {
        Snackbar snackbar = Snackbar.make(binding.layoutDetail, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadRecord();
            }
        });

        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }

    private void showLoading(Boolean show) {
        binding.content.progressDetail.setVisibility(show ? View.VISIBLE : View.GONE);

        if (show) {
            binding.fabDetail.hide();
        } else {
            binding.fabDetail.show();
        }
    }

    private void showContent(Boolean show) {
        binding.content.contentDetail.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showFavoriteMenu(Boolean show) {
        if (menu == null) return;

        MenuItem item = menu.findItem(R.id.action_favorite);
        item.setVisible(show);
    }

    private void validateFavorite(final Movie movie) {
        ExecutorHelper.getInstance().disk().execute(new Runnable() {
            @Override
            public void run() {
                Favorite found = db.favoriteDao().findFirst(movie.getId());

                if (found != null) {
                    movie.setFavorited(true);
                } else {
                    movie.setFavorited(false);
                }
            }
        });
    }

    private void toggleFavorite(final Movie movie) {
        ExecutorHelper.getInstance().disk().execute(new Runnable() {
            @Override
            public void run() {
                final Favorite found = db.favoriteDao().findFirst(movie.getId());
                final String message;

                if (found != null) {
                    db.favoriteDao().delete(found);
                    movie.setFavorited(false);
                    message = String.format("%s %s", movie.getCaption(), getString(R.string.removed_from_favorite));
                } else {

                    final Favorite favorite = Favorite.createFromMovie(movie);
                    db.favoriteDao().insert(favorite);
                    movie.setFavorited(true);
                    message = String.format("%s %s", movie.getCaption(), getString(R.string.added_to_favorite));
                }

                getContentResolver().notifyChange(FavoriteProvider.CONTENT_URI, null);
                FavoriteWidget.refresh(DetailActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_FAVORITE_RELOAD, movie.getType());
                        setResult(RESPONSE_FAVORITE_ACTION, intent);

                        Toast.makeText(DetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}
