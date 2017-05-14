package com.ajibigad.udacity.plato;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.data.Review;
import com.ajibigad.udacity.plato.data.ReviewDeserializer;
import com.ajibigad.udacity.plato.data.Trailer;
import com.ajibigad.udacity.plato.data.TrailerDeserializer;
import com.ajibigad.udacity.plato.events.AddFavoriteMovieEvent;
import com.ajibigad.udacity.plato.events.DeleteFavoriteMovieEvent;
import com.ajibigad.udacity.plato.events.FavoriteMovieDeletedEvent;
import com.ajibigad.udacity.plato.network.ImageSize;
import com.ajibigad.udacity.plato.network.MovieService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.List;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {

    private static final String TAG = DetailsActivity.class.getSimpleName();
    public static final String IS_MOVIE_FAVORITE = "is_movie_favorite";
    public static final String MOVIE_PARCEL = "movie_parcel";
    private static final int MOVIE_LOADER = 45345;
    private static final int TRAILERS_AND_REVIEWS_LOADER = 7633467;
    private Movie selectedMovie;
    private List<Trailer> trailers;
    private List<Review> reviews;

    private boolean isSelectedMovieFavorite;

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_release_date)
    TextView tvReleaseDate;
    @BindView(R.id.tv_synopsis)
    TextView tvSynopsis;
    @BindView(R.id.tv_user_ratings)
    TextView tvUserRating;
    @BindView(R.id.movie_poster_image)
    ImageView posterImage;

    @BindView(R.id.tv_error_message_display)
    TextView tvErrorMessage;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;
    @BindView(R.id.favorite_btn)
    ImageButton favoriteBtn;
    @BindView(R.id.share_trailer_url)
    Button shareTrailerBtn;
    @BindView(R.id.watch_trailer1_btn)
    Button watchTrailer1Btn;
    @BindView(R.id.watch_trailer2_btn)
    Button watchTrailer2Btn;
    @BindView(R.id.view_reviews_btn)
    Button viewReviewsBtn;

    @BindView(R.id.movie_details_layout)
    View movieDetailsLayout;

    @BindDrawable(R.drawable.ic_favorite_black_24dp)
    Drawable favoriteButtonSelectedStateImage;

    @BindDrawable(R.drawable.ic_favorite_border_black_24dp)
    Drawable favoriteButtonUnSelectedStateImage;

    MovieService movieService;


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

        if(getIntent().hasExtra(IS_MOVIE_FAVORITE) && getIntent().getBooleanExtra(IS_MOVIE_FAVORITE, false)){
            toogleFavoriteButtonState();
        }

        movieService = new MovieService();
