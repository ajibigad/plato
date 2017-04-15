package com.ajibigad.udacity.plato.network;

import android.content.Context;
import android.util.Log;

import com.ajibigad.udacity.plato.BuildConfig;
import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.data.MoviePagedResponse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Julius on 13/04/2017.
 */
public class MovieService {

    public final static String API_BASE_URL = "https://api.themoviedb.org/3/";
    public final static String IMAGE_API_BASE_URL = "https://image.tmdb.org/t/p/";
    private static String API_KEY = BuildConfig.API_KEY;

    private static final String TAG = MovieService.class.getSimpleName();

    public static enum SortCriteria {
        POPULARITY("popularity"),
        RATINGS("vote_average");

        private String value;
        SortCriteria(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    };
    public static enum SortDirection {
        ASC("asc"),
        DESC("desc");
        private String value;
        SortDirection(String value) {
            this.value = value;
        }
    };

    private MovieClient movieClient;

    public MovieService(){
        setupRetrofit();
    }

    private void setupRetrofit(){
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

        movieClient =  retrofit.create(MovieClient.class);
    }

    public Call<Movie> getMoviesById(long movieID){
        return movieClient.getMoviesById(movieID);
    }

    public Call<MoviePagedResponse> getMoviesSortBy(SortCriteria sortCriteria, SortDirection sortDirection){
        return movieClient.getMovies(getSortQuery(sortCriteria, sortDirection));
    }

    private String getSortQuery(SortCriteria sortCriteria, SortDirection sortDirection){
        return new StringBuilder().append(sortCriteria.value).append(".").append(sortDirection.value).toString();
    }

    public static String getPosterImageFullLink(String posterImagePath, ImageSize imageSize){
        return IMAGE_API_BASE_URL + imageSize.getSize() + posterImagePath;
    }
}
