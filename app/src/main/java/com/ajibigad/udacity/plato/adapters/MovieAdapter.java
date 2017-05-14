package com.ajibigad.udacity.plato.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ajibigad.udacity.plato.R;
import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.network.MovieService;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Julius on 13/04/2017.
 */
public abstract class MovieAdapter<T> extends RecyclerView.Adapter<MovieAdapter.MovieHolder> {

    protected T movies;

    private Context context;

    private final MovieAdapterOnClickHandler movieAdapterOnClickHandler;

    public MovieAdapter(MovieAdapterOnClickHandler movieAdapterOnClickHandler, Context context){
        this.movieAdapterOnClickHandler = movieAdapterOnClickHandler;
        this.context = context;
    }

    @Override
    public MovieHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_layout_item, parent, false);
        return new MovieHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapter.MovieHolder holder, int position) {
        Movie movie = getMovieAtPosition(position);
        Picasso.with(context)
                .load(MovieService.getPosterImagePath(movie))
                .fit()
                .centerCrop()
                .into(holder.moviePosterImageView);
    }

    public abstract Movie getMovieAtPosition(int position);

    @Override
    public int getItemCount() {
        return getMoviesCount();
    }

    public abstract int getMoviesCount();

    public void setMovies(T movies){
        this.movies = movies;
        notifyDataSetChanged();
    }

    public class MovieHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.movie_poster_image)
        ImageView moviePosterImageView;

        public MovieHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.movie_poster_image)
        public void onClick(View v) {
            int selectedItemIndex = getAdapterPosition();
            Movie selectedMovie = getMovieAtPosition(selectedItemIndex);
            movieAdapterOnClickHandler.onClick(selectedMovie);
        }
    }

    public interface MovieAdapterOnClickHandler{

        public void onClick(Movie movie);
    }
}
