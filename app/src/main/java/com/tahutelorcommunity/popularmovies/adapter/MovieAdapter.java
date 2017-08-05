package com.tahutelorcommunity.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;
import com.tahutelorcommunity.popularmovies.R;
import com.tahutelorcommunity.popularmovies.activity.DetailActivity;
import com.tahutelorcommunity.popularmovies.activity.MainActivity;
import com.tahutelorcommunity.popularmovies.utils.NetworkUtils;
import com.tahutelorcommunity.popularmovies.viewholder.MovieViewHolder;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Fikry-PC on 7/2/2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieViewHolder>{
    private JSONArray movieJsonArray;
    private String category;
    private MainActivity activity;

    public MovieAdapter(MainActivity activity){
        this.activity = activity;
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        String poster_path;
        try{
            holder.pb_loading.setVisibility(View.GONE);
            holder.movie_image.setVisibility(View.VISIBLE);
            holder.category = this.category;
            holder.dataSourceType = DetailActivity.INTERNET_DATASOURCE;
            JSONObject dataJson = movieJsonArray.getJSONObject(position);
            holder.id = dataJson.getString("id");
            JSONObject movie = movieJsonArray.getJSONObject(position);
            poster_path = movie.getString("poster_path");
//            notifyDataSetChanged();
            Picasso.with(holder.context).load(String.valueOf(NetworkUtils.buildImageUrl(poster_path.substring(1)))).into(holder.movie_image);

        } catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
        if(movieJsonArray == null){
            return 0;
        }
        else{
            return movieJsonArray.length();
        }
    }

    public void setMovieData(JSONArray movieData, String category){
        this.movieJsonArray = movieData;
        this.category = category;
        notifyDataSetChanged();
    }


    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(R.layout.movie_list_item,parent, shouldAttachToParentImmediately);
        return new MovieViewHolder(view, activity);
    }

}
