package com.ajibigad.udacity.plato.events;

import com.ajibigad.udacity.plato.data.Movie;

/**
 * Created by Julius on 12/05/2017.
 */
public class AddFavoriteMovieEvent {

    private Movie movie;

    // make sure this is the application context and not an activity's to prevent memory leak
    public AddFavoriteMovieEvent(Movie movie){
        this.movie = movie;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }
}
