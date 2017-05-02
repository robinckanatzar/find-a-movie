package com.robinkanatzar.findamovie;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.robinkanatzar.findamovie.api.RestClient;
import com.robinkanatzar.findamovie.api.SearchResponse;
import com.robinkanatzar.findamovie.recyclerview.Movie;
import com.robinkanatzar.findamovie.recyclerview.MovieAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static android.nfc.tech.MifareUltralight.PAGE_SIZE;
import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    // API Documentation for The Movie DB
    // https://developers.themoviedb.org/3/search/search-people

    @BindView(R.id.et_query) EditText mQuery;
    @BindView(R.id.rv_results) RecyclerView mResults;
    @BindView(R.id.progress_spinner) ProgressBar mProgressSpinner;

    private String mQueryString;
    private List<Movie> movieList = new ArrayList<>();
    private MovieAdapter movieAdapter;
    private Subscription subscription = Subscriptions.empty();
    private Integer mPageNumber = 1;
    private Integer mItemsPerPage = 20;
    private LinearLayoutManager layoutManager;
    private Boolean isLoading = false;
    private Boolean isLastPage = false;

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            Timber.i("visibleItemCount: " + visibleItemCount);
            Timber.i("totalItemCount: " + totalItemCount);
            Timber.i("firstVisibleItemPosition: " + firstVisibleItemPosition);
            Timber.i("isLoading: " + isLoading);
            Timber.i("isLastPage: " + isLastPage);
            Timber.i("PAGE_SIZE: " + PAGE_SIZE);

            // when firstVisibleItemPosition == 16 (out of 20), load more

            if (!isLoading && !isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= PAGE_SIZE) {
                    loadMoreItems();
                }
            }

            if (firstVisibleItemPosition == 16) {
                loadMoreItems();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Timber.i("Inside MainActivity onCreate");

        movieAdapter = new MovieAdapter(movieList);

        layoutManager = new LinearLayoutManager(this);
        mResults.setLayoutManager(layoutManager);
        mResults.setAdapter(movieAdapter);

        mResults.addOnScrollListener(recyclerViewOnScrollListener);
    }

    @OnClick(R.id.btn_search) void search() {

        movieList.clear();
        dismissKeyboard();

        mQueryString = mQuery.getText().toString().replace(" ", "%20");
        if (mQueryString == "" || mQueryString.isEmpty() || mQueryString == null) {
            Toast.makeText(this, "Please enter a city.", Toast.LENGTH_SHORT).show();
        } else {
            mProgressSpinner.setVisibility(View.VISIBLE);

            rx.Observable<SearchResponse> observable = new RestClient().getService().searchForMovieFirstPage(getString(R.string.API_KEY), mPageNumber, mQueryString);

            subscription = observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<SearchResponse>() {

                        String title;
                        String description;
                        String iconUrl;
                        int totalResults;
                        int currentPage;
                        int totalPages;

                        @Override
                        public void onCompleted() {
                            movieAdapter.notifyDataSetChanged();
                            mProgressSpinner.setVisibility(GONE);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.e(e);
                        }

                        @Override
                        public void onNext(SearchResponse searchResponse) {

                            if(searchResponse.getTotalResults() != 0) {
                                totalResults = searchResponse.getTotalResults();
                                currentPage = searchResponse.getPage();
                                totalPages = searchResponse.getTotalPages();

                                if (totalResults < mItemsPerPage) {
                                    mItemsPerPage = totalResults;
                                }

                                for (int i = 0; i < mItemsPerPage; i++) {
                                    if (searchResponse.getResults().get(i).getOriginalTitle() != null) {
                                        title = searchResponse.getResults().get(i).getOriginalTitle().toString();
                                        Timber.d("i = " + i + " " + title);
                                    } else {
                                        title = "";
                                    }

                                    if(searchResponse.getResults().get(i).getOverview() != null) {
                                        description = searchResponse.getResults().get(i).getOverview().toString();
                                        Timber.d("i = " + i + " " + description);
                                    } else {
                                        description = "";
                                    }

                                    if (searchResponse.getResults().get(i).getPosterPath() != null) {
                                        iconUrl = "http://image.tmdb.org/t/p/w500" + searchResponse.getResults().get(i).getPosterPath().toString();
                                        Timber.d("i = " + i + " " + iconUrl);
                                    } else {
                                        iconUrl = getString(R.string.default_movie_image);
                                    }

                                    addMovieToList(title, description, iconUrl);
                                }

                            } else {
                                Toast.makeText(MainActivity.this, getString(R.string.no_results), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void dismissKeyboard() {
        View view = this.getCurrentFocus();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void addMovieToList(String title, String description, String iconUrl) {
        Movie movie = new Movie(title, description, iconUrl);
        movieList.add(movie);
    }

    private void loadMoreItems() {
        isLoading = true;

        mPageNumber += 1;

        rx.Observable<SearchResponse> observable = new RestClient().getService().searchForMovieFirstPage(getString(R.string.API_KEY), mPageNumber, mQueryString);

        subscription = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SearchResponse>() {

                    String title;
                    String description;
                    String iconUrl;
                    int totalResults;
                    int currentPage;
                    int totalPages;
                    int totalResultsOnPage;
                    int max;

                    @Override
                    public void onCompleted() {
                        movieAdapter.notifyDataSetChanged();
                        mProgressSpinner.setVisibility(GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onNext(SearchResponse searchResponse) {

                        totalResults = searchResponse.getTotalResults(); // 57
                        currentPage = searchResponse.getPage(); // 2
                        totalPages = searchResponse.getTotalPages(); // 3
                        totalResultsOnPage = totalResults - (currentPage - 1) * mItemsPerPage; // 57 - (1)(20) => 37

                        if (totalResultsOnPage < 20) { // 37 !< 20
                            max = totalResultsOnPage;
                        } else {
                            max = 20;
                        }

                        for (int i = 0; i < max; i++) {
                            title = searchResponse.getResults().get(i).getOriginalTitle().toString();
                            description = searchResponse.getResults().get(i).getOverview().toString();
                            iconUrl = "http://image.tmdb.org/t/p/w500" + searchResponse.getResults().get(i).getPosterPath().toString();
                            addMovieToList(title, description, iconUrl);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListeners();
        mPageNumber = 1;
        subscription.unsubscribe();
    }

    private void removeListeners(){
        mResults.removeOnScrollListener(recyclerViewOnScrollListener);
    }
}
