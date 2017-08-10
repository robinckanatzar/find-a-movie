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

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

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

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            if (firstVisibleItemPosition == ((mPageNumber * 20) - 5) && !isLoading) {
                isLoading = true;
                loadMoreItems();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        movieAdapter = new MovieAdapter(movieList);
        layoutManager = new LinearLayoutManager(this);
        mResults.setLayoutManager(layoutManager);
        mResults.setAdapter(movieAdapter);
        mResults.addOnScrollListener(recyclerViewOnScrollListener);
    }

    @OnClick(R.id.btn_search) void search() {

        movieList.clear();
        dismissKeyboard();
        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }

        mQueryString = mQuery.getText().toString();

        if (mQueryString == "" || mQueryString.isEmpty() || mQueryString == null) {
            Toast.makeText(this, getString(R.string.error_enter_query), Toast.LENGTH_LONG).show();
        } else {
            mProgressSpinner.setVisibility(View.VISIBLE);

            rx.Observable<SearchResponse> observable = new RestClient().getService().searchForMovie(getString(R.string.API_KEY), mPageNumber, mQueryString);

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
                            isLoading = false;
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
                                totalResultsOnPage = totalResults - (currentPage - 1) * mItemsPerPage;

                                if (totalResultsOnPage < mItemsPerPage) {
                                    max = totalResultsOnPage;
                                } else {
                                    max = 20;
                                }

                                for (int i = 0; i < max; i++) {
                                    if (searchResponse.getResults().get(i).getOriginalTitle() != null) {
                                        title = searchResponse.getResults().get(i).getOriginalTitle().toString();
                                    } else {
                                        title = "";
                                    }

                                    if (searchResponse.getResults().get(i).getOverview() != null) {
                                        description = searchResponse.getResults().get(i).getOverview().toString();
                                    } else {
                                        description = "";
                                    }

                                    if (searchResponse.getResults().get(i).getPosterPath() != null) {
                                        iconUrl = "http://image.tmdb.org/t/p/w500" + searchResponse.getResults().get(i).getPosterPath().toString();
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

        mPageNumber += 1;

        rx.Observable<SearchResponse> observable = new RestClient().getService().searchForMovie(getString(R.string.API_KEY), mPageNumber, mQueryString);

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
                        isLoading = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onNext(SearchResponse searchResponse) {

                        totalResults = searchResponse.getTotalResults();
                        currentPage = searchResponse.getPage();
                        totalPages = searchResponse.getTotalPages();
                        totalResultsOnPage = totalResults - (currentPage - 1) * mItemsPerPage;

                        if (totalResultsOnPage < 20) {
                            max = totalResultsOnPage;
                        } else {
                            max = 20;
                        }

                        for (int i = 0; i < max; i++) {
                            if (searchResponse.getResults().get(i).getOriginalTitle() != null) {
                                title = searchResponse.getResults().get(i).getOriginalTitle().toString();
                            } else {
                                title = "";
                            }

                            if(searchResponse.getResults().get(i).getOverview() != null) {
                                description = searchResponse.getResults().get(i).getOverview().toString();
                            } else {
                                description = "";
                            }

                            if (searchResponse.getResults().get(i).getPosterPath() != null) {
                                iconUrl = getString(R.string.icon_url_base_string) + searchResponse.getResults().get(i).getPosterPath().toString();
                            } else {
                                iconUrl = getString(R.string.default_movie_image);
                            }

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
