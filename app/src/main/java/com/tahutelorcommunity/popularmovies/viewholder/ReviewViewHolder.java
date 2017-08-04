package com.tahutelorcommunity.popularmovies.viewholder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tahutelorcommunity.popularmovies.R;
import com.tahutelorcommunity.popularmovies.activity.DetailActivity;
import com.tahutelorcommunity.popularmovies.activity.ReviewDetailActivity;
import com.tahutelorcommunity.popularmovies.databinding.ListReviewBinding;

import org.json.JSONObject;

/**
 * Created by Fikry-PC on 8/3/2017.
 */

public class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    ListReviewBinding mBinding;
    View view;
    Context context;
    public TextView review_author;
    public TextView review_content;
    public String complete_content;
    public String author;

    public ReviewViewHolder(View view){
        super(view);
        mBinding = DataBindingUtil.bind(view);

        this.view = view;
        context = view.getContext();
        review_author = mBinding.reviewAuthor;
        review_content = mBinding.reviewContent;
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            review_content.setMaxLines(4);
        }
        else{
            review_content.setMaxLines(8);
        }
        view.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        Intent intent = new Intent(context, ReviewDetailActivity.class);
        intent.putExtra(ReviewDetailActivity.EXTRA_AUTHOR, author);
        intent.putExtra(ReviewDetailActivity.EXTRA_COMPLETE_CONTENT, complete_content);

        context.startActivity(intent);
    }
}
