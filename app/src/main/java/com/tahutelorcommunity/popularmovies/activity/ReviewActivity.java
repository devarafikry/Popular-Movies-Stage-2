package com.tahutelorcommunity.popularmovies.activity;

import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.tahutelorcommunity.popularmovies.R;
import com.tahutelorcommunity.popularmovies.adapter.ReviewAdapter;
import com.tahutelorcommunity.popularmovies.databinding.ActivityReviewBinding;
import com.tahutelorcommunity.popularmovies.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ReviewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{

    private static final int LOADER_ID_REVIEW = 76;
    private static final String URL_REVIEW_STRING_CONSTANT = "urlReview";
    public static final String MOVIE_ID_CONSTANT ="movieId";
    public static final String MOVIE_TITLE_CONSTANT = "movieTitle";
    private String movieId;
    private String movieTitle;
    private ReviewAdapter reviewAdapter;
    private ActivityReviewBinding mBinding;
    private Toast mToast;

    private void notifyUser(String s){
        if(mToast!=null){
            mToast.cancel();
        }
        mToast = Toast.makeText(this, s, Toast.LENGTH_LONG);
        mToast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_review);

        reviewAdapter = new ReviewAdapter(this);
        movieId = getIntent().getStringExtra(MOVIE_ID_CONSTANT);
        movieTitle = getIntent().getStringExtra(MOVIE_TITLE_CONSTANT);

        String title = String.format(
                getString(R.string.review_of),
                movieTitle
        );

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
        }


        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mBinding.recyclerviewReview.setLayoutManager(linearLayoutManager);
        }
        else{
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this,4);
            mBinding.recyclerviewReview.setLayoutManager(gridLayoutManager);
        }
        mBinding.recyclerviewReview.setAdapter(reviewAdapter);

        getReview(movieId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportLoaderManager().destroyLoader(LOADER_ID_REVIEW);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getReview(String movieId){
        URL reviewDataUrl = NetworkUtils.buildReviewUrl(movieId, getString(R.string.tmdb_api_key));
        Bundle bundle = new Bundle();
        bundle.putString(URL_REVIEW_STRING_CONSTANT, reviewDataUrl.toString());

        getSupportLoaderManager().restartLoader(LOADER_ID_REVIEW, bundle, this);
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        switch (id){
            case LOADER_ID_REVIEW:
                final String urlString = args.getString(URL_REVIEW_STRING_CONSTANT);
                return new AsyncTaskLoader<String>(this) {
                    @Override
                    protected void onStartLoading() {
                        forceLoad();
                    }

                    @Override
                    public String loadInBackground() {
                        URL reviewUrl = null;
                        String reviewData = null;
                        try{
                            reviewUrl = new URL(urlString);
                        } catch (MalformedURLException e){
                            e.printStackTrace();
                        }

                        try{
                            reviewData = NetworkUtils.getResponseFromHttpUrl(reviewUrl);
                        } catch (Exception e){
                            e.printStackTrace();

                        }
                        return reviewData;
                    }

                };
            default:
                throw new RuntimeException("Loader not implemented: "+id);
        }
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        switch (loader.getId()){
            case LOADER_ID_REVIEW:
                JSONObject initialJson = null;
                try {
                    initialJson = new JSONObject(data);
                    JSONArray result = initialJson.getJSONArray("results");
                    reviewAdapter.swapData(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    notifyUser(getString(R.string.error));
                    showError();
                }
                break;
            default:
                throw new RuntimeException("Loader not implemented: "+loader.getId());
        }
    }

    private void showError(){
        mBinding.recyclerviewReview.setVisibility(View.GONE);
        mBinding.errorText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
