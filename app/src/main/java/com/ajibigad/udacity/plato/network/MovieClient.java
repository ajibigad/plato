package com.ajibigad.udacity.plato.network;

import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.data.MoviePagedResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Julius on 13/04/2017.
 */
public interface MovieClient {

    @GET("movie/{ID}?append_to_response=reviews,trailers")
    public Call<Movie> getMoviesById(@Path("ID") long movieID);

    @GET("movie/{ID}?append_to_response=reviews,trailers")
    public Call<ResponseBody> getMoviesByIdString(@Path("ID") long movieID);

    @GET("discover/movie")
    public Call<MoviePagedResponse> getMovies();

    @GET("discover/movie")
    public Call<MoviePagedResponse> getMovies(@Query("sort_by") String sortBy);
}
