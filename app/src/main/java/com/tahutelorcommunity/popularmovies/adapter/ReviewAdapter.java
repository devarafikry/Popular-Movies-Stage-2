package com.tahutelorcommunity.popularmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tahutelorcommunity.popularmovies.R;
import com.tahutelorcommunity.popularmovies.viewholder.ReviewViewHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Fikry-PC on 8/3/2017.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewViewHolder> {
    private JSONArray reviewData;
    private Context mContext;
    public ReviewAdapter(Context context){
        this.mContext = context;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachParentImmediately = false;

        View view = inflater.inflate(R.layout.list_review, parent, shouldAttachParentImmediately);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        try {
            JSONObject review = reviewData.getJSONObject(position);
            String content = review.getString(mContext.getString(R.string.end_review_results_content));
            String author = review.getString(mContext.getString(R.string.end_review_results_author));

            holder.author = author;
            holder.complete_content = content;

            holder.review_author.setText(author);
            holder.review_content.setText(content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if(reviewData == null){
            return 0;
        } else{
            return reviewData.length();
        }
    }

    public void swapData(JSONArray data){
        this.reviewData = data;
        notifyDataSetChanged();
    }
}
