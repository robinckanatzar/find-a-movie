package com.robinkanatzar.findamovie.api;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by robinkanatzar on 4/28/17.
 */

public class RestClient {

    private MovieService service;

    public RestClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(MovieService.class);
    }

    public MovieService getService() {
        return service;
    }

}
