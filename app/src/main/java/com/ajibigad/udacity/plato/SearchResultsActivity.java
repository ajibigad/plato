package com.ajibigad.udacity.plato;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ajibigad.udacity.plato.adapters.MovieAdapter;
import com.ajibigad.udacity.plato.adapters.SearchResultsAdapter;
import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.data.MoviePagedResponse;
import com.ajibigad.udacity.plato.network.MovieService;
import com.ajibigad.udacity.plato.utils.NetworkConnectivityUtils;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;

import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;

public class SearchResultsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Movie>>,MovieAdapter.MovieAdapterOnClickHandler {

    private SearchView searchView;
    private String query;

    private static final int QUERY_MOVIES_LOADER = 43342321;

    @BindView(R.id.list)
    RecyclerView movieRecyclerView;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;
    @BindView(R.id.tv_error_message_display)
    TextView tvErrorMessage;

    SearchResultsAdapter movieAdapter;

    private MovieService movieService;
    private final String TAG = SearchResultsActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle(R.string.search_results);

        movieService = new MovieService();
        movieAdapter = new SearchResultsAdapter(this, this);
        movieRecyclerView.setAdapter(movieAdapter);
        movieRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            loadResults();
        }
    }

    private void loadResults() {
        LoaderManager loaderManager = getSupportLoaderManager();
        if(loaderManager.getLoader(QUERY_MOVIES_LOADER) == null){
            loaderManager.initLoader(QUERY_MOVIES_LOADER, null, this);
        }else{
            loaderManager.restartLoader(QUERY_MOVIES_LOADER, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search_results, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) item.getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setQuery(query, false);
        searchView.setImeOptions(IME_ACTION_SEARCH);

        return true;
    }



    private void showErrorMessage() {
        tvErrorMessage.setVisibility(View.VISIBLE);
        movieRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void showResultsView() {
        movieRecyclerView.setVisibility(View.VISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar() {
        movieRecyclerView.setVisibility(View.INVISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<List<Movie>>(this) {

            @Override
            protected void onStartLoading() {
                if(NetworkConnectivityUtils.isConnected(getContext())){
                    showProgressBar();
                    forceLoad();
                } else{
                    Toast.makeText(SearchResultsActivity.this, R.string.check_network_connection, Toast.LENGTH_SHORT).show();
                    deliverResult(Collections.<Movie>emptyList());
                }
            }

            @Override
            public List<Movie> loadInBackground() {
                try {
                    Response<MoviePagedResponse> response = movieService.getMoviesByName(query)
                            .execute();
                    if (response.isSuccessful()) {
                        return response.body().getResults();
                    } else {
                        Log.i(TAG, String.format("Message : %s,Response code: %s", response.message(), response.code()));
                        return Collections.emptyList();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return Collections.emptyList();
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        progressBar.setVisibility(View.INVISIBLE);
        if (movies.isEmpty()) {
            showErrorMessage();
        } else {
            movieAdapter.setMovies(movies);
            showResultsView();
            getSupportActionBar().setTitle(String.format("%d movies found", movies.size()));
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {

    }

    @Override
    public void onClick(Movie movie) {
        Intent detailsIntent = new Intent(this, DetailsActivity.class);
        detailsIntent.putExtra(DetailsActivity.MOVIE_PARCEL, Parcels.wrap(movie));
        startActivity(detailsIntent);
    }
}
