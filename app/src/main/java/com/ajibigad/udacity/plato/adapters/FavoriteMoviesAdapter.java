package com.ajibigad.udacity.plato.adapters;

import android.content.Context;
import android.database.Cursor;

import com.ajibigad.udacity.plato.data.FavoriteMovie;
import com.ajibigad.udacity.plato.data.FavoriteMovieColumns;
import com.ajibigad.udacity.plato.data.Movie;

/**
 * Created by Julius on 13/05/2017.
 */
public class FavoriteMoviesAdapter extends MovieAdapter<Cursor> {

    public FavoriteMoviesAdapter(MovieAdapterOnClickHandler movieAdapterOnClickHandler, Context context) {
        super(movieAdapterOnClickHandler, context);
    }

    @Override
    public Movie getMovieAtPosition(int position) {
        movies.moveToPosition(position);
        return getMovieFromCursor(movies);
    }

    @Override
    public int getMoviesCount() {
        if (null == movies) return 0;
        return movies.getCount();
    }

    private Movie getMovieFromCursor(Cursor cursor) {
        FavoriteMovie movie = new FavoriteMovie();
        movie.setId(cursor.getLong(cursor.getColumnIndex(FavoriteMovieColumns._ID)));
        movie.setTitle(cursor.getString(cursor.getColumnIndex(FavoriteMovieColumns.TITLE)));
        movie.setOverview(cursor.getString(cursor.getColumnIndex(FavoriteMovieColumns.SYNOPSIS)));
        movie.setVoteAverage(cursor.getDouble(cursor.getColumnIndex(FavoriteMovieColumns.USER_RATINGS)));
        movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(FavoriteMovieColumns.DATE_RELEASE)));
        movie.setPopularity(cursor.getDouble(cursor.getColumnIndex(FavoriteMovieColumns.POPULARITY)));
        movie.setPosterImageFileUri(cursor.getString(cursor.getColumnIndex(FavoriteMovieColumns.MOVIE_POSTER_URI)));
        movie.setBackdropImageFileUri(cursor.getString(cursor.getColumnIndex(FavoriteMovieColumns.MOVIE_BACKDROP_URI)));
        return movie;
    }
}
