package com.tahutelorcommunity.popularmovies.viewholder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.tahutelorcommunity.popularmovies.R;
import com.tahutelorcommunity.popularmovies.activity.DetailActivity;

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
    public MovieViewHolder(View view){
        super(view);
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
        } catch (Exception e){
            e.printStackTrace();
        }
        context.startActivity(intent);
    }
}