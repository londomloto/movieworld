package com.digitalent.submission.movieworld.page;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.digitalent.submission.movieworld.DetailActivity;
import com.digitalent.submission.movieworld.R;
import com.digitalent.submission.movieworld.adapter.ListMovieAdapter;
import com.digitalent.submission.movieworld.database.Moviedb;
import com.digitalent.submission.movieworld.databinding.FragmentMovieBinding;
import com.digitalent.submission.movieworld.model.Favorite;
import com.digitalent.submission.movieworld.model.Movie;
import com.digitalent.submission.movieworld.provider.FavoriteProvider;
import com.digitalent.submission.movieworld.util.ExecutorHelper;
import com.digitalent.submission.movieworld.vm.MovieViewModel;
import com.digitalent.submission.movieworld.vm.SearchViewModel;
import com.digitalent.submission.movieworld.widget.FavoriteWidget;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("WeakerAccess")
public class MovieFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private final String STATE_SEARCH_VALUE = "STATE_SEARCH_VALUE";

    private MovieViewModel movieViewModel;
    private FragmentMovieBinding binding;
    private ListMovieAdapter adapter;
    private Boolean pullRefresh = false;
    private String searchValue;

    public MovieFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            searchValue = savedInstanceState.getString(STATE_SEARCH_VALUE);
        }

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_movie, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        movieViewModel = ViewModelProviders.of(this).get(MovieViewModel.class);

        final Context context = view.getContext();
        final Resources resources = context.getResources();
        final Moviedb db = Moviedb.getInstance(context.getApplicationContext());

        adapter = new ListMovieAdapter(movieViewModel);
        adapter.setMovies(new ArrayList<Movie>());
        adapter.notifyDataSetChanged();
        adapter.setOnItemClickHandler(onItemClickHandler);

        binding.recyclerMovie.setHasFixedSize(true);
        binding.recyclerMovie.setLayoutManager(new LinearLayoutManager(context));
        binding.recyclerMovie.setAdapter(adapter);

        binding.swipeMovie.setOnRefreshListener(this);
        binding.swipeMovie.setColorSchemeColors(
                resources.getColor(android.R.color.holo_green_dark),
                resources.getColor(android.R.color.holo_red_dark),
                resources.getColor(android.R.color.holo_blue_dark),
                resources.getColor(android.R.color.holo_orange_dark)
        );

        movieViewModel.getMovies().observe(this, new Observer<ArrayList<Movie>>() {
            @Override
            public void onChanged(ArrayList<Movie> records) {
                adapter.setMovies(records);

                if (records.size() == 0) {
                    showEmpty(true);
                    showRecycler(false);
                } else {
                    showEmpty(false);
                    showRecycler(true);
                }
            }
        });

        movieViewModel.getMoviesLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                if (loading == null) return;

                if (!loading) {
                    showLoading(false);
                    pullRefresh = false;
                    binding.swipeMovie.setRefreshing(false);
                } else {
                    showEmpty(false);
                    if (!pullRefresh) {
                        showLoading(true);
                    }
                }

                showRecycler(!loading);
            }
        });

        movieViewModel.getMoviesError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message == null) return;
                showError(message);
            }
        });

        movieViewModel.getFavorited().observe(this, new Observer<Movie>() {
            @Override
            public void onChanged(final Movie movie) {
                if (movie == null) return;
                if (!movie.getType().equals(Movie.TYPE_MOVIE)) return;

                movieViewModel.postFavorited(null);

                final Favorite favorite = Favorite.createFromMovie(movie);

                ExecutorHelper.getInstance().disk().execute(new Runnable() {
                    @Override
                    public void run() {
                        final Favorite found = db.favoriteDao().findFirst(movie.getId());
                        final String message;

                        if (found == null) {
                            db.favoriteDao().insert(favorite);

                            if (getContext() != null) {
                                getContext().getContentResolver().notifyChange(FavoriteProvider.CONTENT_URI, null);
                                FavoriteWidget.refresh(getContext());
                            }

                            message = movie.getCaption() + " " + getString(R.string.added_to_favorite);
                        } else {
                            message = movie.getCaption() + " " + getString(R.string.already_favorited);
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

            }
        });

        if (getActivity() != null) {
            final SearchViewModel searchViewModel = ViewModelProviders.of(getActivity()).get(SearchViewModel.class);
            searchViewModel.getQuery().observe(this, new Observer<String>() {
                @Override
                public void onChanged(String query) {
                    if (query != null) {
                        if (searchValue != null && searchValue.equals(query)) return;

                        if (query.equals("")) {
                            loadRecords();
                        } else {
                            findRecords(query);
                        }

                        searchValue = query;
                    }
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(STATE_SEARCH_VALUE, searchValue);
    }

    private void loadRecords() {
        movieViewModel.loadMovies("movie", getString(R.string.language_param));
    }

    private void findRecords(String query) {
        movieViewModel.findMovies("movie", getString(R.string.language_param), query);
    }

    private void refresh() {
        if (searchValue != null) {
            if (searchValue.equals("")) {
                loadRecords();
            } else {
                findRecords(searchValue);
            }
        } else {
            loadRecords();
        }
    }

    private void showRecycler(Boolean show) {
        binding.recyclerMovie.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showLoading(Boolean show) {
        binding.progressMovie.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar snackbar = Snackbar.make(this.getView(), message, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    refresh();
                }
            });

            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
            snackbar.show();
        }
    }

    private void showEmpty(Boolean show) {
        binding.txtNodataMovie.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRefresh() {
        pullRefresh = true;
        refresh();
    }

    private final ListMovieAdapter.OnItemClickHandler onItemClickHandler = new ListMovieAdapter.OnItemClickHandler() {
        @Override
        public void onClick(Movie movie) {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_MOVIE_ID, movie.getId());
            intent.putExtra(DetailActivity.EXTRA_MOVIE_TYPE, movie.getType());
            startActivity(intent);
        }
    };
}
