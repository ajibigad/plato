package com.ajibigad.udacity.plato;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ajibigad.udacity.plato.adapters.FavoriteMoviesAdapter;
import com.ajibigad.udacity.plato.adapters.MovieAdapter;
import com.ajibigad.udacity.plato.data.FavoriteMovieColumns;
import com.ajibigad.udacity.plato.data.FavoriteMovieProvider;
import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.events.FetchMovieEvent;
import com.ajibigad.udacity.plato.network.MovieService;
import com.ajibigad.udacity.plato.utils.SortOrderResolver;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteMoviesFragment extends Fragment implements MovieAdapter.MovieAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener, SortOrderResolver {

    private static final String TAG = FavoriteMoviesFragment.class.getSimpleName();
    private static final int FAVORITE_MOVIES_LOADER = 2345;
    @BindView(R.id.recyclerview_movies)
    RecyclerView movieRecyclerView;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;
    @BindView(R.id.tv_error_message_display)
    TextView tvErrorMessage;

    MovieAdapter<Cursor> movieAdapter;

    SharedPreferences sharedPreferences;

    public FavoriteMoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieAdapter = new FavoriteMoviesAdapter(this, getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movies, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);

        movieRecyclerView.setAdapter(movieAdapter);
        movieRecyclerView.setLayoutManager(gridLayoutManager);

        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        loadFavoriteMovies();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "Favorite movies fragment attached");
        getActivity().getPreferences(Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "Favorite moviesfragment detached");
        getActivity().getPreferences(Context.MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void loadFavoriteMovies() {
        showProgressBar();
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader<List<Movie>> favoriteMoviesLoader = loaderManager.getLoader(FAVORITE_MOVIES_LOADER);
        if (favoriteMoviesLoader == null) {
            loaderManager.initLoader(FAVORITE_MOVIES_LOADER, null, this);
        } else {
            loaderManager.restartLoader(FAVORITE_MOVIES_LOADER, null, this);
        }
    }

    private void showErrorMessage() {
        tvErrorMessage.setVisibility(View.VISIBLE);
        movieRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void showFavoriteMoviesView() {
        movieRecyclerView.setVisibility(View.VISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar() {
        movieRecyclerView.setVisibility(View.INVISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String prefSortCriteria = sharedPreferences.getString(MainActivity.SORT_CRITERIA_KEY, MovieService.SortCriteria.POPULARITY.name());
        String prefSortDirection = sharedPreferences.getString(MainActivity.SORT_DIRECTION_KEY, MovieService.SortDirection.DESC.name());
        SortOrderResolver.SortCriteria sortCriteria = MovieService.SortCriteria.valueOf(prefSortCriteria);
        SortOrderResolver.SortDirection sortDirection = MovieService.SortDirection.valueOf(prefSortDirection);

        String sortOrderQuery = getSortOrderQuery(sortCriteria, sortDirection);
        Log.i(TAG, "Sort Order: " + sortOrderQuery);
        return new CursorLoader(getContext(), FavoriteMovieProvider.FavoriteMovies.CONTENT_URI, null, null, null, sortOrderQuery);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor movies) {
        progressBar.setVisibility(View.INVISIBLE);
        if (movies.getCount() == 0) {
            showErrorMessage();
        } else {
            movieAdapter.setMovies(movies);
            showFavoriteMoviesView();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        movieAdapter.setMovies(null);
    }

    @Override
    public void onClick(Movie movie) {
        Intent detailsIntent = new Intent(getContext(), DetailsActivity.class);
        detailsIntent.putExtra(DetailsActivity.MOVIE_PARCEL, Parcels.wrap(movie));
        detailsIntent.putExtra(DetailsActivity.IS_MOVIE_FAVORITE, true);
        startActivity(detailsIntent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        loadFavoriteMovies();
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
        loadFavoriteMovies();
    }

    @Override
    public String getSortOrderQuery(SortCriteria sortCriteria, SortDirection sortDirection) {
        StringBuilder sortOrderQuery = new StringBuilder();
        switch (sortCriteria) {
            case POPULARITY:
                sortOrderQuery.append(FavoriteMovieColumns.POPULARITY).append(" ");
                break;
            case RATINGS:
                sortOrderQuery.append(FavoriteMovieColumns.USER_RATINGS).append(" ");
                break;
        }
        sortOrderQuery.append(sortDirection.name());
        return sortOrderQuery.toString();
    }
}
