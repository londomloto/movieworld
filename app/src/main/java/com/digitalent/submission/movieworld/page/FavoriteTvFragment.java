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
import com.digitalent.submission.movieworld.databinding.FragmentFavoriteTvBinding;
import com.digitalent.submission.movieworld.model.Favorite;
import com.digitalent.submission.movieworld.model.Movie;
import com.digitalent.submission.movieworld.provider.FavoriteProvider;
import com.digitalent.submission.movieworld.util.ExecutorHelper;
import com.digitalent.submission.movieworld.vm.FavoriteViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("WeakerAccess")
public class FavoriteTvFragment extends Fragment {
    private final static int REQUEST_FAVORITE_ACTION = 200;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private FragmentFavoriteTvBinding binding;
    private Moviedb db;
    private ListFavoriteAdapter adapter;


    public FavoriteTvFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorite_tv, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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

        binding.recyclerFavoriteTv.setHasFixedSize(true);
        binding.recyclerFavoriteTv.setLayoutManager(new LinearLayoutManager(context));
        binding.recyclerFavoriteTv.setAdapter(adapter);

        db = Moviedb.getInstance(context.getApplicationContext());

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
                final List<Favorite> favs = db.favoriteDao().findByType(Movie.TYPE_TV);
                final ArrayList<Favorite> favorites = new ArrayList<>(favs);

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
                    if (reloadContext.equals(Movie.TYPE_TV)) {
                        loadFavorites();
                    }
                }
            }
        }
    }
}
