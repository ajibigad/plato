package com.ajibigad.udacity.plato.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by Julius on 10/05/2017.
 */
@ContentProvider(authority = FavoriteMovieProvider.AUTHORITY, database = FavoriteMovieDatabase.class)
public class FavoriteMovieProvider {
    public static final String AUTHORITY =
            "com.ajibigad.udacity.plato.data.FavoriteMovieProvider";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String FAVORITE_MOVIES = "favorite_movies";
    }

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = FavoriteMovieDatabase.FAVORITE_MOVIES)
    public static class FavoriteMovies {
        @ContentUri(
                path = Path.FAVORITE_MOVIES,
                type = "vnd.android.cursor.dir/favorite_movies",
                defaultSort = FavoriteMovieColumns.USER_RATINGS + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.FAVORITE_MOVIES);

        @InexactContentUri(
                name = "FAVORITE_MOVIE_ID",
                path = Path.FAVORITE_MOVIES + "/#",
                type = "vnd.android.cursor.item/favorite_movie",
                whereColumn = FavoriteMovieColumns._ID,
                pathSegment = 1)
        public static Uri withId(long id) {
            return buildUri(Path.FAVORITE_MOVIES, String.valueOf(id));
        }
    }

}
