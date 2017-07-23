package com.ajibigad.udacity.plato;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ajibigad.udacity.plato.adapters.CastAdapter;
import com.ajibigad.udacity.plato.data.Cast;
import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.events.MovieFetchedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class CastFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;

    private CastAdapter castAdapter;

    private List<Cast> casts;

    @BindView(R.id.tv_error_message_display)
    TextView tvErrorMessage;

    @BindView(R.id.list)
    RecyclerView recyclerView;

    private int mColumnCount = 2;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cast_list, container, false);
        ButterKnife.bind(this, view);

        // Set the adapter
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), mColumnCount));
        }
        castAdapter = new CastAdapter(Collections.<Cast>emptyList(), mListener, getContext());
        recyclerView.setAdapter(castAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Movie selectedMovie = ((DetailsActivity) getActivity()).getSelectedMovie();
        if(selectedMovie != null){
            displayCasts(selectedMovie.getCasts());
        }
        EventBus.getDefault().register(this);
    }

    private void displayCasts(List<Cast> casts) {
        if (casts == null || casts.isEmpty()) {
            showErrorMessage();
        } else {
            //display casts
            showMovieCastsView();
            castAdapter.setData(casts);
            Toast.makeText(getActivity(), "Movie Casts fetched", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMovieFetchedEvent(MovieFetchedEvent event) {
        displayCasts(event.getMovie().getCasts());
    }

    private void showMovieCastsView() {
        tvErrorMessage.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        tvErrorMessage.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Cast cast);
    }
}
