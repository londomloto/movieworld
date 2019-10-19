package com.digitalent.submission.movieworld.page;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.digitalent.submission.movieworld.R;
import com.digitalent.submission.movieworld.adapter.TabAdapter;
import com.digitalent.submission.movieworld.databinding.FragmentFavoriteBinding;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("WeakerAccess")
public class FavoriteFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private FragmentFavoriteBinding binding;
    private TabAdapter tabAdapter;

    public FavoriteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorite, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabAdapter = new TabAdapter(getChildFragmentManager());
        tabAdapter.addFragment(new FavoriteMovieFragment(), getString(R.string.title_movies));
        tabAdapter.addFragment(new FavoriteTvFragment(), getString(R.string.title_tv_shows));

        binding.favoriteViewPager.setAdapter(tabAdapter);
        binding.favoriteTabs.setupWithViewPager(binding.favoriteViewPager);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.key_pref_language))) {
            if (tabAdapter != null) {
                tabAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroyView() {
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);

        super.onDestroyView();
    }
}
