package com.robinkanatzar.findamovie;

import android.app.Application;
import android.util.Log;

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
            Log.d("RCK", "in app onCreate in debug config");
            Timber.i("debug config selected");
        } else {
            Log.d("RCK", "in app onCreate but not in debug config");
        }
    }
}
