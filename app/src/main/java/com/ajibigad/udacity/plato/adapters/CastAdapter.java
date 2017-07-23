package com.ajibigad.udacity.plato.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ajibigad.udacity.plato.CastFragment.OnListFragmentInteractionListener;
import com.ajibigad.udacity.plato.R;
import com.ajibigad.udacity.plato.data.Cast;
import com.ajibigad.udacity.plato.network.ImageSize;
import com.ajibigad.udacity.plato.network.MovieService;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Cast} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CastAdapter extends RecyclerView.Adapter<CastAdapter.ViewHolder> {

    private List<Cast> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Context mContext;

    public CastAdapter(List<Cast> casts, OnListFragmentInteractionListener listener, Context context) {
        mValues = casts;
        mListener = listener;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_cast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mCastName.setText(mValues.get(position).getName());
        Picasso.with(mContext)
                .load(MovieService.getImageFullLink(mValues.get(position).getProfileImage(), ImageSize.W154))
                .placeholder(R.drawable.loading)
                .fit()
                .centerCrop()
                .into(holder.mProfileImage);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setData(List<Cast> data) {
        this.mValues = data;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mProfileImage;
        public final TextView mCastName;
        public Cast mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mProfileImage = (ImageView) view.findViewById(R.id.iv_profile_image);
            mCastName = (TextView) view.findViewById(R.id.tv_cast_name);
        }
    }
}
