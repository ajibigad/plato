package com.ajibigad.udacity.plato.data;

import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by Julius on 10/05/2017.
 */
public interface FavoriteMovieColumns {
    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    String _ID = "_id"; //would be same with id from api

    @DataType(DataType.Type.TEXT)
    @NotNull
    String TITLE = "title";

    @DataType(DataType.Type.TEXT)
    @NotNull
    String SYNOPSIS = "synopsis";

    @DataType(DataType.Type.REAL)
    @NotNull
    String USER_RATINGS = "user_ratings";

    @DataType(DataType.Type.TEXT)
    @NotNull
    String DATE_RELEASE = "date_release";

    @DataType(DataType.Type.TEXT)
    @NotNull
    String MOVIE_POSTER_URI = "movie_poster_uri";

    @DataType(DataType.Type.REAL)
    @NotNull
    String POPULARITY = "popularity";

}
