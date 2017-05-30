package com.ajibigad.udacity.plato.events;

import android.support.annotation.NonNull;

import com.ajibigad.udacity.plato.data.Movie;

/**
 * Created by Julius on 29/05/2017.
 */
public class MovieFetchedEvent {

    private Movie movie;

    public MovieFetchedEvent(@NonNull Movie movie){
        this.movie = movie;
    }

    public Movie getMovie() {
        return movie;
    }
}
