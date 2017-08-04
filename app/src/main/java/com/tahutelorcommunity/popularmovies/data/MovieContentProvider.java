package com.tahutelorcommunity.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Fikry-PC on 8/2/2017.
 */

public class MovieContentProvider extends ContentProvider {

    public static final int MOVIES_FAVOURITE = 100;
    public static final int MOVIES_FAVOURITE_WITH_ID = 101;

    public static final UriMatcher sUriMatcher = buildUriMatcher();

    private MovieDbHelper mMovieDbHelper;

    private static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_FAVOURITE_MOVIES, MOVIES_FAVOURITE);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_FAVOURITE_MOVIES + "/#", MOVIES_FAVOURITE_WITH_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mMovieDbHelper = new MovieDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mMovieDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match){
            case MOVIES_FAVOURITE:
                retCursor = db.query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MOVIES_FAVOURITE_WITH_ID:
                String sel = MovieContract.MovieEntry.COLUMN_MOVIE_ID+"=?";
                String[] args = new String[]{uri.getPathSegments().get(1)};
                retCursor = db.query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        sel,
                        args,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri : "+uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        Uri returnUri;
        long id;
        String table_name;
        Uri content_uri;
        switch(match){
            case MOVIES_FAVOURITE:
                table_name = MovieContract.MovieEntry.TABLE_NAME;
                content_uri = MovieContract.MovieEntry.CONTENT_URI;
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri :"+uri +" match:"+match);
        }

        id = db.insert(table_name,
                null, contentValues);
        if(id > 0){
            returnUri = ContentUris.withAppendedId(content_uri, id);
        } else {
            throw new android.database.SQLException("Failed to insert row into "+uri);
        }

        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        int movieDeleted;

        switch (match){
            case MOVIES_FAVOURITE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                movieDeleted = db.delete(
                        MovieContract.MovieEntry.TABLE_NAME,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID+"=?",
                        new String[]{id}
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri : "+uri);
        }

        if(movieDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return movieDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
