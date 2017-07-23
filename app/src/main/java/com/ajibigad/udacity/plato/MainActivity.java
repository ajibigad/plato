package com.ajibigad.udacity.plato;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ajibigad.udacity.plato.adapters.MoviesPagerAdapter;
import com.ajibigad.udacity.plato.events.FetchMovieEvent;
import com.ajibigad.udacity.plato.network.MovieService;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    @BindView(R.id.pager)
    ViewPager viewPager;

    MoviesPagerAdapter moviesPagerAdapter;

    SharedPreferences sharedPreferences;

    private AlertDialog sortOrderDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_all_movies));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.title_favorite_movies)));

        moviesPagerAdapter = new MoviesPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), this);
        viewPager.setAdapter(moviesPagerAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        createSortOrderDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setImeOptions(IME_ACTION_SEARCH);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort_by) {
            //display dialog to change movie sort order
            sortOrderDialog.show();
            return true;
        }
        if (item.getItemId() == R.id.action_refresh) {
            EventBus.getDefault().post(new FetchMovieEvent());
        }
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createSortOrderDialog() {
        final String SORT_CRITERIA_KEY = getString(R.string.pref_sort_criteria_key);
        String prefSortCriteria = sharedPreferences.getString(SORT_CRITERIA_KEY, MovieService.SortCriteria.POPULARITY.name());
        final String[] sortCriteriaNames = new String[MovieService.SortCriteria.values().length];
        int indexOfPrefCriteria = 0;
        int index = 0;
        final String[] selectedOptions = new String[1];
        for (MovieService.SortCriteria sortCriteria : MovieService.SortCriteria.values()) {
            sortCriteriaNames[index] = sortCriteria.name();
            if (sortCriteria.name().equals(prefSortCriteria)) {
                indexOfPrefCriteria = index;
            }
            index++;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sort_by)
                .setSingleChoiceItems(sortCriteriaNames, indexOfPrefCriteria, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedOptions[0] = sortCriteriaNames[which];
                        sharedPreferences.edit().putString(SORT_CRITERIA_KEY, selectedOptions[0]).apply();
                        dialog.dismiss();
                    }
                });
//                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//
//                    }
//                })
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                    }
//                });
        // Create the AlertDialog object and return it
        sortOrderDialog = builder.create();
    }
}
