package com.ajibigad.udacity.plato.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ajibigad.udacity.plato.AllMoviesFragment;
import com.ajibigad.udacity.plato.FavoriteMoviesFragment;
import com.ajibigad.udacity.plato.R;

import java.util.Locale;

/**
 * Created by Julius on 13/05/2017.
 */
public class MoviesPagerAdapter extends FragmentPagerAdapter {

    private int tabCount;
    private Context context;

    public MoviesPagerAdapter(FragmentManager fragmentManager, int tabCount, Context context) {
        super(fragmentManager);
        this.tabCount = tabCount;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new AllMoviesFragment();
            case 1:
                return new FavoriteMoviesFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return tabCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale locale = Locale.getDefault();
        switch (position) {
            case 0:
                return "";
            case 1:
                return context.getString(R.string.title_favorite_movies).toUpperCase(locale);
        }
        return null;
    }
}
