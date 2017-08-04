package com.tahutelorcommunity.popularmovies.activity;

import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tahutelorcommunity.popularmovies.R;
import com.tahutelorcommunity.popularmovies.adapter.FavouriteAdapter;
import com.tahutelorcommunity.popularmovies.adapter.MovieAdapter;
import com.tahutelorcommunity.popularmovies.data.MovieContract;
import com.tahutelorcommunity.popularmovies.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{

    private TextView error_text;
    private RecyclerView movieRecyclerView;
    private ProgressBar progressBarMovies;
    private MovieAdapter movieAdapter;
    private FavouriteAdapter favouriteAdapter;
    private String api_key ;
    private ActionBar actionBar;
    private String action_bar_name;
    private String currentCategory;
    private String currentCategoryKey;
    private LoaderManager.LoaderCallbacks<Cursor> favouriteCallback;

    private static final String SORT_URL_ARGS_KEY = "sortUrlString";
    private static final String SORT_CATEGORY_ARGS_KEY = "sortCategoryString";
    private static final String SORT_CATEGORY_KEY_ARGS_KEY = "sortCategoryKeyString";
    private static final String SORT_ARGS_KEY = "sortString";
    private static final int ID_MOVIE_LOADER = 87;
    private static final int ID_FAVOURITE_LOADER = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api_key = getResources().getString(R.string.tmdb_api_key);
        actionBar = getSupportActionBar();
        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(SORT_CATEGORY_KEY_ARGS_KEY) &&
                    savedInstanceState.containsKey(SORT_CATEGORY_ARGS_KEY)){
                currentCategoryKey = savedInstanceState.getString(SORT_CATEGORY_KEY_ARGS_KEY);
                currentCategory = savedInstanceState.getString(SORT_CATEGORY_ARGS_KEY);
            }
        } else{
            currentCategoryKey = getString(R.string.sort_popular);
            this.currentCategory = getString(R.string.popular_movies);
        }

        action_bar_name = currentCategory;

        if(getSupportActionBar()!=null){
            actionBar.setTitle(action_bar_name);
        }

        error_text = (TextView) findViewById(R.id.text_error);
        progressBarMovies = (ProgressBar) findViewById(R.id.progress_bar_movies);
        movieRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        LinearLayoutManager layoutManager;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            layoutManager = new GridLayoutManager(this,2);
        }
        else{
            layoutManager = new GridLayoutManager(this,4);
        }
        movieRecyclerView.setLayoutManager(layoutManager);
        movieRecyclerView.setHasFixedSize(true);
        movieAdapter = new MovieAdapter();
        favouriteAdapter = new FavouriteAdapter();

        movieRecyclerView.setAdapter(movieAdapter);
        initFavouriteCallback();

        sortMovie(currentCategoryKey, action_bar_name);
    }

    private void loadMovie(boolean isLoadMovie){
        showError(false);
        if(isLoadMovie){
            progressBarMovies.setVisibility(View.VISIBLE);
            movieRecyclerView.setVisibility(View.INVISIBLE);
        } else{
            progressBarMovies.setVisibility(View.INVISIBLE);
            movieRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(currentCategory!=null && currentCategoryKey!=null){
            sortMovie(currentCategoryKey, currentCategory);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SORT_CATEGORY_KEY_ARGS_KEY, currentCategoryKey);
        outState.putString(SORT_CATEGORY_ARGS_KEY, currentCategory);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_popular:
                currentCategoryKey = getString(R.string.sort_popular);
                action_bar_name = getString(R.string.popular_movies);
                actionBar.setTitle(action_bar_name);
                this.currentCategory = action_bar_name;
                sortMovie(currentCategoryKey, currentCategory);
                break;
            case R.id.action_now_playing:
                action_bar_name = getString(R.string.now_playing_movies);
                actionBar.setTitle(action_bar_name);
                this.currentCategory = action_bar_name;
                currentCategoryKey = getString(R.string.sort_now_playing);
                sortMovie(currentCategoryKey, currentCategory);
                break;
            case R.id.action_top_rated:
                action_bar_name = getString(R.string.top_rated_movies);
                actionBar.setTitle(action_bar_name);
                this.currentCategory = action_bar_name;
                currentCategoryKey = getString(R.string.sort_top_rated);
                sortMovie(currentCategoryKey,currentCategory);
                break;
            case R.id.action_upcoming:
                action_bar_name = getString(R.string.upcoming_movies);
                actionBar.setTitle(action_bar_name);
                currentCategoryKey = getString(R.string.sort_upcoming);
                this.currentCategory = action_bar_name;
                sortMovie(currentCategoryKey, currentCategory);
                break;
            case R.id.action_favourite:
                action_bar_name = getString(R.string.favourited_movies);
                actionBar.setTitle(action_bar_name);
                currentCategoryKey = getString(R.string.sort_favourite);
                this.currentCategory = action_bar_name;
                sortMovie(currentCategoryKey, currentCategory);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortMovie(final String sortBy, final String category){
        if(sortBy.equals(getString(R.string.sort_favourite))){
            getSupportLoaderManager().restartLoader(ID_FAVOURITE_LOADER, null, favouriteCallback);
        } else{
            movieRecyclerView.swapAdapter(movieAdapter, true);
            movieAdapter.notifyDataSetChanged();
            URL sortMovieUrl = NetworkUtils.buildSortUrl(sortBy, api_key);
            Bundle bundle = new Bundle();
            bundle.putString(SORT_CATEGORY_ARGS_KEY, category);
            bundle.putString(SORT_URL_ARGS_KEY, sortMovieUrl.toString());
            getSupportLoaderManager().restartLoader(ID_MOVIE_LOADER, bundle, this);
        }

    }
//
    private void initFavouriteCallback(){
        favouriteCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Uri movieQueryUri = MovieContract.MovieEntry.CONTENT_URI;
                loadMovie(true);
                return new CursorLoader(MainActivity.this,
                        movieQueryUri,
                        null,
                        null,
                        null,
                        null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                favouriteAdapter.swapCursor(data, currentCategory);
                movieRecyclerView.swapAdapter(favouriteAdapter,false);
                loadMovie(false);
//                    favouriteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }


    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        final String sortUrlString = args.getString(SORT_URL_ARGS_KEY);
        final String category = args.getString(SORT_CATEGORY_ARGS_KEY);
        switch (id){
            case ID_MOVIE_LOADER:
                return new AsyncTaskLoader<String>(this) {
                    @Override
                    protected void onStartLoading() {
                        loadMovie(true);
                        forceLoad();
                    }

                    @Override
                    public String loadInBackground() {
                        URL sortUrl = null;
                        try {
                            sortUrl = new URL(sortUrlString);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                        String movieSearchResult = null;
                        try {
                            movieSearchResult = NetworkUtils.getResponseFromHttpUrl(sortUrl);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        return movieSearchResult;
                    }

                };

            default:
                throw new RuntimeException("Loader not implemented: "+id);
        }
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        try {
            JSONObject initialJson = new JSONObject(data);
            JSONArray  jsonArray = initialJson.getJSONArray("results");
            movieAdapter.setMovieData(jsonArray, currentCategory);
            loadMovie(false);
        }
        catch (Exception e){
            e.printStackTrace();
            showError(true);
        }
    }

    private void showError(boolean isError){
        if(isError){
            loadMovie(false);
            error_text.setVisibility(View.VISIBLE);
        } else{
            error_text.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        movieAdapter.setMovieData(null,null);
    }
}
