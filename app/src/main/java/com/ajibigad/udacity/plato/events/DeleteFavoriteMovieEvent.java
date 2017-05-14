package com.ajibigad.udacity.plato.events;

import com.ajibigad.udacity.plato.data.Movie;

/**
 * Created by Julius on 12/05/2017.
 */
public class DeleteFavoriteMovieEvent {

    private Movie movie;

    public DeleteFavoriteMovieEvent(Movie movie) {
        this.movie = movie;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }
}
