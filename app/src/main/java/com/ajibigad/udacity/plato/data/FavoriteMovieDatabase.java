package com.ajibigad.udacity.plato.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by Julius on 10/05/2017.
 */
@Database(version = FavoriteMovieDatabase.VERSION)
public class FavoriteMovieDatabase {

    public static final int VERSION = 1;

    @Table(FavoriteMovieColumns.class)
    public static final String FAVORITE_MOVIES = "favorite_movies";
}
