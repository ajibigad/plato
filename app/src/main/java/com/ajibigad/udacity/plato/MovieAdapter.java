package com.ajibigad.udacity.plato;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.network.ImageSize;
import com.ajibigad.udacity.plato.network.MovieService;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Julius on 13/04/2017.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieHolder> {

    private List<Movie> movieList = new ArrayList<>();

    private final MovieAdapterOnClickHandler movieAdapterOnClickHandler;

    public MovieAdapter(MovieAdapterOnClickHandler movieAdapterOnClickHandler){
        this.movieAdapterOnClickHandler = movieAdapterOnClickHandler;
    }

    @Override
    public MovieHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_layout_item, parent, false);
        return new MovieHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieHolder holder, int position) {
        Movie movie = movieList.get(position);
        String posterImageFullLink = MovieService.getPosterImageFullLink(movie.getPosterPath(), ImageSize.W342);
        holder.moviePosterImageView.setImageURI(posterImageFullLink);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public void setMovieList(List<Movie> movieList){
        this.movieList = movieList;
        notifyDataSetChanged();
    }

    class MovieHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.movie_poster_image)
        SimpleDraweeView moviePosterImageView;

        public MovieHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.movie_poster_image)
        public void onClick(View v) {
            int selectedItemIndex = getAdapterPosition();
            Movie selectedMovie = movieList.get(selectedItemIndex);
            movieAdapterOnClickHandler.onClick(selectedMovie);
        }
    }

    interface MovieAdapterOnClickHandler{

        public void onClick(Movie movie);
    }
}
