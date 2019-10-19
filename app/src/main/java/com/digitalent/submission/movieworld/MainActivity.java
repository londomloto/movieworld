package com.digitalent.submission.movieworld;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.digitalent.submission.movieworld.databinding.ActivityMainBinding;
import com.digitalent.submission.movieworld.receiver.AlarmReceiver;
import com.digitalent.submission.movieworld.util.LocaleHelper;
import com.digitalent.submission.movieworld.vm.SearchViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final String STATE_SEARCH_VALUE = "STATE_SEARCH_VALUE";
    private final String STATE_SEARCH_VALUE_DEBOUNCED = "SEARCH_VALUE_DEBOUNCED";

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;

    private final Handler searchHandler = new Handler();
    private String searchValue;
    private String searchValueDebounced;
    private Runnable searchRunner;
    private SearchViewModel searchViewModel;

    private SharedPreferences sharedPreferences;
    private AlarmReceiver alarmReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        resolveLanguage();

        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        alarmReceiver = new AlarmReceiver();

        if (savedInstanceState != null) {
            searchValue = savedInstanceState.getString(STATE_SEARCH_VALUE);
            searchValueDebounced = savedInstanceState.getString(STATE_SEARCH_VALUE_DEBOUNCED);
        }

        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel.class);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        setupBottomNavigation();
        setupAlarm();

        if (savedInstanceState == null) {
            searchViewModel.postQuery("");
        }
    }

    private void setupAlarm() {
        final boolean dailyReminder = sharedPreferences.getBoolean(getString(R.string.key_pref_daily_reminder), true);

        if (dailyReminder) {
            alarmReceiver.setupDailyReminder(this);
        } else {
            alarmReceiver.cancelDailyReminder(this);
        }

        final boolean releaseReminder = sharedPreferences.getBoolean(getString(R.string.key_pref_release_reminder), true);

        if (releaseReminder) {
            alarmReceiver.setupReleaseReminder(this);
        } else {
            alarmReceiver.cancelReleaseReminder(this);
        }
    }

    private void resolveLanguage() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String language = sharedPreferences.getString(getString(R.string.key_pref_language), LocaleHelper.getLanguage(this));
        LocaleHelper.setLocale(this, language);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(STATE_SEARCH_VALUE, searchValue);
        bundle.putString(STATE_SEARCH_VALUE_DEBOUNCED, searchValueDebounced);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = binding.bottomNavigation;

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_movie,
                R.id.navigation_tv,
                R.id.navigation_favorite).build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_main);

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if (destination.getId() == R.id.navigation_favorite) {
                    showAppBarShadow(false);
                } else {
                    showAppBarShadow(true);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        if (searchManager != null) {
            MenuItem searchItem = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setQueryHint(getResources().getString(R.string.search_hint));

            if (searchValue != null && !searchValue.isEmpty()) {
                searchItem.expandActionView();
                searchView.setQuery(searchValue, false);
                searchView.clearFocus();
            }

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchViewModel.postQuery(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    searchValue = query;
                    searchHandler.removeCallbacks(searchRunner);
                    searchRunner = new Runnable() {
                        @Override
                        public void run() {
                            if (searchValue.length() == 0) {
                                if (searchValueDebounced != null) {
                                    searchViewModel.postQuery("");
                                }
                            } else {
                                searchViewModel.postQuery(searchValue);
                            }
                            searchValueDebounced = searchValue;
                        }
                    };

                    searchHandler.postDelayed(searchRunner, 500);
                    return true;
                }
            });

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    private void showAppBarShadow(Boolean show) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setElevation(show ? 8 : 0);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String keyLanguage = getString(R.string.key_pref_language);
        String keyDailyReminder = getString(R.string.key_pref_daily_reminder);
        String keyReleaseReminder = getString(R.string.key_pref_release_reminder);

        if (key.equals(keyLanguage)) {
            String language = sharedPreferences.getString(keyLanguage, LocaleHelper.getLanguage(this));

            LocaleHelper.setLocale(MainActivity.this, language);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    NavDestination destination = navController.getCurrentDestination();
                    if (destination != null) {
                        navController.navigate(destination.getId());
                    }

                    MainActivity.this.recreate();
                }
            }, 1);
        }

        if (key.equals(keyDailyReminder)) {
            final boolean dailyReminder = sharedPreferences.getBoolean(keyDailyReminder, true);
            if (dailyReminder) {
                alarmReceiver.setupDailyReminder(this);
            } else {
                alarmReceiver.cancelDailyReminder(this);
            }
        }

        if (key.equals(keyReleaseReminder)) {
            final boolean releaseReminder = sharedPreferences.getBoolean(keyReleaseReminder, true);

            if (releaseReminder) {
                alarmReceiver.setupReleaseReminder(this);
            } else {
                alarmReceiver.cancelReleaseReminder(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

        super.onDestroy();
    }
}
