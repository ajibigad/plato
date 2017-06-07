package com.ajibigad.udacity.plato;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ajibigad.udacity.plato.adapters.ReviewAdapter;
import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.data.Review;
import com.ajibigad.udacity.plato.data.ReviewDeserializer;
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
import okhttp3.ResponseBody;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewFragment extends Fragment implements LoaderManager.LoaderCallbacks{

    private static final int REVIEWS_LOADER = 6544433;

    @BindView(R.id.tv_reviews_card)
    View reviewsLayout;
    @BindView(R.id.reviews_recycler_view)
    RecyclerView reviewsRecyclerView;

    @BindView(R.id.tv_error_message_display)
    TextView tvErrorMessage;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;

    ReviewAdapter reviewAdapter;

    private List<Review> reviews;

    private Movie selectedMovie;

    private MovieService movieService;
    private boolean movieLoaded;

    public ReviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_review, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reviewAdapter = new ReviewAdapter();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        reviewsRecyclerView.setAdapter(reviewAdapter);
        reviewsRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        reviewsRecyclerView.setLayoutManager(layoutManager);
        movieService = ((DetailsActivity) getActivity()).getMovieService();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        selectedMovie = ((DetailsActivity) getActivity()).getSelectedMovie();
        if(selectedMovie != null){
            loadMovieReviews();
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
        selectedMovie = event.getMovie();
        loadMovieReviews();
        Toast.makeText(getActivity(), "Movie Reviews fetched", Toast.LENGTH_LONG).show();
    }

    private void loadMovieReviews() {
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        loaderManager.initLoader(REVIEWS_LOADER, null, this);
    }

    private void showErrorMessage() {
        tvErrorMessage.setVisibility(View.VISIBLE);
        reviewsLayout.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showMovieReviewsView() {
        reviewsLayout.setVisibility(View.VISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar() {
        reviewsLayout.setVisibility(View.INVISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<String>(getActivity()) {

            @Override
            protected void onStartLoading() {
                showProgressBar();
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                try {
                    Response<ResponseBody> response = movieService.getMoviesByIdString(selectedMovie.getId()).execute();
                    if (response.isSuccessful()) {
                        return response.body().string();
                    } else {
                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        String rawResponse = (String) data;
        if (rawResponse == null) {
            return;
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<List<Review>>() {
                }.getType(), new ReviewDeserializer())
                .create();

        reviews = gson.fromJson(rawResponse, new TypeToken<List<Review>>() {
        }.getType());
        if (reviews == null || reviews.isEmpty()) {
            //hide reviews card
            showErrorMessage();
        } else {
            //display reviews
            showMovieReviewsView();
            reviewAdapter.setData(reviews);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
