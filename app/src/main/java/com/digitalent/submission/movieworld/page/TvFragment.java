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
import com.digitalent.submission.movieworld.databinding.FragmentTvBinding;
import com.digitalent.submission.movieworld.model.Favorite;
import com.digitalent.submission.movieworld.model.Movie;
import com.digitalent.submission.movieworld.provider.FavoriteProvider;
import com.digitalent.submission.movieworld.util.ExecutorHelper;
import com.digitalent.submission.movieworld.vm.MovieViewModel;
import com.digitalent.submission.movieworld.vm.SearchViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("WeakerAccess")
public class TvFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private MovieViewModel movieViewModel;
    private FragmentTvBinding binding;
    private Boolean pullRefresh = false;
    private String searchValue;

    public TvFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tv, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        movieViewModel = ViewModelProviders.of(this).get(MovieViewModel.class);

        final Context context = view.getContext();
        final Resources resources = context.getResources();
        final Moviedb db = Moviedb.getInstance(context.getApplicationContext());
        final ListMovieAdapter adapter = new ListMovieAdapter(movieViewModel);
        adapter.setMovies(new ArrayList<Movie>());
        adapter.notifyDataSetChanged();
        adapter.setOnItemClickHandler(onItemClickHandler);

        binding.recyclerTv.setHasFixedSize(true);
        binding.recyclerTv.setLayoutManager(new LinearLayoutManager(context));
        binding.recyclerTv.setAdapter(adapter);

        binding.swipeTv.setOnRefreshListener(this);
        binding.swipeTv.setColorSchemeColors(
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
                    binding.swipeTv.setRefreshing(false);
                    showLoading(false);
                    pullRefresh = false;
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
                if (!movie.getType().equals(Movie.TYPE_TV)) return;

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
                            }
                            message = movie.getCaption() + " " + getString(R.string.added_to_favorite);
                        } else {
                            message = movie.getCaption() + " " + getString(R.string.already_favorited);
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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
                    // searchViewModel.postQuery(null);
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

    private void loadRecords() {
        movieViewModel.loadMovies("tv", getString(R.string.language_param));
    }

    private void findRecords(String query) {
        movieViewModel.findMovies("tv", getString(R.string.language_param), query);
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
        binding.recyclerTv.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showLoading(Boolean show) {
        binding.progressTv.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
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
        binding.txtNodataTv.setVisibility(show ? View.VISIBLE : View.GONE);
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
