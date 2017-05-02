package com.robinkanatzar.findamovie.api;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by robinkanatzar on 4/28/17.
 */

public interface MovieService {

    @GET("search/movie")
    rx.Observable<SearchResponse> searchForMovie(@Query("api_key") String api_key,
                                                          @Query("page") Integer page,
                                                          @Query("query") String query);

}
