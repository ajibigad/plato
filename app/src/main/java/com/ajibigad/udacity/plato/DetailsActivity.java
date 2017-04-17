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

import org.parceler.Parcels;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = DetailsActivity.class.getSimpleName();
    public static String MOVIE_PARCEL = "movie_parcel";
    private Movie selectedMovie;

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_release_date)
    TextView tvReleaseDate;
    @BindView(R.id.tv_synopsis)
    TextView tvSynopsis;
    @BindView(R.id.tv_user_ratings)
    TextView tvUserRating;
    @BindView(R.id.movie_poster_image)
    SimpleDraweeView posterImage;

    @BindView(R.id.tv_error_message_display)
    TextView tvErrorMessage;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;

    @BindView(R.id.movie_details_layout)
    View movieDetailsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ButterKnife.bind(this);

        if(getIntent().hasExtra(MOVIE_PARCEL)){
            selectedMovie = Parcels.unwrap(getIntent().getParcelableExtra(MOVIE_PARCEL));
            if (selectedMovie == null) finish(); // this shouldn't happen
            displayMovieDetails(selectedMovie);
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

}
