package com.tahutelorcommunity.popularmovies.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.tahutelorcommunity.popularmovies.R;
import com.tahutelorcommunity.popularmovies.activity.DetailActivity;
import com.tahutelorcommunity.popularmovies.activity.MainActivity;

import org.json.JSONObject;

/**
 * Created by Fikry-PC on 8/2/2017.
 */

public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    final View view;
    public Context context = null;
    public String id;
    public String category;
    public int dataSourceType;
    public final ImageView movie_image;
    public final ProgressBar pb_loading;
    private MainActivity activity;
    public MovieViewHolder(View view, MainActivity activity){
        super(view);
        this.activity = activity;
        this.view = view;
        context = view.getContext();
        movie_image = (ImageView) view.findViewById(R.id.movie_image);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        view.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        int clickedPosition = getAdapterPosition();
        Intent intent = new Intent(context, DetailActivity.class);
        JSONObject dataJson;
        try{
//            dataJson = movieJsonArray.getJSONObject(clickedPosition);
            intent.putExtra(DetailActivity.DETAIL_ACTIVITY_EXTRA,id);
            intent.putExtra(DetailActivity.DETAIL_ACTIVITY_CATEGORY_EXTRA, category);
            intent.putExtra(DetailActivity.DETAIL_ACTIVITY_DATA_SOURCE, dataSourceType);
            intent.putExtra(DetailActivity.DETAIL_ACTIVITY_POSITION, clickedPosition);
        } catch (Exception e){
            e.printStackTrace();
        }
        activity.startActivityForResult(intent, MainActivity.LAST_CLICKED_ITEM);
    }
}