package com.ajibigad.udacity.plato.network;

import android.util.Log;

import com.ajibigad.udacity.plato.BuildConfig;
import com.ajibigad.udacity.plato.data.FavoriteMovie;
import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.data.MoviePagedResponse;
import com.ajibigad.udacity.plato.utils.SortOrderResolver;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Julius on 13/04/2017.
 */
public class MovieService implements SortOrderResolver {

    public final static String API_BASE_URL = "https://api.themoviedb.org/3/";
    public final static String IMAGE_API_BASE_URL = "https://image.tmdb.org/t/p/";
    private static String API_KEY = BuildConfig.API_KEY;

    private static final String TAG = MovieService.class.getSimpleName();

    public static String getPosterImagePath(Movie movie) {
        if (movie instanceof FavoriteMovie) {
            Log.i(TAG, "Movie is instance of Favorite Movie");
            return ((FavoriteMovie) movie).getPosterImageFileUri();
        } else {
            return MovieService.getImageFullLink(movie.getPosterPath(), ImageSize.W342);
        }
    }

    public static String getBackdropImagePath(Movie movie) {
        if (movie instanceof FavoriteMovie) {
            Log.i(TAG, "Movie is instance of Favorite Movie");
            return ((FavoriteMovie) movie).getBackdropImageFileUri();
        } else {
            return MovieService.getImageFullLink(movie.getBackdropPath(), ImageSize.W500);
        }
    }

    private MovieClient movieClient;

    public MovieService() {
        setupRetrofit();
    }

    private void setupRetrofit() {
        OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request oldRequest = chain.request();
                HttpUrl newUrl = oldRequest.url().newBuilder()
                        .addQueryParameter("api_key", API_KEY)
                        //based on assumption that currently playing movies are released in the current year
                        .addQueryParameter("primary_release_year", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)))
                        .build();
                Request newRequest = oldRequest.newBuilder().url(newUrl).build();
                Log.i(TAG, newRequest.url().toString());
                return chain.proceed(newRequest);
            }
        }).build();

        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(API_BASE_URL)
                        .addConverterFactory(
                                GsonConverterFactory.create()
                        );

        Retrofit retrofit = builder.client(httpClient).build();

        movieClient = retrofit.create(MovieClient.class);
    }

    public Call<Movie> getMovieById(long movieID) {
        return movieClient.getMovieById(movieID);
    }

    public Call<Movie> getMovieByIdWithMoreDetails(long movieID) {
        return movieClient.getMovieByIdWithMoreDetails(movieID);
    }

    public Call<MoviePagedResponse> getMoviesSortBy(SortCriteria sortCriteria, SortDirection sortDirection) {
        return movieClient.getMovies(getSortOrderQuery(sortCriteria, sortDirection), 1);
    }

    public static String getImageFullLink(String posterImagePath, ImageSize imageSize) {
        return IMAGE_API_BASE_URL + imageSize.getSize() + posterImagePath;
    }

    @Override
    public String getSortOrderQuery(SortCriteria sortCriteria, SortDirection sortDirection) {
        return new StringBuilder().append(sortCriteria.getValue()).append(".").append(sortDirection.getValue()).toString();
    }

    public Call<MoviePagedResponse> getMoviesByName(String query) {
        return movieClient.getMoviesByName(query);
    }
}
