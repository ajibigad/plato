package com.ajibigad.udacity.plato.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ajibigad.udacity.plato.R;
import com.ajibigad.udacity.plato.data.Movie;
import com.ajibigad.udacity.plato.network.ImageSize;
import com.ajibigad.udacity.plato.network.MovieService;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

/**
 * Created by ajibigad on 23/07/2017.
 */

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>{

    private List<Movie> mValues = Collections.emptyList();
    private final MovieAdapter.MovieAdapterOnClickHandler mListener;
    private Context mContext;

    public SearchResultsAdapter(MovieAdapter.MovieAdapterOnClickHandler listener, Context context) {
        mListener = listener;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_results_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mMovieName.setText(mValues.get(position).getTitle());
        Picasso.with(mContext)
                .load(MovieService.getImageFullLink(mValues.get(position).getPosterPath(), ImageSize.W154))
                .placeholder(R.drawable.loading)
                .fit()
                .centerCrop()
                .into(holder.mPosterImage);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onClick(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setData(List<Movie> data) {
        this.mValues = data;
    }

    public void setMovies(List<Movie> movies) {
        this.mValues = movies;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mPosterImage;
        public final TextView mMovieName;
        public Movie mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mPosterImage = (ImageView) view.findViewById(R.id.iv_poster_image);
            mMovieName = (TextView) view.findViewById(R.id.tv_movie_name);
        }
    }
}
