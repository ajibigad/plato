package com.ajibigad.udacity.plato.events;

import com.ajibigad.udacity.plato.data.FavoriteMovie;
import com.ajibigad.udacity.plato.data.Movie;

/**
 * Created by Julius on 12/05/2017.
 */
public class NewFavoriteMovieAdded {

    private FavoriteMovie favoriteMovie;

    public NewFavoriteMovieAdded(FavoriteMovie favoriteMovie){
        this.favoriteMovie = favoriteMovie;
    }

    public FavoriteMovie getFavoriteMovie() {
        return favoriteMovie;
    }
}
