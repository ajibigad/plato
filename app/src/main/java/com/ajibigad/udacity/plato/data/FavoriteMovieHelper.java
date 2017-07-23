package com.ajibigad.udacity.plato.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.ajibigad.udacity.plato.events.AddFavoriteMovieEvent;
import com.ajibigad.udacity.plato.events.DeleteFavoriteMovieEvent;
import com.ajibigad.udacity.plato.events.FavoriteMovieDeletedEvent;
import com.ajibigad.udacity.plato.events.NewFavoriteMovieAdded;
import com.ajibigad.udacity.plato.network.ImageSize;
import com.ajibigad.udacity.plato.network.MovieService;
import com.ajibigad.udacity.plato.utils.ImageHelper;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

/**
 * Created by Julius on 12/05/2017.
 */
public class FavoriteMovieHelper {

    private static final String TAG = FavoriteMovieHelper.class.getSimpleName();
    private Context context;

    public FavoriteMovieHelper(Context context) {
        this.context = context;
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void addMovieToFavoriteMovies(AddFavoriteMovieEvent addFavoriteMovieEvent) {
        FavoriteMovie movie = addFavoriteMovieEvent.getMovie();
        Bitmap moviePosterBitmap = null, movieBackdropBitmap;
        try {
            moviePosterBitmap = Picasso.with(context)
                    .load(MovieService.getImageFullLink(movie.getPosterPath(), ImageSize.W342))
                    .get();
            movieBackdropBitmap = Picasso.with(context)
                    .load(MovieService.getImageFullLink(movie.getBackdropPath(), ImageSize.W500))
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String moviePosterAbsolutePath = ImageHelper.saveImageToFile(context, movie, moviePosterBitmap);
        String movieBackdropAbsolutePath = ImageHelper.saveImageToFile(context, movie, movieBackdropBitmap);

        ContentValues contentValues = new ContentValues();
        contentValues.put(FavoriteMovieColumns._ID, movie.getId());
        contentValues.put(FavoriteMovieColumns.TITLE, movie.getTitle());
        contentValues.put(FavoriteMovieColumns.SYNOPSIS, movie.getOverview());
        contentValues.put(FavoriteMovieColumns.USER_RATINGS, movie.getVoteAverage());
        contentValues.put(FavoriteMovieColumns.DATE_RELEASE, movie.getReleaseDate());
        contentValues.put(FavoriteMovieColumns.POPULARITY, movie.getPopularity());
        contentValues.put(FavoriteMovieColumns.MOVIE_POSTER_URI, moviePosterAbsolutePath);
        contentValues.put(FavoriteMovieColumns.MOVIE_BACKDROP_URI, movieBackdropAbsolutePath);
        Uri uri = context.getContentResolver().insert(FavoriteMovieProvider.FavoriteMovies.CONTENT_URI, contentValues);
        if (uri != null) {
            movie.setPosterImageFileUri(moviePosterAbsolutePath);
            movie.setBackdropImageFileUri(movieBackdropAbsolutePath);
            EventBus.getDefault().post(new NewFavoriteMovieAdded(movie));
            Log.i(TAG, "Favorite movie added : " + uri.toString());
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void deleteMovieFromFavoriteMovies(DeleteFavoriteMovieEvent deleteFavoriteMovieEvent) {
        FavoriteMovie movie = deleteFavoriteMovieEvent.getMovie();
        ImageHelper.deleteMovieImage(movie.getBackdropImageFileUri());
        ImageHelper.deleteMovieImage(movie.getPosterImageFileUri());
        int deletedCount = context.getContentResolver().delete(FavoriteMovieProvider.FavoriteMovies.withId(movie.getId()), null, null);
        if (deletedCount > 0) {
            EventBus.getDefault().post(new FavoriteMovieDeletedEvent());
        } else {
            Log.e(TAG, "Failed to delete movie with id: " + movie.getId());
        }
    }

    public static boolean MovieExists(Context context, long movieID){
//        String[] projection = {FavoriteMovieColumns._ID};
//        String[] selection = {""}
        Cursor cursor = context.getContentResolver().query(FavoriteMovieProvider.FavoriteMovies.withId(movieID), null, null, null, null);
        return cursor != null && cursor.getCount() == 1;
    }
}
