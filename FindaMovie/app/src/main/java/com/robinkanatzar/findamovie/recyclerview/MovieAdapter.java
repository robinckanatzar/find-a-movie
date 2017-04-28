package com.robinkanatzar.findamovie.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.robinkanatzar.findamovie.R;

import java.util.List;

/**
 * Created by robinkanatzar on 4/28/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movieList;
    private Context context;

    public MovieAdapter(List<Movie> movieList) {
        this.movieList = movieList;
    }

    @Override
    public MovieAdapter.MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_item, parent, false);

        return new MovieViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MovieAdapter.MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.mMovieTitle.setText(movie.getTitle());
        holder.mMovieDesc.setText(movie.getDescription());

        Glide
                .with(context)
                .load(movie.getImageUrl())
                .into(holder.mMovieImage);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {

        public TextView mMovieTitle;
        public TextView mMovieDesc;
        public ImageView mMovieImage;

        public MovieViewHolder(View itemView) {
            super(itemView);

            mMovieTitle = (TextView) itemView.findViewById(R.id.tv_movie_title);
            mMovieDesc = (TextView) itemView.findViewById(R.id.tv_movie_desc);
            mMovieImage = (ImageView) itemView.findViewById(R.id.iv_movie_image);

            context = itemView.getContext();
        }
    }
}
