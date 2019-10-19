package com.digitalent.submission.movieworld.page;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.digitalent.submission.movieworld.DetailActivity;
import com.digitalent.submission.movieworld.R;
import com.digitalent.submission.movieworld.adapter.ListFavoriteAdapter;
import com.digitalent.submission.movieworld.database.Moviedb;
import com.digitalent.submission.movieworld.databinding.FragmentFavoriteMovieBinding;
import com.digitalent.submission.movieworld.model.Favorite;
import com.digitalent.submission.movieworld.model.Movie;
import com.digitalent.submission.movieworld.provider.FavoriteProvider;
import com.digitalent.submission.movieworld.util.ExecutorHelper;
import com.digitalent.submission.movieworld.vm.FavoriteViewModel;
import com.digitalent.submission.movieworld.widget.FavoriteWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("WeakerAccess")
public class FavoriteMovieFragment extends Fragment {
    private final static int REQUEST_FAVORITE_ACTION = 100;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private FragmentFavoriteMovieBinding binding;
    private ListFavoriteAdapter adapter;
    private Moviedb db;

    public FavoriteMovieFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorite_movie, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Context context = view.getContext();
        final FavoriteViewModel favoriteViewModel = ViewModelProviders.of(this).get(FavoriteViewModel.class);

        adapter = new ListFavoriteAdapter(favoriteViewModel);

        adapter.setFavorites(new ArrayList<Favorite>());
        adapter.notifyDataSetChanged();
        adapter.setOnItemClickHandler(new ListFavoriteAdapter.OnItemClickHandler() {
            @SuppressWarnings("unused")
            @Override
            public void onClick(Favorite favorite) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_MOVIE_ID, favorite.getId());
                intent.putExtra(DetailActivity.EXTRA_MOVIE_TYPE, favorite.getType());

                startActivityForResult(intent, REQUEST_FAVORITE_ACTION);
            }
        });

        binding.recyclerFavoriteMovie.setHasFixedSize(true);
        binding.recyclerFavoriteMovie.setLayoutManager(new LinearLayoutManager(context));
        binding.recyclerFavoriteMovie.setAdapter(adapter);

        db = Moviedb.getInstance(context.getApplicationContext());

        loadFavorites();

        favoriteViewModel.getRemoved().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(final Integer position) {
                if (position == null) return;

                final Favorite favorite = adapter.getItemAt(position);
                if (favorite == null) return;

                favoriteViewModel.postRemoved(null);

                ExecutorHelper.getInstance().disk().execute(new Runnable() {
                    @Override
                    public void run() {
                        Favorite item =  db.favoriteDao().findFirst(favorite.getId());
                        if (item != null) {
                            db.favoriteDao().delete(item);

                            if (getContext() != null) {
                                getContext().getContentResolver().notifyChange(FavoriteProvider.CONTENT_URI, null);
                                FavoriteWidget.refresh(getContext());
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.removeItemAt(position);
                                }
                            });
                        }
                    }
                });
            }
        });

        if (savedInstanceState == null) {
            loadFavorites();
        }
    }

    private void loadFavorites() {
        final Activity activity = getActivity();

        ExecutorHelper.getInstance().disk().execute(new Runnable() {
            @Override
            public void run() {
                final List<Favorite> items = db.favoriteDao().findByType(Movie.TYPE_MOVIE);
                final ArrayList<Favorite> favorites = new ArrayList<>(items);

                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setFavorites(favorites);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FAVORITE_ACTION) {
            if (resultCode ==  DetailActivity.RESPONSE_FAVORITE_ACTION) {
                if (data != null) {
                    String reloadContext = data.getStringExtra(DetailActivity.EXTRA_FAVORITE_RELOAD);
                    if (reloadContext.equals(Movie.TYPE_MOVIE)) {
                        loadFavorites();
                    }
                }

            }
        }
    }
}
