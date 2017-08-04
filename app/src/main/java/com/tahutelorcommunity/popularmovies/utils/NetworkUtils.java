package com.tahutelorcommunity.popularmovies.utils;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Fikry-PC on 7/2/2017.
 */

public class NetworkUtils {
    private final static String MOVIE_DATABASE_BASE_URL = "http://api.themoviedb.org/3/movie/";
    private final static String MOVIE_IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    private final static String YOUTUBE_BASE_URL = "https://www.youtube.com/";
    private final static String REVIEW_END = "reviews";

    //build sort url to sort movies by latest, now playing, top rated, popular, and upcoming
    public static URL buildSortUrl(String sort, String api_key){
        Uri buildUri = Uri.parse(MOVIE_DATABASE_BASE_URL).buildUpon()
                .appendPath(sort)
                .appendQueryParameter("api_key", api_key)
                .build();
        URL url = null;
        try{
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e){
            e.printStackTrace();
        }

        return url;
    }

    //build image url to retrieve movie's image
    public static URL buildImageUrl(String poster_path){
        Uri buildUri = Uri.parse(MOVIE_IMAGE_BASE_URL).buildUpon()
                .appendPath(poster_path)
                .build();
        URL url = null;
        try{
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e){
            e.printStackTrace();
        }

        return url;
    }

    //build image url to retrieve movie's review
    public static URL buildReviewUrl(String movieId, String api_key){
        Uri buildUri = Uri.parse(MOVIE_DATABASE_BASE_URL).buildUpon()
                .appendPath(movieId)
                .appendPath(REVIEW_END)
                .appendQueryParameter("api_key", api_key)
                .build();
        URL url = null;
        try{
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e){
            e.printStackTrace();
        }

        return url;
    }

    //build detail url
    public static URL buildGetData(String movieId, String api_key){
        Uri buildUri = Uri.parse(MOVIE_DATABASE_BASE_URL).buildUpon()
                .appendPath(movieId)
                .appendQueryParameter("api_key", api_key)
                .build();
        URL url = null;
        try{
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e){
            e.printStackTrace();
        }

        return url;
    }

    //build video url
    public static URL buildGetVideo(String movieId, String api_key){
        Uri buildUri = Uri.parse(MOVIE_DATABASE_BASE_URL).buildUpon()
                .appendPath(movieId)
                .appendPath("videos")
                .appendQueryParameter("api_key", api_key)
                .build();
        URL url = null;
        try{
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e){
            e.printStackTrace();
        }

        return url;
    }

    //build video youtube url
    public static URL buildGetYoutubeUrl(String key){
        Uri buildUri = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                .appendPath("watch")
                .appendQueryParameter("v", key)
                .build();
        URL url = null;
        try{
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e){
            e.printStackTrace();
        }

        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException{
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try{
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if(hasInput){
                return scanner.next();
            } else{
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
