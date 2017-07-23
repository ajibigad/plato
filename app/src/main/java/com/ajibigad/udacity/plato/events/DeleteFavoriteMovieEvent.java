package com.ajibigad.udacity.plato.events;

import android.support.annotation.NonNull;

import com.ajibigad.udacity.plato.data.FavoriteMovie;

/**
 * Created by Julius on 12/05/2017.
 */
public class DeleteFavoriteMovieEvent {

    private FavoriteMovie movie;

    public DeleteFavoriteMovieEvent(@NonNull FavoriteMovie movie) {
        this.movie = movie;
    }

    public FavoriteMovie getMovie() {
        return movie;
    }

    public void setMovie(FavoriteMovie movie) {
        this.movie = movie;
    }
}
