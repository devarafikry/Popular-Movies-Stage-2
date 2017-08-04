package com.tahutelorcommunity.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;
import com.tahutelorcommunity.popularmovies.R;
import com.tahutelorcommunity.popularmovies.activity.DetailActivity;
import com.tahutelorcommunity.popularmovies.data.MovieContract;
import com.tahutelorcommunity.popularmovies.utils.NetworkUtils;
import com.tahutelorcommunity.popularmovies.viewholder.MovieViewHolder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.util.ArrayList;

/**
 * Created by Fikry-PC on 7/2/2017.
 */

public class FavouriteAdapter extends RecyclerView.Adapter<MovieViewHolder>{
    private Cursor mCursor;
    private String category;
    private ArrayList<String> data = new ArrayList<>();

    public FavouriteAdapter(){

    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        byte[] poster_path;
        try{
            holder.pb_loading.setVisibility(View.GONE);
            holder.movie_image.setVisibility(View.VISIBLE);
            holder.category = this.category;
            holder.dataSourceType = DetailActivity.CURSOR_DATASOURCE;
            mCursor.moveToPosition(position);

            holder.id = mCursor.getString(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
            String title = mCursor.getString(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE));

            poster_path = mCursor.getBlob(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_BYTE));
            byte[] imageByteArray = poster_path;

            ByteArrayInputStream imageStream = new ByteArrayInputStream(imageByteArray);
            Bitmap poster = BitmapFactory.decodeStream(imageStream);

            holder.movie_image.setImageBitmap(poster);
//            notifyDataSetChanged();
//            Picasso.with(holder.context).load(String.valueOf(NetworkUtils.buildImageUrl(poster_path.substring(1)))).into(holder.movie_image);

        } catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
        if(mCursor == null){
            return 0;
        }
        else{
            return mCursor.getCount();
        }
    }

    public void swapCursor(Cursor cursor, String category){
        this.mCursor = cursor;
        this.category = category;
        notifyDataSetChanged();
    }

//
    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(R.layout.movie_list_item,parent, shouldAttachToParentImmediately);
        return new MovieViewHolder(view);
    }
//
//    class FavouriteAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
//        final View view;
//        Context context = null;
//        public final ImageView movie_image;
//        public final ProgressBar pb_loading;
//        FavouriteAdapterViewHolder(View view){
//            super(view);
//            this.view = view;
//            context = view.getContext();
//            view.setOnClickListener(this);
//            movie_image = (ImageView) view.findViewById(R.id.movie_image);
//            pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
//        }
//
//        @Override
//        public void onClick(View view) {
//            int clickedPosition = getAdapterPosition();
//            Intent intent = new Intent(context, DetailActivity.class);
//            JSONObject dataJson;
//            try{
//                mCursor.move(clickedPosition);
//                intent.putExtra(DetailActivity.DETAIL_ACTIVITY_EXTRA,mCursor.getString(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID)));
//            } catch (Exception e){
//                e.printStackTrace();
//            }
//            context.startActivity(intent);
//        }
//    }
}
