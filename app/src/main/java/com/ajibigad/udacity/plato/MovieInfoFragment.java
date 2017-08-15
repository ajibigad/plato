package com.ajibigad.udacity.plato;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.events.MovieFetchedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class MovieInfoFragment extends Fragment {

    @BindView(R.id.tv_synopsis)
    TextView tvSynopsis;

    @BindView(R.id.tv_viewers_ratings)
    TextView tvViewerRatings;

    @BindView(R.id.tv_release_date)
    TextView tvReleaseDate;

    public MovieInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_info, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    private void displayMovieInfo(Movie movie){
        tvSynopsis.setText(movie.getOverview());
        tvReleaseDate.setText(String.format(getString(R.string.release_date_format), movie.getReleaseDate()));
        tvViewerRatings.setText(String.format(getString(R.string.ratings_format), movie.getVoteAverage()));
    }

    @Override
    public void onStart() {
        super.onStart();
        Movie selectedMovie = ((DetailsActivity) getActivity()).getSelectedMovie();
        if(selectedMovie != null){
            displayMovieInfo(selectedMovie);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMovieFetchedEvent(MovieFetchedEvent event) {
        displayMovieInfo(event.getMovie());
    }

}
