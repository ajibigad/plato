package com.ajibigad.udacity.plato;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.data.Trailer;
import com.ajibigad.udacity.plato.data.TrailerDeserializer;
import com.ajibigad.udacity.plato.events.MovieFetchedEvent;
import com.ajibigad.udacity.plato.network.MovieService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class TrailerFragment extends Fragment{//} implements LoaderManager.LoaderCallbacks{

    @BindView(R.id.tv_trailer2_card)
    View trailer2Layout;

    @BindView(R.id.tv_watch_trailer1)
    TextView tvTrailer1;

    @BindView(R.id.tv_watch_trailer2)
    TextView tvTrailer2;

    @BindView(R.id.tv_error_message_display)
    TextView tvErrorMessage;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;

    @BindView(R.id.trailers_layout)
    View trailersLayout;

    private List<Trailer> trailers;

    public TrailerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trailer, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Movie selectedMovie = ((DetailsActivity) getActivity()).getSelectedMovie();
        if(selectedMovie != null){
            displayTrailers(selectedMovie.getTrailers());
        }
        EventBus.getDefault().register(this);
    }

    private void displayTrailers(List<Trailer> trailers) {
        this.trailers = trailers;
        if (trailers == null || trailers.isEmpty()) {
            //hide trailer buttons
            trailersLayout.setVisibility(View.GONE);
            showErrorMessage();
        } else {
            showMovieTrailersView();
            tvTrailer1.setText(trailers.get(0).getName());
            if (trailers.size() > 1) {
                //display both buttons
                trailer2Layout.setVisibility(View.VISIBLE);
                tvTrailer2.setText(trailers.get(1).getName());
            }
            Toast.makeText(getActivity(), "Movie Trailers fetched", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMovieFetchedEvent(MovieFetchedEvent event) {
        displayTrailers(event.getMovie().getTrailers());

    }

    @OnClick(R.id.share_trailer_url)
    public void onClickShareTrailerButton() {
        startShareTrailerIntent(trailers.get(0));
    }

    @OnClick(R.id.tv_watch_trailer1)
    public void onClickWatchTrailer1() {
        startViewTrailerIntent(trailers.get(0));
    }

    @OnClick(R.id.tv_watch_trailer2)
    public void onClickWatchTrailer2() {
        startViewTrailerIntent(trailers.get(1));
    }

    private void startShareTrailerIntent(Trailer trailer) {
        ShareCompat.IntentBuilder.from(getActivity())
                .setChooserTitle(R.string.share_trailer_title)
                .setType("text/plain")
                .setText("Watch " + ((DetailsActivity) getActivity()).getSelectedMovie().getTitle() + " trailer here : " + getYoutubeLink(trailer))
                .startChooser();
    }

    private void startViewTrailerIntent(Trailer trailer) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getYoutubeLink(trailer))));
    }

    private String getYoutubeLink(Trailer trailer) {
        return getString(R.string.youtube_link) + trailer.getSource();
    }

    private void showErrorMessage() {
        tvErrorMessage.setVisibility(View.VISIBLE);
        trailersLayout.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showMovieTrailersView() {
        trailersLayout.setVisibility(View.VISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar() {
        trailersLayout.setVisibility(View.INVISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }
}
