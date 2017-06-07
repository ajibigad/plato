package com.ajibigad.udacity.plato;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ajibigad.udacity.plato.adapters.MovieDetailsPagerAdapter;
import com.ajibigad.udacity.plato.data.FavoriteMovie;
import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.events.AddFavoriteMovieEvent;
import com.ajibigad.udacity.plato.events.DeleteFavoriteMovieEvent;
import com.ajibigad.udacity.plato.events.FavoriteMovieDeletedEvent;
import com.ajibigad.udacity.plato.events.MovieFetchedEvent;
import com.ajibigad.udacity.plato.events.NewFavoriteMovieAdded;
import com.ajibigad.udacity.plato.network.MovieService;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.parceler.Parcels;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import javadz.beanutils.BeanUtils;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {

    private static final String TAG = DetailsActivity.class.getSimpleName();
    public static final String IS_MOVIE_FAVORITE = "is_movie_favorite";
    public static final String MOVIE_PARCEL = "movie_parcel";
    private static final int MOVIE_LOADER = 45345;
    private Movie selectedMovie;

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    MovieDetailsPagerAdapter movieDetailsPagerAdapter;

    private boolean isSelectedMovieFavorite;

    @BindView(R.id.tv_release_date)
    TextView tvReleaseDate;
//    @BindView(R.id.tv_synopsis)
//    TextView tvSynopsis;
    @BindView(R.id.tv_user_ratings)
    TextView tvUserRating;
    @BindView(R.id.movie_poster_image)
    ImageView posterImage;

    @BindView(R.id.tv_error_message_display)
    TextView tvErrorMessage;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;

    @BindDrawable(R.drawable.ic_favorite_black_24dp)
    Drawable favoriteButtonSelectedStateImage;

    @BindDrawable(R.drawable.ic_favorite_border_black_24dp)
    Drawable favoriteButtonUnSelectedStateImage;

    private MovieService movieService;

    @BindView(R.id.fab)
    FloatingActionButton favoriteBtn;

    @BindView(R.id.main_content)
    CoordinatorLayout rootLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.collapsingToolbarLayout)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.backdrop_image_view)
    ImageView backdropImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.info_tab_label));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.trailer_tab_label));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.review_tab_layout));

        movieDetailsPagerAdapter = new MovieDetailsPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), this);
        viewPager.setAdapter(movieDetailsPagerAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (getIntent().hasExtra(MOVIE_PARCEL)) {
            selectedMovie = Parcels.unwrap(getIntent().getParcelableExtra(MOVIE_PARCEL));
            if (selectedMovie == null) finish(); // this shouldn't happen
            displayMovieDetails(selectedMovie);
        }

        if (getIntent().hasExtra(IS_MOVIE_FAVORITE) && getIntent().getBooleanExtra(IS_MOVIE_FAVORITE, false)) {
            toogleFavoriteButtonState();
        }

        movieService = new MovieService();
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

    private void loadMovieDetails() {
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<List<Movie>> moviesLoader = loaderManager.getLoader(MOVIE_LOADER);
        if (moviesLoader == null) {
            loaderManager.initLoader(MOVIE_LOADER, null, this);
        } else {
            loaderManager.restartLoader(MOVIE_LOADER, null, this);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleFavoriteMovieAddedEvent(NewFavoriteMovieAdded event) {
        selectedMovie = event.getFavoriteMovie();
        toogleFavoriteButtonState();
        Toast.makeText(this, "Movie added to Favorites", Toast.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleFavoriteMovieDeleteEvent(FavoriteMovieDeletedEvent event) {
        toogleFavoriteButtonState();
        Toast.makeText(this, "Movie removed from Favorites", Toast.LENGTH_LONG).show();
        //load details from web since local copy has been deleted
        loadMovieDetails();
    }

    // this should be the only method to edit isSelectedMovieFavorite
    private void toogleFavoriteButtonState() {
        isSelectedMovieFavorite = !isSelectedMovieFavorite;
        if (isSelectedMovieFavorite) {
            favoriteBtn.setImageDrawable(favoriteButtonSelectedStateImage);
        } else {
            favoriteBtn.setImageDrawable(favoriteButtonUnSelectedStateImage);
        }
    }

    private void displayErrorMessage(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
        rootLayout.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar() {
        tvErrorMessage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        rootLayout.setVisibility(View.INVISIBLE);
    }

    private void displayMovieDetails(Movie movie) {
        collapsingToolbarLayout.setTitle(movie.getTitle());
        tvReleaseDate.setText(movie.getReleaseDate());
//        tvSynopsis.setText(movie.getOverview());
        tvUserRating.setText(String.valueOf(movie.getVoteAverage()));
        Picasso.with(DetailsActivity.this)
                .load(MovieService.getPosterImagePath(movie))
                .fit()
                .into(posterImage);
        Picasso.with(DetailsActivity.this)
                .load(MovieService.getBackdropImagePath(movie))
                .fit()
                .into(backdropImage);
        rootLayout.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.fab)
    public void handleFavoriteBtnClick() {
        if (isSelectedMovieFavorite) {
            //means users want to remove from favorite
            EventBus.getDefault().post(new DeleteFavoriteMovieEvent((FavoriteMovie) selectedMovie));
        } else {
            FavoriteMovie favoriteMovie = new FavoriteMovie();
            try {
                BeanUtils.copyProperties(favoriteMovie, selectedMovie);
                EventBus.getDefault().post(new AddFavoriteMovieEvent(favoriteMovie));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            finally {
                Toast.makeText(DetailsActivity.this, "Failed to add to favorite", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Movie>(this) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                showProgressBar();
                forceLoad();
            }

            @Override
            public Movie loadInBackground() {
                try {
                    Response<Movie> response = movieService.getMoviesById(selectedMovie.getId()).execute();
                    if (response.isSuccessful()) {
                        return response.body();
                    } else {
                        return null;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error occured while trying to fetch movie details");
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        progressBar.setVisibility(View.INVISIBLE);
        Movie movie = (Movie) data;
        if (movie == null) {
            Toast.makeText(DetailsActivity.this, "Movie not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        selectedMovie = movie;
        displayMovieDetails(movie);
        EventBus.getDefault().post(new MovieFetchedEvent(movie));
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    public MovieService getMovieService() {
        return movieService;
    }

    public Movie getSelectedMovie() {
        return selectedMovie;
    }
}
