package com.robinkanatzar.findamovie;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by robinkanatzar on 4/27/17.
 */

public class BaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());

        } else {

        }
    }
}
