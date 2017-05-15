package com.ajibigad.udacity.plato.adapters;

import android.content.Context;

import com.ajibigad.udacity.plato.data.Movie;

import java.util.List;

/**
 * Created by Julius on 13/05/2017.
 */
public class AllMoviesAdapter extends MovieAdapter<List<Movie>> {

    public AllMoviesAdapter(MovieAdapterOnClickHandler movieAdapterOnClickHandler, Context context) {
        super(movieAdapterOnClickHandler, context);
    }

    @Override
    public Movie getMovieAtPosition(int position) {
        return movies.get(position);
    }

    @Override
    public int getMoviesCount() {
        if (null == movies) return 0;
        return movies.size();
    }
}
