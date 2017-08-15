package com.ajibigad.udacity.plato.network;

import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.data.MoviePagedResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Julius on 13/04/2017.
 */
public interface MovieClient {

    @GET("movie/{ID}")
    Call<Movie> getMovieById(@Path("ID") long movieID);

    @GET("movie/{ID}?append_to_response=reviews,trailers,casts")
    Call<Movie> getMovieByIdWithMoreDetails(@Path("ID") long movieID);

    @GET("discover/movie")
    Call<MoviePagedResponse> getMovies();

    @GET("discover/movie")
    Call<MoviePagedResponse> getMovies(@Query("sort_by") String sortBy, @Query("page") int page);

    @GET("search/movie")
    Call<MoviePagedResponse> getMoviesByName(@Query("query") String query);
}
