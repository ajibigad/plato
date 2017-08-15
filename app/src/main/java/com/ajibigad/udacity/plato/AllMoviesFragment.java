package com.ajibigad.udacity.plato;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ajibigad.udacity.plato.adapters.AllMoviesAdapter;
import com.ajibigad.udacity.plato.adapters.MovieAdapter;
import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.data.MoviePagedResponse;
import com.ajibigad.udacity.plato.events.FetchMovieEvent;
import com.ajibigad.udacity.plato.network.MovieService;
import com.ajibigad.udacity.plato.utils.NetworkConnectivityUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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
        LoaderManager.LoaderCallbacks<List<Movie>>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int MOVIES_LOADER = 4321;
    private static final String TAG = AllMoviesFragment.class.getSimpleName();

    @BindView(R.id.recyclerview_movies)
    RecyclerView movieRecyclerView;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;
    @BindView(R.id.tv_error_message_display)
    TextView tvErrorMessage;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

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
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadPopularMovies();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "All movies fragment attached");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "All moviesfragment detached");
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);

        movieRecyclerView.setAdapter(movieAdapter);
        movieRecyclerView.setLayoutManager(gridLayoutManager);

        loadPopularMovies();
    }

    //use cached data
    private void loadPopularMovies() {
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        loaderManager.initLoader(MOVIES_LOADER, null, this);
    }

    //refetch movies from internet
    private void reloadPopularMovies(){
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        loaderManager.restartLoader(MOVIES_LOADER, null, this);
    }

    private void showErrorMessage() {
        tvErrorMessage.setVisibility(View.VISIBLE);
        movieRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void showPopularMoviesView() {
        movieRecyclerView.setVisibility(View.VISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar() {
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

    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMovieFetchedEvent(FetchMovieEvent event) {
        reloadPopularMovies();
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<List<Movie>>(getContext()) {

            private List<Movie> cachedMovies;

            @Override
            protected void onStartLoading() {
                if(NetworkConnectivityUtils.isConnected(getContext())){
                    if (cachedMovies != null && !cachedMovies.isEmpty()) {
                        deliverResult(cachedMovies);
                    } else {
                        showProgressBar();
                        forceLoad();
                    }
                } else{
                    Toast.makeText(AllMoviesFragment.this.getContext(), R.string.no_network_connection, Toast.LENGTH_SHORT).show();
                    deliverResult(Collections.<Movie>emptyList());
                }
            }

            @Override
            public List<Movie> loadInBackground() {
                String prefSortCriteria = sharedPreferences.getString(getString(R.string.pref_sort_criteria_key), MovieService.SortCriteria.POPULARITY.name());
                String prefSortDirection = sharedPreferences.getString(getString(R.string.pref_sort_direction_key), MovieService.SortDirection.DESC.name());
                MovieService.SortCriteria sortCriteria = MovieService.SortCriteria.valueOf(prefSortCriteria);
                MovieService.SortDirection sortDirection = MovieService.SortDirection.valueOf(prefSortDirection);

                //pages logic
                int prefNumOfMoviesToLoad = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_movie_list_size_key), "20"));
                int prefNumOfPages = prefNumOfMoviesToLoad / 20;
                List<Movie> movies;

                try {
                    int totalPagesReceived;
                    Response<MoviePagedResponse> response = movieService.getMoviesSortBy(sortCriteria,
                            sortDirection, 1).execute();
                    totalPagesReceived = response.body().getTotalPages();
                    if (response.isSuccessful()) {
                        movies = response.body().getResults();
                    } else {
                        Log.i(TAG, String.format("Message : %s,Response code: %s", response.message(), response.code()));
                        return Collections.emptyList();
                    }

                    //from page 2 onward
                    for(int pageNum = 2; pageNum <= prefNumOfPages && pageNum <= totalPagesReceived; pageNum++){
                        //fetch page number
                        response = movieService.getMoviesSortBy(sortCriteria,
                                sortDirection, pageNum).execute();
                        if (response.isSuccessful()) {
                            movies.addAll(response.body().getResults());
                        } else {
                            Log.i(TAG, String.format("Message : %s,Response code: %s", response.message(), response.code()));
                            return Collections.emptyList();
                        }
                    }

                    return movies;

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
        swipeRefreshLayout.setRefreshing(false);
        if (movies.isEmpty()) {
            showErrorMessage();
            Toast.makeText(getActivity(), R.string.could_not_fetch_movies, Toast.LENGTH_SHORT).show();
        } else {
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
        reloadPopularMovies();
    }
}
