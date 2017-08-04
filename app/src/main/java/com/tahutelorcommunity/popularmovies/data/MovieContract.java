package com.tahutelorcommunity.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Fikry-PC on 8/2/2017.
 */

public class MovieContract {
    public static final String AUTHORITY = "com.tahutelorcommunity.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+AUTHORITY);

//    public static final String PATH_MOVIE = "movie";
    public static final String PATH_FAVOURITE_MOVIES = "favourite";

    public static final class MovieEntry implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVOURITE_MOVIES).build();


        public static final String TABLE_NAME = "favourite_movies";

        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_TITLE="title";
        public static final String COLUMN_RELEASE_DATE = "releaseDate";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_VOTE_AVERAGE = "vote";
        public static final String COLUMN_POSTER_BYTE = "posterByte";

    }
}
