package com.ajibigad.udacity.plato.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ajibigad.udacity.plato.MovieInfoFragment;
import com.ajibigad.udacity.plato.ReviewFragment;
import com.ajibigad.udacity.plato.TrailerFragment;

/**
 * Created by Julius on 29/05/2017.
 */
public class MovieDetailsPagerAdapter extends FragmentPagerAdapter {
    private int tabCount;
    private Context context;

    public MovieDetailsPagerAdapter(FragmentManager fragmentManager, int tabCount, Context context) {
        super(fragmentManager);
        this.tabCount = tabCount;
        this.context = context;
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MovieInfoFragment();
            case 1:
                return new TrailerFragment();
            case 2:
                return new ReviewFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
