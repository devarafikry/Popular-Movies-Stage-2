package com.tahutelorcommunity.popularmovies.data;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Fikry-PC on 8/2/2017.
 */

public class MovieDbHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "moviesDb.db";
    private static final int VERSION = 1;

    MovieDbHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    private String CREATE_FAVOURITE_TABLE(){
        return  "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " ("
                + MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY, "
                + MovieContract.MovieEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, "
                + MovieContract.MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                + MovieContract.MovieEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, "
                + MovieContract.MovieEntry.COLUMN_DURATION + " INTEGER NOT NULL, "
                + MovieContract.MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, "
                + MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " FLOAT NOT NULL, "
                + MovieContract.MovieEntry.COLUMN_POSTER_BYTE + " BLOB NOT NULL);";
    }

    private String DROP_FAVOURITE_TABLE_IF_EXIST(){
        return "DROP TABLE IF EXIST " + MovieContract.MovieEntry.TABLE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_FAVOURITE_TABLE());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DROP_FAVOURITE_TABLE_IF_EXIST());

        onCreate(sqLiteDatabase);
    }
}
