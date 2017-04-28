package com.robinkanatzar.findamovie;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    // API Documentation for The Movie DB
    // https://developers.themoviedb.org/3/search/search-people

    @BindView(R.id.et_query) EditText mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Timber.i("Inside MainActivity onCreate");
        Log.d("RCK", "inside onCreate");
    }
}
