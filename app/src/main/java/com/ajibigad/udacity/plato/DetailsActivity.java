package com.ajibigad.udacity.plato;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.network.ImageSize;
import com.ajibigad.udacity.plato.network.MovieService;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.IOException;

import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = DetailsActivity.class.getSimpleName();
    public static String MOVIE_ID = "movieID";
    private long selectedMovieID;

    TextView tvTitle, tvReleaseDate, tvSynopsis, tvUserRating;
    SimpleDraweeView posterImage;

    TextView tvErrorMessage;
    ProgressBar progressBar;

    View movieDetailsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvReleaseDate = (TextView) findViewById(R.id.tv_release_date);
        tvSynopsis = (TextView) findViewById(R.id.tv_synopsis);
        tvUserRating = (TextView) findViewById(R.id.tv_user_ratings);
        posterImage = (SimpleDraweeView) findViewById(R.id.movie_poster_image);

        tvErrorMessage = (TextView) findViewById(R.id.tv_error_message_display);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        movieDetailsLayout = (View) findViewById(R.id.movie_details_layout);

        if(getIntent().hasExtra(MOVIE_ID)){
            selectedMovieID = getIntent().getLongExtra(MOVIE_ID, 0);
            if (selectedMovieID == 0) finish(); // this shouldn't happen
            new FetchMovieDetailsAsync().execute(selectedMovieID);
        }
    }

    private void displayErrorMessage(String message){
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
        movieDetailsLayout.setVisibility(View.INVISIBLE);
    }

    private void displayMovieDetails(Movie movie){
        tvTitle.setText(movie.getTitle());
        tvReleaseDate.setText(movie.getReleaseDate());
        tvSynopsis.setText(movie.getOverview());
        tvUserRating.setText(String.valueOf(movie.getVoteAverage()));
        String fullPosterLink = MovieService.getPosterImageFullLink(movie.getPosterPath(), ImageSize.W342);
        posterImage.setImageURI(fullPosterLink);
        movieDetailsLayout.setVisibility(View.VISIBLE);
    }

    class FetchMovieDetailsAsync extends AsyncTask<Long, Void, Movie>{

        private MovieService movieService;

        public FetchMovieDetailsAsync(){
            movieService = new MovieService();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //show progress bar
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Movie doInBackground(Long... params) {
            try {
                Response<Movie> response = movieService.getMoviesById(params[0]).execute();
                if (response.isSuccessful()){
                    return response.body();
                }
                else{
                    return null;
                }
            } catch (IOException e) {
                Log.e(TAG, "Error occured while trying to fetch movie details");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Movie movie) {
            super.onPostExecute(movie);
            //hide progress bar
            if(movie == null) {
                Toast.makeText(DetailsActivity.this, "Movie not found", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            progressBar.setVisibility(View.INVISIBLE);
            displayMovieDetails(movie);
        }
    }

}
