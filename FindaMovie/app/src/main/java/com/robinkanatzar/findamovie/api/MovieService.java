package com.robinkanatzar.findamovie.api;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by robinkanatzar on 4/28/17.
 */

public interface MovieService {

    // https://api.themoviedb.org/3/search/movie?api_key=a755518bdaba7680a8b160f485220377&page=1&query=terminator
    @GET("search/movie")
    rx.Observable<SearchResponse> searchForMovie(@Query("api_key") String api_key,
                                                 @Query("page") Integer page,
                                                 @Query("query") String query);


    // http://image.tmdb.org/t/p/w500/5JU9ytZJyR3zmClGmVm9q4Geqbd.jpg
    // base url, size, file path
    @GET("movie/{movie_id}/images")
    rx.Observable<MovieImageResponse> searchForMovieImages(@Path("movie_id") Long movie_id,
                                                           @Query("api_key") String api_key);

}
