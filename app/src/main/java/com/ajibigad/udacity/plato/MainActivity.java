package com.ajibigad.udacity.plato;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.data.MoviePagedResponse;
import com.ajibigad.udacity.plato.network.MovieService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler{

    private static final String TAG = MainActivity.class.getSimpleName();
    RecyclerView movieRecyclerView;
    ProgressBar progressBar;
    TextView tvErrorMessage;

    MovieAdapter movieAdapter;

    SharedPreferences sharedPreferences;

    public static final String SORT_CRITERIA_KEY = "sort_criteria";
    public static final String SORT_DIRECTION_KEY = "sort_direction";
    private AlertDialog sortOrderDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        movieRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        tvErrorMessage = (TextView) findViewById(R.id.tv_error_message_display);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        movieAdapter = new MovieAdapter(this);

        movieRecyclerView.setAdapter(movieAdapter);
        movieRecyclerView.setLayoutManager(gridLayoutManager);

        sharedPreferences = getPreferences(MODE_PRIVATE);
        createSortOrderDialog();

        loadPopularMovies();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_sort_by){
            //display dialog to change movie sort order
            sortOrderDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadPopularMovies(){
        new FetchMoviesAsync().execute();
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
    public void onClick(long movieID) {
        // start details intent
        Intent detailsIntent = new Intent(this, DetailsActivity.class);
        detailsIntent.putExtra(DetailsActivity.MOVIE_ID, movieID);
        startActivity(detailsIntent);
    }

    class FetchMoviesAsync extends AsyncTask<Void, Void, List<Movie>>{

        private MovieService movieService;

        public FetchMoviesAsync(){
            movieService = new MovieService();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar();
        }

        @Override
        protected List<Movie> doInBackground(Void... params) {
            String prefSortCriteria = sharedPreferences.getString(SORT_CRITERIA_KEY, MovieService.SortCriteria.POPULARITY.name());
            String prefSortDirection = sharedPreferences.getString(SORT_DIRECTION_KEY, MovieService.SortDirection.DESC.name());
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
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            progressBar.setVisibility(View.INVISIBLE);
            if(movies.isEmpty()){
                showErrorMessage();
            }
            else{
                movieAdapter.setMovieList(movies);
                showPopularMoviesView();
            }
        }
    }

    private void createSortOrderDialog(){
        String prefSortCriteria = sharedPreferences.getString(SORT_CRITERIA_KEY, MovieService.SortCriteria.POPULARITY.name());
        final String [] sortCriteriaNames = new String[MovieService.SortCriteria.values().length];
        int indexOfPrefCriteria = 0;
        int index = 0;
        final String[] selectedOptions = new String[1];
        for(MovieService.SortCriteria sortCriteria : MovieService.SortCriteria.values()){
            sortCriteriaNames[index] = sortCriteria.name();
            if(sortCriteria.name().equals(prefSortCriteria)){
                indexOfPrefCriteria = index;
            }
            index++;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sort_by)
                .setSingleChoiceItems(sortCriteriaNames, indexOfPrefCriteria, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedOptions[0] = sortCriteriaNames[which];
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sharedPreferences.edit().putString(SORT_CRITERIA_KEY, selectedOptions[0]).apply();
                        loadPopularMovies();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                });
        // Create the AlertDialog object and return it
        sortOrderDialog = builder.create();
    }
}
