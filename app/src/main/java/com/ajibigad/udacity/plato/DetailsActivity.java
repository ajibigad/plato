package com.ajibigad.udacity.plato;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ajibigad.udacity.plato.adapters.MovieDetailsPagerAdapter;
import com.ajibigad.udacity.plato.data.Cast;
import com.ajibigad.udacity.plato.data.FavoriteMovie;
import com.ajibigad.udacity.plato.data.FavoriteMovieHelper;
import com.ajibigad.udacity.plato.data.FavoriteMovieProvider;
import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.events.AddFavoriteMovieEvent;
import com.ajibigad.udacity.plato.events.DeleteFavoriteMovieEvent;
import com.ajibigad.udacity.plato.events.FavoriteMovieDeletedEvent;
import com.ajibigad.udacity.plato.events.MovieFetchedEvent;
import com.ajibigad.udacity.plato.events.NewFavoriteMovieAdded;
import com.ajibigad.udacity.plato.network.MovieService;
import com.ajibigad.udacity.plato.services.ReminderAlarmService;
import com.ajibigad.udacity.plato.utils.NetworkConnectivityUtils;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.parceler.Parcels;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import javadz.beanutils.BeanUtils;
import retrofit2.Response;
public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Movie>, CastFragment.OnListFragmentInteractionListener {

    private static final String TAG = DetailsActivity.class.getSimpleName();
    public static final String IS_MOVIE_FAVORITE = "is_movie_favorite";
    public static final String MOVIE_PARCEL = "movie_parcel";
    private static final int MOVIE_LOADER = 45345;
    private Movie selectedMovie;

    //holds the ID of a movie removed from favorites(local) and used to fetch the movie from web
    private long cachedDeletedFavoriteMovieID;

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    MovieDetailsPagerAdapter movieDetailsPagerAdapter;

    private boolean isSelectedMovieFavorite;

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

    private Menu activityMenu;


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
        tabLayout.addTab(tabLayout.newTab().setText(R.string.cast_tab_layout));

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

        movieService = new MovieService();

        Cursor cursor = null;
        //get movie from either parcel or uri
        if (getIntent().hasExtra(MOVIE_PARCEL)) {
            selectedMovie = Parcels.unwrap(getIntent().getParcelableExtra(MOVIE_PARCEL));
            if (selectedMovie == null) finish(); // this shouldn't happen
            if( !(selectedMovie instanceof FavoriteMovie) && (cursor = FavoriteMovieHelper.findFavoriteMovieByID(this, selectedMovie.getId())) != null){
                // convert movie to type FavouriteMovie.class
                selectedMovie = FavoriteMovieHelper.CreateFavouriteMovieFromCursor(cursor);
            }
        }
        else if(getIntent().getData() != null){ //handles intent from Notification
            if ((cursor = FavoriteMovieHelper.findFavoriteMovieByUri(this, getIntent().getData())) != null) {
                selectedMovie = FavoriteMovieHelper.CreateFavouriteMovieFromCursor(cursor);
            } else finish();
        } else finish();

        loadMovieDetails();
        displayMovieDetails(selectedMovie);
        if (selectedMovie instanceof FavoriteMovie) toogleFavoriteMovieState();

        if(cursor != null){
            cursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_details, menu);
        activityMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_set_remainder){
            setRemainder();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setRemainder(){
        if(!(selectedMovie instanceof FavoriteMovie)){
            Toast.makeText(this, R.string.reminder_for_only_fav_movies_msg, Toast.LENGTH_LONG).show();
            return;
        }
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date releaseDate = dateFormatter.parse(selectedMovie.getReleaseDate());
            Calendar c =  Calendar.getInstance();
            c.setTime(releaseDate);
            c.set(Calendar.HOUR_OF_DAY, 8);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);

            if(c.before(Calendar.getInstance())){
                Toast.makeText(this, R.string.movie_has_been_released, Toast.LENGTH_LONG).show();
                return;
            }

            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            PendingIntent operation =
                    ReminderAlarmService.getReminderPendingIntent(this, FavoriteMovieProvider.FavoriteMovies.withId(selectedMovie.getId()));

            manager.setExact(AlarmManager.RTC, c.getTimeInMillis(), operation);
            Toast.makeText(this, R.string.reminder_set, Toast.LENGTH_SHORT).show();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        toogleFavoriteMovieState();
        Toast.makeText(this, R.string.favorite_movie_added_msg, Toast.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleFavoriteMovieDeletedEvent(FavoriteMovieDeletedEvent event) {
        toogleFavoriteMovieState();
        //remove the remainder set for this favorite movie if any
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent operation =
                ReminderAlarmService.getReminderPendingIntent(this, FavoriteMovieProvider.FavoriteMovies.withId(selectedMovie.getId()));
        manager.cancel(operation);

        Toast.makeText(this, R.string.favorite_movie_removed_msg, Toast.LENGTH_LONG).show();
        cachedDeletedFavoriteMovieID = selectedMovie.getId();
        selectedMovie = null;
        //load details from web since local copy has been deleted
        loadMovieDetails();
    }

    // this should be the only method to edit isSelectedMovieFavorite
    private void toogleFavoriteMovieState() {
        isSelectedMovieFavorite = !isSelectedMovieFavorite;
        //TODO change state of set reminder menu option so reminders can only be set for favorite movies
        if (isSelectedMovieFavorite) {
//            activityMenu.getItem(0).setEnabled(true);
            favoriteBtn.setImageDrawable(favoriteButtonSelectedStateImage);
        } else {
//            activityMenu.getItem(0).setEnabled(false);
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
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                Toast.makeText(DetailsActivity.this, R.string.adding_favorite_movie_faliure_msg, Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public Loader<Movie> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Movie>(this) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if(NetworkConnectivityUtils.isConnected(getContext())){
                    showProgressBar();
                    forceLoad();
                } else {
                    deliverResult(null);
                }

            }

            @Override
            public Movie loadInBackground() {
                try {
                    long movieID = selectedMovie != null ? selectedMovie.getId() : cachedDeletedFavoriteMovieID;
                    Response<Movie> response = movieService.getMovieByIdWithMoreDetails(movieID).execute();
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
    public void onLoadFinished(Loader loader, Movie movie) {
        progressBar.setVisibility(View.INVISIBLE);
        if (movie == null) {
            if(selectedMovie != null){
                Toast.makeText(this, R.string.loading_more_movie_details_failure_msg, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(DetailsActivity.this, R.string.movie_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //null selected movie means movie was removed from favorites so a fresh movie was fetched
        if(selectedMovie == null){
            selectedMovie = movie;
        } else { //append the extra details to the selected movie
            selectedMovie.setReviews(movie.getReviews());
            selectedMovie.setTrailers(movie.getTrailers());
            selectedMovie.setCasts(movie.getCasts());
        }

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

    @Override
    public void onListFragmentInteraction(Cast cast) {

    }
}