//        loadMovieTrailersAndReviews();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void loadMovieDetails(){
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<List<Movie>> moviesLoader = loaderManager.getLoader(MOVIE_LOADER);
        if (moviesLoader == null) {
            loaderManager.initLoader(MOVIE_LOADER, null, this);
        } else {
            loaderManager.restartLoader(MOVIE_LOADER, null, this);
        }
    }

    private void loadMovieTrailersAndReviews(){
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<List<Movie>> trailerAndReviewsLoader = loaderManager.getLoader(TRAILERS_AND_REVIEWS_LOADER);
        if (trailerAndReviewsLoader == null) {
            loaderManager.initLoader(TRAILERS_AND_REVIEWS_LOADER, null, this);
        } else {
            loaderManager.restartLoader(TRAILERS_AND_REVIEWS_LOADER, null, this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleFavoriteMovieAddedEvent(AddFavoriteMovieEvent event){
        toogleFavoriteButtonState();
        Toast.makeText(this, "Movie added to Favorites", Toast.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleFavoriteMovieDeleteEvent(FavoriteMovieDeletedEvent event){
        toogleFavoriteButtonState();
        Toast.makeText(this, "Movie removed from Favorites", Toast.LENGTH_LONG).show();
        //load details from web since local copy has been deleted
        loadMovieDetails();
//        loadMovieTrailersAndReviews();
    }

    // this should be the only method to edit isSelectedMovieFavorite
    private void toogleFavoriteButtonState() {
        isSelectedMovieFavorite = !isSelectedMovieFavorite;
        if (isSelectedMovieFavorite){
            favoriteBtn.setImageDrawable(favoriteButtonSelectedStateImage);
        }
        else{
            favoriteBtn.setImageDrawable(favoriteButtonUnSelectedStateImage);
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
        Picasso.with(DetailsActivity.this)
                .load(MovieService.getPosterImagePath(movie))
                .fit()
                .into(posterImage);
        movieDetailsLayout.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.favorite_btn)
    public void handleFavoriteBtnClick(){
        if(isSelectedMovieFavorite){
           //means users want to remove from favorite
            EventBus.getDefault().post(new DeleteFavoriteMovieEvent(selectedMovie));
        } else {
            EventBus.getDefault().post(new AddFavoriteMovieEvent(selectedMovie));
        }
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        switch (id){
            case MOVIE_LOADER:
                return new AsyncTaskLoader<Movie>(this) {

                    @Override
                    protected void onStartLoading() {
                        super.onStartLoading();

                        progressBar.setVisibility(View.VISIBLE);
                        forceLoad();
                    }

                    @Override
                    public Movie loadInBackground() {
                        try {
                            Response<Movie> response = movieService.getMoviesById(selectedMovie.getId()).execute();
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
                };
            case TRAILERS_AND_REVIEWS_LOADER:
                return new AsyncTaskLoader<String>(this) {

                    @Override
                    protected void onStartLoading() {
                        super.onStartLoading();

                        progressBar.setVisibility(View.VISIBLE);
                        forceLoad();
                    }

                    @Override
                    public String loadInBackground() {
                        try {
                            Response<ResponseBody> response = movieService.getMoviesByIdString(selectedMovie.getId()).execute();
                            if (response.isSuccessful()){
                                return response.body().string();
                            }
                            else{
                                return null;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                };
            default:
                throw new UnsupportedOperationException("Loader with id "+ id + "does not exist");
        }

    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        int id = loader.getId();
        switch (id){
            case MOVIE_LOADER:
                Movie movie = (Movie) data;
                if(movie == null) {
                    Toast.makeText(DetailsActivity.this, "Movie not found", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                progressBar.setVisibility(View.INVISIBLE);
                selectedMovie = movie;
                displayMovieDetails(movie);
                break;
            case TRAILERS_AND_REVIEWS_LOADER:
                String rawResponse = (String) data;
                if(rawResponse == null){
                    return;
                }
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Review.class, new ReviewDeserializer())
                        .registerTypeAdapter(Trailer.class, new TrailerDeserializer())
                        .create();

                reviews = gson.fromJson(rawResponse, new TypeToken<List<Review>>() {}.getType());
                if(reviews == null || reviews.isEmpty()){
                    //hide reviews card
                }else{
                    //display reviews
                }
                trailers = gson.fromJson(rawResponse, new TypeToken<List<Trailer>>(){}.getType());
                if(trailers == null || trailers.isEmpty()){
                    //hide trailer buttons

                } else if(trailers.size() > 1){
                    //display both buttons
                } else {
                    //display only first button
                }
                progressBar.setVisibility(View.INVISIBLE);
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @OnClick(R.id.share_trailer_url)
    public void onClickShareTrailerButton(){
        startShareTrailerIntent(trailers.get(0));
    }

    @OnClick(R.id.watch_trailer1_btn)
    public void onClickWatchTrailer1(){
        startViewTrailerIntent(trailers.get(0));
    }

    @OnClick(R.id.watch_trailer2_btn)
    public void onClickWatchTrailer2(){
        startViewTrailerIntent(trailers.get(1));
    }

    private void startShareTrailerIntent(Trailer trailer){
        ShareCompat.IntentBuilder.from(this)
                .setChooserTitle("Share Trailer URL")
                .setText("Watch " + selectedMovie.getTitle() + " trailer here : "+ getYoutubeLink(trailer))
                .startChooser();
    }

    private void startViewTrailerIntent(Trailer trailer){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getYoutubeLink(trailer))));
    }

    private void displayReviews(){

    }

    private String getYoutubeLink(Trailer trailer){
        return getString(R.string.youtube_link) + trailer.getSource();
    }
}
