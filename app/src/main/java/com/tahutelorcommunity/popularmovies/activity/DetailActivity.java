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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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

public class DetailActivity extends AppCompatActivity{

    public final static String DETAIL_ACTIVITY_EXTRA = "extra_message";
    public final static String DETAIL_ACTIVITY_CATEGORY_EXTRA = "category";
    public final static String DETAIL_ACTIVITY_DATA_SOURCE = "dataSourceType";
    private int dataSourceType;
    public final static int CURSOR_DATASOURCE = 1;
    public final static int INTERNET_DATASOURCE = 0;

    public String category;
    private TextView error_text;
    private FloatingActionButton reviewButton;
    private ImageView btn_fav, btn_unfav;
    private ScrollView detail_view;
    private ProgressBar pb_loading;
    private RecyclerView trailer_recyclerview;
    private TrailerAdapter trailerAdapter;
    private String index;
    private String api_key ;
    private Intent intent;
    private Toast mToast;
    private String firstTrailerUrl;
    private String title, release_date, duration, description, vote, poster_path;
    private final static int LOADER_ID_TRAILER = 97;
    private final static int LOADER_ID_DETAIL = 12;
    private final static int LOADER_ID_CHECK_FAVOURITE = 17;
    private final static String SORT_URL_ARGS_KEY = "sortUrl";
    private LoaderManager.LoaderCallbacks<Cursor> checkFavouriteCallback;
    private LoaderManager.LoaderCallbacks<String> movieDataCallback;

    private TextView movie_title, movie_year, movie_duration, movie_description, movie_vote, movie_release_date;
    private ImageView movie_image;

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
        setContentView(R.layout.activity_detail);
        intent = getIntent();

        if(intent.hasExtra(DETAIL_ACTIVITY_EXTRA)){
            index = intent.getStringExtra(DETAIL_ACTIVITY_EXTRA);
        }
        if(intent.hasExtra(DETAIL_ACTIVITY_DATA_SOURCE)){
            this.dataSourceType = intent.getIntExtra(DETAIL_ACTIVITY_DATA_SOURCE,0);
        }
        if(intent.hasExtra(DETAIL_ACTIVITY_CATEGORY_EXTRA)){
            category = intent.getStringExtra(DETAIL_ACTIVITY_CATEGORY_EXTRA);
        }
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(category);
        }

        api_key = getString(R.string.tmdb_api_key);
        btn_fav = (ImageView) findViewById(R.id.btn_fav);
        btn_unfav = (ImageView) findViewById(R.id.btn_unfav);
        detail_view = (ScrollView) findViewById(R.id.detail_view);
        pb_loading = (ProgressBar) findViewById(R.id.pb_loading);
        trailer_recyclerview = (RecyclerView) findViewById(R.id.recyclerview_trailers);
        CustomLinearLayoutManager customLayoutManager = new CustomLinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        trailer_recyclerview.setLayoutManager(customLayoutManager);
        trailer_recyclerview.setHasFixedSize(true);
        trailerAdapter = new TrailerAdapter();
        trailer_recyclerview.setAdapter(trailerAdapter);
        reviewButton = (FloatingActionButton) findViewById(R.id.review_button);

        error_text = (TextView) findViewById(R.id.text_error);
        movie_title = (TextView) findViewById(R.id.movie_title);
        movie_year = (TextView) findViewById(R.id.movie_year);
        movie_duration = (TextView) findViewById(R.id.movie_duration);
        movie_vote = (TextView) findViewById(R.id.movie_vote);
        movie_description = (TextView) findViewById(R.id.movie_description);
        movie_release_date = (TextView) findViewById(R.id.movie_release_date);

        movie_image = (ImageView) findViewById(R.id.movie_image);

        switch (dataSourceType){
            case 0:
                initMovieDataCallback();
                break;
        }
        initCheckFavouriteCallback();
        initMovieDataCallback();
        checkFavouriteStatus();
        getData(index);
        getTrailer(index);


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
//                        loadMovie(true);
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
//                        loadMovie(true);
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
                            detail_view.setVisibility(View.VISIBLE);
                            pb_loading.setVisibility(View.GONE);
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

                            poster_path = jsonObject.getString(getString(R.string.end_detail_poster_path)).substring(1);


                            int dur = Integer.valueOf(duration);
                            int min = dur%60;
                            int hour = dur/60;

                            movie_title.setText(title);
                            movie_release_date.setText(release_date);
                            movie_year.setText(release_date.substring(0,4));
                            movie_duration.setText(hour+"h "+min+"min");
                            movie_description.setText(description);
                            movie_vote.setText(vote);
                            Picasso.with(DetailActivity.this).load(String.valueOf(NetworkUtils.buildImageUrl(poster_path))).into(movie_image);

                            Bitmap bitmap = ((BitmapDrawable) movie_image.getDrawable()).getBitmap();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            final byte[] bytes = baos.toByteArray();

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
                                    cv.put(MovieContract.MovieEntry.COLUMN_POSTER_BYTE, bytes);

                                    mNewUri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,
                                            cv);

                                    if(mNewUri != null){
                                        setFavouriteCondition(true);
                                    }

                                    Log.d("Test Insert", mNewUri.toString());
                                    notifyUser(getString(R.string.you_favourited_this));
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
                                        notifyUser(getString(R.string.you_unfavourited_this));
                                    }
                                }
                            });
                        }
                        catch (Exception e){
                            e.printStackTrace();
//                            notifyUser(getString(R.string.error));
                        }
                        break;

                    case LOADER_ID_TRAILER:
                        try {
                            JSONObject initialJson = new JSONObject(data);
                            JSONArray result = initialJson.getJSONArray("results");

                            JSONObject trailerObject = result.getJSONObject(0);
                            String key = trailerObject.getString("key");

                            firstTrailerUrl = Uri.parse(NetworkUtils.buildGetYoutubeUrl(key).toString()).toString();

                            trailerAdapter.setTrailerData(result);
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
                                detail_view.setVisibility(View.VISIBLE);
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

                                final byte[] poster_byte = data.getBlob(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_BYTE));

                                ByteArrayInputStream imageStream = new ByteArrayInputStream(poster_byte);
                                Bitmap poster = BitmapFactory.decodeStream(imageStream);
                                movie_image.setImageBitmap(poster);


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
                                        cv.put(MovieContract.MovieEntry.COLUMN_POSTER_BYTE, poster_byte);

                                        mNewUri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,
                                                cv);

                                        if(mNewUri != null){
                                            setFavouriteCondition(true);
                                        }

                                        Log.d("Test Insert", mNewUri.toString());
                                        notifyUser(getString(R.string.you_favourited_this));
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
                                            notifyUser(getString(R.string.you_unfavourited_this));
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
        startActivity(intent);
    }

    public class CustomLinearLayoutManager extends LinearLayoutManager {
        public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);

        }

        @Override
        public boolean canScrollVertically() {
            return false;
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
        } else if(item.getItemId() == R.id.action_share){
            shareTrailerUrl(firstTrailerUrl);
        }
        return super.onOptionsItemSelected(item);
    }

    }


