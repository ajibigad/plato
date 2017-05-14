package com.ajibigad.udacity.plato;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ajibigad.udacity.plato.adapters.AllMoviesAdapter;
import com.ajibigad.udacity.plato.adapters.MovieAdapter;
import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.data.MoviePagedResponse;
import com.ajibigad.udacity.plato.network.MovieService;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class AllMoviesFragment extends Fragment implements MovieAdapter.MovieAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<List<Movie>>, SharedPreferences.OnSharedPreferenceChangeListener{

    private static final int MOVIES_LOADER = 4321;
    private static final String TAG = AllMoviesFragment.class.getSimpleName();

    @BindView(R.id.recyclerview_movies)
    RecyclerView movieRecyclerView;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;
    @BindView(R.id.tv_error_message_display)
    TextView tvErrorMessage;

    MovieAdapter<List<Movie>> movieAdapter;

    private MovieService movieService;

    SharedPreferences sharedPreferences;

    public AllMoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieService = new MovieService();
        movieAdapter = new AllMoviesAdapter(this, getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "All movies fragment attached");
        getActivity().getPreferences(Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "All moviesfragment detached");
        getActivity().getPreferences(Context.MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);

        movieRecyclerView.setAdapter(movieAdapter);
        movieRecyclerView.setLayoutManager(gridLayoutManager);

        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        loadPopularMovies();
    }

    private void loadPopularMovies(){
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader<List<Movie>> moviesLoader = loaderManager.getLoader(MOVIES_LOADER);
        if (moviesLoader == null) {
            loaderManager.initLoader(MOVIES_LOADER, null, this);
        } else {
            loaderManager.restartLoader(MOVIES_LOADER, null, this);
        }
    }

    private void showErrorMessage(){
        tvErrorMessage.setVisibility(View.VISIBLE);
        movieRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void showPopularMoviesView(){
        movieRecyclerView.setVisibility(View.VISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar(){
        movieRecyclerView.setVisibility(View.INVISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(Movie movie) {
        // start details intent
        Intent detailsIntent = new Intent(getContext(), DetailsActivity.class);
        detailsIntent.putExtra(DetailsActivity.MOVIE_PARCEL, Parcels.wrap(movie));
        startActivity(detailsIntent);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<List<Movie>>(getContext()) {

            private List<Movie> cachedMovies;

            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                if(cachedMovies != null && !cachedMovies.isEmpty()){
                    deliverResult(cachedMovies);
                }
                else{
                    showProgressBar();
                    forceLoad();
                }
            }

                @Override
            public List<Movie> loadInBackground() {
                    String prefSortCriteria = sharedPreferences.getString(MainActivity.SORT_CRITERIA_KEY, MovieService.SortCriteria.POPULARITY.name());
                    String prefSortDirection = sharedPreferences.getString(MainActivity.SORT_DIRECTION_KEY, MovieService.SortDirection.DESC.name());
                    MovieService.SortCriteria sortCriteria = MovieService.SortCriteria.valueOf(prefSortCriteria);
                    MovieService.SortDirection sortDirection = MovieService.SortDirection.valueOf(prefSortDirection);
                    try {
                        Response<MoviePagedResponse> response = movieService.getMoviesSortBy(sortCriteria,
                                sortDirection)
                                .execute();
                        if(response.isSuccessful()){
                            return response.body().getResults();
                        } else{
                            Log.i(TAG, String.format("Message : %s,Response code: %s", response.message(), response.code()));
                            return Collections.emptyList();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return Collections.emptyList();
                    }
            }

            @Override
            public void deliverResult(List<Movie> movies) {
                cachedMovies = movies;
                super.deliverResult(movies);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        progressBar.setVisibility(View.INVISIBLE);
        if(movies.isEmpty()){
            showErrorMessage();
        }
        else{
            movieAdapter.setMovies(movies);
            showPopularMoviesView();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        movieAdapter.setMovies(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        loadPopularMovies();
    }
}
