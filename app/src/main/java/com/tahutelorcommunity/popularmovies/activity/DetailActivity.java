package com.tahutelorcommunity.popularmovies.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.tahutelorcommunity.popularmovies.R;
import com.tahutelorcommunity.popularmovies.adapter.MovieAdapter;
import com.tahutelorcommunity.popularmovies.adapter.TrailerAdapter;
import com.tahutelorcommunity.popularmovies.data.MovieContract;
import com.tahutelorcommunity.popularmovies.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity{

    public final static String DETAIL_ACTIVITY_EXTRA = "extra_message";
    public final static String DETAIL_ACTIVITY_CATEGORY_EXTRA = "category";
    public final static String DETAIL_ACTIVITY_DATA_SOURCE = "dataSourceType";
    public final static String DETAIL_ACTIVITY_POSITION = "position";
    private int dataSourceType;
    private int lastTrailerPosition;
    public final static int CURSOR_DATASOURCE = 1;
    public final static int INTERNET_DATASOURCE = 0;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    public String category;
    @BindView(R.id.text_error) TextView error_text;
    @BindView(R.id.review_button) FloatingActionButton reviewButton;
    @BindView(R.id.btn_fav) ImageView btn_fav;
    @BindView(R.id.btn_unfav) ImageView btn_unfav;
    @BindView(R.id.pb_loading) ProgressBar pb_loading;
    @BindView(R.id.recyclerview_trailers) RecyclerView trailer_recyclerview;
    @BindView(R.id.movie_title) TextView movie_title;
    @BindView(R.id.movie_year) TextView movie_year;
    @BindView(R.id.movie_duration) TextView movie_duration;
    @BindView(R.id.movie_description) TextView movie_description;
    @BindView(R.id.movie_vote) TextView movie_vote;
    @BindView(R.id.movie_release_date) TextView movie_release_date;
    @BindView(R.id.movie_image) ImageView movie_image;
    @BindView(R.id.detail_view) LinearLayout content;
    @BindView(R.id.coordinatorLayout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.progress_bar_trailer) ProgressBar progress_bar_trailer;
    @BindView(R.id.trailer_view) LinearLayout trailer_view;

    @BindView(R.id.progress_bar_image) ProgressBar progress_bar_image;

    private TrailerAdapter trailerAdapter;
    private String index;
    private int position;
    private String api_key ;
    private Intent intent;
    private Toast mToast;
    private String firstTrailerUrl;
    private String title, release_date, duration, description, vote, poster_path;
    private final static int LOADER_ID_TRAILER = 97;
    private final static int LOADER_ID_DETAIL = 12;
    private final static String DETAIL_ACTIVITY_LAST_POSITION = "lastPosition";
    private final static int LOADER_ID_CHECK_FAVOURITE = 17;
    private final static String SORT_URL_ARGS_KEY = "sortUrl";
    private LoaderManager.LoaderCallbacks<Cursor> checkFavouriteCallback;
    private LoaderManager.LoaderCallbacks<String> movieDataCallback;
    private Snackbar snackbar;
    private void notifyUserUsingSnackbar(String s, int duration){
        if(snackbar != null){
            snackbar.dismiss();
        }
        snackbar = Snackbar
                .make(coordinatorLayout,s, duration);

        snackbar.show();
    }

    @Override
    protected void onPause() {
        LinearLayoutManager layoutManager = (LinearLayoutManager)trailer_recyclerview.getLayoutManager();
        lastTrailerPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        trailer_recyclerview.scrollToPosition(lastTrailerPosition);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DETAIL_ACTIVITY_LAST_POSITION, lastTrailerPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        lastTrailerPosition = savedInstanceState.getInt(DETAIL_ACTIVITY_LAST_POSITION);
    }

    private void notifyUser(String s){
        if(mToast!=null){
            mToast.cancel();
        }
        mToast = Toast.makeText(this, s, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void loadImage(boolean isLoadImage){
        if(isLoadImage){
            progress_bar_image.setVisibility(View.VISIBLE);
            movie_image.setVisibility(View.INVISIBLE);
        } else{
            progress_bar_image.setVisibility(View.INVISIBLE);
            movie_image.setVisibility(View.VISIBLE);
        }
    }

    private void loadTrailer(boolean isLoadTrailer){
        if(isLoadTrailer){
            progress_bar_trailer.setVisibility(View.VISIBLE);
            trailer_view.setVisibility(View.INVISIBLE);
        } else{
            progress_bar_trailer.setVisibility(View.INVISIBLE);
            trailer_view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);
        if(lastTrailerPosition>0){
            appBarLayout.setExpanded(false);
        }
        intent = getIntent();
        setSupportActionBar(toolbar);

        if(intent.hasExtra(DETAIL_ACTIVITY_EXTRA)){
            index = intent.getStringExtra(DETAIL_ACTIVITY_EXTRA);
        }
        if(intent.hasExtra(DETAIL_ACTIVITY_DATA_SOURCE)){
            this.dataSourceType = intent.getIntExtra(DETAIL_ACTIVITY_DATA_SOURCE,0);
        }
        if(intent.hasExtra(DETAIL_ACTIVITY_CATEGORY_EXTRA)){
            category = intent.getStringExtra(DETAIL_ACTIVITY_CATEGORY_EXTRA);
        }
        if(intent.hasExtra(DETAIL_ACTIVITY_POSITION)){
            position = intent.getIntExtra(DETAIL_ACTIVITY_POSITION,0);
        }
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(category);
        }
        api_key = getString(R.string.tmdb_api_key);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        trailer_recyclerview.setLayoutManager(linearLayoutManager);
        trailer_recyclerview.setHasFixedSize(true);
        trailer_recyclerview.setNestedScrollingEnabled(true);
        trailer_recyclerview.setFocusable(false);

        trailerAdapter = new TrailerAdapter();
        trailer_recyclerview.setAdapter(trailerAdapter);
        reviewButton = (FloatingActionButton) findViewById(R.id.review_button);


        initMovieDataCallback();
        if(dataSourceType == INTERNET_DATASOURCE){
            getData(index);
        }
        initCheckFavouriteCallback();
        checkFavouriteStatus();
        getTrailer(index);
        loadMovie(true);


    }
    private void showError(){
        trailer_recyclerview.setVisibility(View.GONE);
        error_text.setVisibility(View.VISIBLE);
    }

    private void setFavouriteCondition(Boolean isFavourite){
        if(isFavourite){
            btn_unfav.setVisibility(View.VISIBLE);
            btn_fav.setVisibility(View.GONE);
        } else{
            btn_unfav.setVisibility(View.GONE);
            btn_fav.setVisibility(View.VISIBLE);
        }
    }

    private void loadMovie(boolean isLoadMovie){
        if(isLoadMovie){
            pb_loading.setVisibility(View.VISIBLE);
            content.setVisibility(View.INVISIBLE);
        } else {
            pb_loading.setVisibility(View.INVISIBLE);
            content.setVisibility(View.VISIBLE);
        }
    }


    public void shareTrailerUrl(String s){
        String mimeType = "text/plain";
        String message = s;

        ShareCompat.IntentBuilder.from(this)
                .setChooserTitle(title)
                .setType(mimeType)
                .setText(s)
                .startChooser();
    }

    private void initMovieDataCallback(){
        movieDataCallback = new LoaderManager.LoaderCallbacks<String>() {
            @Override
            public Loader<String> onCreateLoader(int id, Bundle args) {
                final String sortUrlString;

                switch (id){
                    case LOADER_ID_DETAIL:
                        sortUrlString = args.getString(SORT_URL_ARGS_KEY);

                        return new AsyncTaskLoader<String>(DetailActivity.this) {
                            @Override
                            protected void onStartLoading() {
                                loadImage(true);
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

                                String movieData = null;
                                try {
                                    movieData = NetworkUtils.getResponseFromHttpUrl(sortUrl);
                                }
                                catch (IOException e){
                                    e.printStackTrace();
                                }
                                return movieData;
                            }

                        };
                    case LOADER_ID_TRAILER:
                        sortUrlString = args.getString(SORT_URL_ARGS_KEY);

                        return new AsyncTaskLoader<String>(DetailActivity.this) {
                            @Override
                            protected void onStartLoading() {
                                loadTrailer(true);

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

                                String movieData = null;
                                try {
                                    movieData = NetworkUtils.getResponseFromHttpUrl(sortUrl);
                                }
                                catch (IOException e){
                                    e.printStackTrace();
                                }
                                return movieData;
                            }

                        };
                    default:
                        throw new RuntimeException("Loader not implemented: "+id);
                }
            }

            @Override
            public void onLoadFinished(Loader<String> loader, String data) {
                int id = loader.getId();
                switch (id) {
                    case LOADER_ID_DETAIL:
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            final String movieId = jsonObject.getString(getString(R.string.end_detail_id));
                            title = jsonObject.getString(getString(R.string.end_detail_title));
                            release_date = jsonObject.getString(getString(R.string.end_detail_release_date));
                            duration = jsonObject.getString(getString(R.string.end_detail_duration));
                            description = jsonObject.getString(getString(R.string.end_detail_description));

                            String vote_s = String.format(
                                    getString(R.string.vote),
                                    jsonObject.getString(getString(R.string.end_detail_vote))
                            );
                            vote = vote_s;
                            loadMovie(false);

                            poster_path = jsonObject.getString(getString(R.string.end_detail_poster_path)).substring(1);
                            String backdrop_path = jsonObject.getString(getString(R.string.end_detail_backdrop_path)).substring(1);

                            int dur = Integer.valueOf(duration);
                            int min = dur%60;
                            int hour = dur/60;

                            movie_title.setText(title);
                            movie_release_date.setText(release_date);
                            movie_year.setText(release_date.substring(0,4));
                            movie_duration.setText(hour+"h "+min+"min");
                            movie_description.setText(description);
                            movie_vote.setText(vote);
                            Picasso.with(DetailActivity.this).load(String.valueOf(NetworkUtils.buildImageUrl(backdrop_path))).into(movie_image, new Callback() {
                                @Override
                                public void onSuccess() {
                                    loadImage(false);
                                    btn_fav.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            new AsyncTask<String, Void, Bitmap>(){

                                                @Override
                                                protected Bitmap doInBackground(String... s) {
                                                    String sUrl = s[0];
                                                    try {
                                                        return  Picasso.with(DetailActivity.this).load(sUrl).get();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    return null;
                                                }

                                                @Override
                                                protected void onPostExecute(Bitmap bitmap) {
                                                    super.onPostExecute(bitmap);
                                                    Bitmap backdrop = ((BitmapDrawable) movie_image.getDrawable()).getBitmap();
                                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                    backdrop.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                                    final byte[] backdrop_bytes = baos.toByteArray();

                                                    Bitmap poster = bitmap;
                                                    ByteArrayOutputStream poster_baos = new ByteArrayOutputStream();
                                                    poster.compress(Bitmap.CompressFormat.JPEG,100,poster_baos);
                                                    final byte[] poster_bytes = poster_baos.toByteArray();

                                                    Uri mNewUri;

                                                    ContentValues cv = new ContentValues();
                                                    cv.put(MovieContract.MovieEntry.COLUMN_TITLE, title);
                                                    cv.put(MovieContract.MovieEntry.COLUMN_DESCRIPTION, description);
                                                    cv.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, vote);
                                                    cv.put(MovieContract.MovieEntry.COLUMN_DURATION, duration);
                                                    cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
                                                    cv.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, release_date);
                                                    cv.put(MovieContract.MovieEntry.COLUMN_POSTER_BYTE, poster_bytes);
                                                    cv.put(MovieContract.MovieEntry.COLUMN_BACKDROP_BYTE, backdrop_bytes);

                                                    mNewUri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,
                                                            cv);

                                                    if(mNewUri != null){
                                                        setFavouriteCondition(true);
                                                    }

                                                    Log.d("Test Insert", mNewUri.toString());
//                                                    notifyUser(getString(R.string.you_favourited_this));
                                                    notifyUserUsingSnackbar(getString(R.string.you_favourited_this), Snackbar.LENGTH_LONG);
                                                }
                                            }.execute(String.valueOf(NetworkUtils.buildImageUrl(poster_path)));

                                        }
                                    });

                                    btn_unfav.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String movieId = index;
                                            Uri uri = MovieContract.MovieEntry.CONTENT_URI;
                                            uri = uri.buildUpon().appendPath(movieId).build();

                                            int result = getContentResolver().delete(uri, null, null);
                                            if(result > 0){
                                                getSupportLoaderManager().restartLoader(LOADER_ID_CHECK_FAVOURITE, null, checkFavouriteCallback);
//                                                notifyUser(getString(R.string.you_unfavourited_this));
                                                notifyUserUsingSnackbar(getString(R.string.you_unfavourited_this), Snackbar.LENGTH_LONG);
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onError() {

                                }
                            });



                        }
                        catch (Exception e){
                            e.printStackTrace();
                              notifyUserUsingSnackbar(getString(R.string.error), Snackbar.LENGTH_INDEFINITE);
//                            finish();
//                            notifyUser(getString(R.string.error));
                        }
                        break;

                    case LOADER_ID_TRAILER:
                        loadTrailer(false);
                        try {
                            JSONObject initialJson = new JSONObject(data);
                            JSONArray result = initialJson.getJSONArray("results");

                            JSONObject trailerObject = result.getJSONObject(0);
                            String key = trailerObject.getString("key");

                            firstTrailerUrl = Uri.parse(NetworkUtils.buildGetYoutubeUrl(key).toString()).toString();

                            trailerAdapter.setTrailerData(result);
                            trailer_recyclerview.scrollToPosition(lastTrailerPosition);

                        }
                        catch (Exception e){
                            e.printStackTrace();
                            showError();
                        }
                        break;
                }
            }

            @Override
            public void onLoaderReset (Loader < String > loader) {
//            movieAdapter.setMovieData(null);
            }
        };
    }

    private void initCheckFavouriteCallback(){

        Uri mNewUri;
        mNewUri = MovieContract.MovieEntry.CONTENT_URI;
        final Uri uri = mNewUri.buildUpon().appendPath(index).build();

        checkFavouriteCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new AsyncTaskLoader<Cursor>(DetailActivity.this) {
                    @Override
                    protected void onStartLoading() {
                        forceLoad();
                    }

                    @Override
                    public Cursor loadInBackground() {
                        return getContentResolver().query(
                                uri,
                                null,
                                null,
                                null,
                                null
                        );
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                switch (loader.getId()){
                    case LOADER_ID_CHECK_FAVOURITE:
                        if(data.getCount() > 0) {
                            setFavouriteCondition(true);
                        } else {
                            setFavouriteCondition(false);
                        }
                        data.moveToFirst();
                        if(dataSourceType == CURSOR_DATASOURCE){
                            try {
                                pb_loading.setVisibility(View.GONE);

                                final String movieId = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
                                title = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE));
                                release_date = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE));
                                duration = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_DURATION));
                                description = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_DESCRIPTION));

                                String vote_s = String.format(
                                        getString(R.string.vote),
                                        data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE))
                                );
                                vote = vote_s;


                                int dur = Integer.valueOf(duration);
                                int min = dur%60;
                                int hour = dur/60;

                                movie_title.setText(title);
                                movie_release_date.setText(release_date);
                                movie_year.setText(release_date.substring(0,4));
                                movie_duration.setText(hour+"h "+min+"min");
                                movie_description.setText(description);
                                movie_vote.setText(vote);

                                final byte[] backdrop_bytes = data.getBlob(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_BACKDROP_BYTE));
                                final byte[] poster_bytes = data.getBlob(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_BYTE));
                                ByteArrayInputStream imageStream = new ByteArrayInputStream(backdrop_bytes);
                                Bitmap poster = BitmapFactory.decodeStream(imageStream);
                                movie_image.setImageBitmap(poster);
                                loadMovie(false);
                                loadImage(false);

                                btn_fav.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Uri mNewUri;

                                        ContentValues cv = new ContentValues();
                                        cv.put(MovieContract.MovieEntry.COLUMN_TITLE, title);
                                        cv.put(MovieContract.MovieEntry.COLUMN_DESCRIPTION, description);
                                        cv.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, vote);
                                        cv.put(MovieContract.MovieEntry.COLUMN_DURATION, duration);
                                        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
                                        cv.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, release_date);
                                        cv.put(MovieContract.MovieEntry.COLUMN_POSTER_BYTE, poster_bytes);
                                        cv.put(MovieContract.MovieEntry.COLUMN_BACKDROP_BYTE, backdrop_bytes);

                                        mNewUri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,
                                                cv);

                                        if(mNewUri != null){
                                            setFavouriteCondition(true);
                                        }

                                        Log.d("Test Insert", mNewUri.toString());
                                        notifyUserUsingSnackbar(getString(R.string.you_favourited_this), Snackbar.LENGTH_LONG);
                                    }
                                });

                                btn_unfav.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String movieId = index;
                                        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
                                        uri = uri.buildUpon().appendPath(movieId).build();

                                        int result = getContentResolver().delete(uri, null, null);
                                        if(result > 0){
                                            getSupportLoaderManager().restartLoader(LOADER_ID_CHECK_FAVOURITE, null, checkFavouriteCallback);
                                            notifyUserUsingSnackbar(getString(R.string.you_unfavourited_this), Snackbar.LENGTH_LONG);
                                        }
                                    }
                                });
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }

    private void checkFavouriteStatus(){
        getSupportLoaderManager().restartLoader(LOADER_ID_CHECK_FAVOURITE, null, checkFavouriteCallback);
    }

    public void openReview(View view) {
        Intent intent = new Intent(this, ReviewActivity.class);
        intent.putExtra(ReviewActivity.MOVIE_ID_CONSTANT, index);
        intent.putExtra(ReviewActivity.MOVIE_TITLE_CONSTANT, title);

        if(index == null || title == null){
            notifyUser(getString(R.string.error));
        } else{
            startActivity(intent);
        }
    }



    private void getTrailer(String movieId){
        URL movieDataUrl = NetworkUtils.buildGetVideo(movieId, api_key);
        Bundle bundle = new Bundle();
        bundle.putString(SORT_URL_ARGS_KEY, movieDataUrl.toString());

        getSupportLoaderManager().restartLoader(LOADER_ID_TRAILER, bundle, movieDataCallback);
    }
    private void getData(String movieId){
        URL movieDataUrl = NetworkUtils.buildGetData(movieId, api_key);
        Bundle bundle = new Bundle();
        bundle.putString(SORT_URL_ARGS_KEY, movieDataUrl.toString());

        getSupportLoaderManager().restartLoader(LOADER_ID_DETAIL, bundle, movieDataCallback);
    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportLoaderManager().destroyLoader(LOADER_ID_CHECK_FAVOURITE);
        getSupportLoaderManager().destroyLoader(LOADER_ID_DETAIL);
        getSupportLoaderManager().destroyLoader(LOADER_ID_TRAILER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        else if(item.getItemId() == R.id.action_share){
            shareTrailerUrl(firstTrailerUrl);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.LAST_CLICKED_ITEM_EXTRA, position);
        setResult(RESULT_OK, intent);
        finish();
    }
}


