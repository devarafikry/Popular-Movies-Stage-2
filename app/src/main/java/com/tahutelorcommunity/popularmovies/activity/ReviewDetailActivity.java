package com.tahutelorcommunity.popularmovies.activity;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.tahutelorcommunity.popularmovies.R;
import com.tahutelorcommunity.popularmovies.databinding.ActivityReviewDetailBinding;

public class ReviewDetailActivity extends AppCompatActivity {

    public final static String EXTRA_COMPLETE_CONTENT ="completeContent";
    public final static String EXTRA_AUTHOR ="author";

    private ActivityReviewDetailBinding mBinding;
    private String author, content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_review_detail);

        author = getIntent().getStringExtra(EXTRA_AUTHOR);
        content = getIntent().getStringExtra(EXTRA_COMPLETE_CONTENT);

        String title = String.format(
                getString(R.string.review_by),
                author
        );

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
        }

        mBinding.reviewAuthor.setText(title);
        mBinding.reviewContent.setText(content);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
