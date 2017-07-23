package com.ajibigad.udacity.plato.events;

import com.ajibigad.udacity.plato.data.FavoriteMovie;

/**
 * Created by Julius on 12/05/2017.
 */
public class AddFavoriteMovieEvent {

    private FavoriteMovie movie;

    public AddFavoriteMovieEvent(FavoriteMovie movie) {
        this.movie = movie;
    }

    public FavoriteMovie getMovie() {
        return movie;
    }

    public void setMovie(FavoriteMovie movie) {
        this.movie = movie;
    }
}
