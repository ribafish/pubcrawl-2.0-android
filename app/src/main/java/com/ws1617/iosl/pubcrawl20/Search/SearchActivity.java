package com.ws1617.iosl.pubcrawl20.Search;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ws1617.iosl.pubcrawl20.R;

/**
 * Created by Haneen on 04/02/2017.
 */

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    static String TAG = SearchActivity.class.getName().toString();


    RecyclerView.LayoutManager eventLayoutManager;
    RecyclerView.LayoutManager pubLayoutManager;
    RecyclerView.LayoutManager personLayoutManager;

    RecyclerView eventsList;
    TextView noEventsTxt;
    EventsListAdapter eventsListAdapter;

    RecyclerView pubsList;
    TextView noPubsTxt;
    PubsListAdapter pubsListAdapter;


    RecyclerView personsList;
    TextView noPersonsTxt;
    PersonListAdapter personListAdapter;

    LinearLayout searchOverLay;
    LinearLayout searchResultContainer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_event);

        initSearchResultView();
        initViewsVisibility(false);


        handleIntent(getIntent());
    }

    private void initSearchResultView() {

        //lists
        eventsList = (RecyclerView) findViewById(R.id.events_list);
        pubsList = (RecyclerView) findViewById(R.id.pubs_list);
        personsList = (RecyclerView) findViewById(R.id.persons_list);

        //no results text
        noEventsTxt = (TextView) findViewById(R.id.no_res_events);
        noPubsTxt = (TextView) findViewById(R.id.no_res_pubs);
        noPersonsTxt = (TextView) findViewById(R.id.no_res_person);

        // set adapters
        eventsListAdapter = new EventsListAdapter(this);
        eventsList.setAdapter(eventsListAdapter);

        pubsListAdapter = new PubsListAdapter(this);
        pubsList.setAdapter(pubsListAdapter);

        personListAdapter = new PersonListAdapter(this);
        personsList.setAdapter(personListAdapter);

        //set layout manager
        eventLayoutManager = new LinearLayoutManager(this);
        eventsList.setLayoutManager(eventLayoutManager);

        pubLayoutManager = new LinearLayoutManager(this);
        pubsList.setLayoutManager(pubLayoutManager);

        personLayoutManager = new LinearLayoutManager(this);
        personsList.setLayoutManager(personLayoutManager);

    }


    private void initViewsVisibility(boolean showResult) {
        int showSearchOverly, listsVisibility;
        if (!showResult) {
            showSearchOverly = View.VISIBLE;
            listsVisibility = View.GONE;
        } else {
            showSearchOverly = View.GONE;
            listsVisibility = View.VISIBLE;
        }

        searchOverLay = (LinearLayout) findViewById(R.id.search_overlay);
        searchOverLay.setVisibility(showSearchOverly);

        searchResultContainer = (LinearLayout) findViewById(R.id.search_result_container);
        searchResultContainer.setVisibility(listsVisibility);

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);

    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
            searchView.setOnQueryTextListener(this);

        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        eventsListAdapter.filter(query, EventSearchListener);
        pubsListAdapter.filter(query, PubSearchListener);
        personListAdapter.filter(query, personSearchListener);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.length() == 0) initViewsVisibility(false);
        else initViewsVisibility(true);

        eventsListAdapter.filter(newText, EventSearchListener);
        pubsListAdapter.filter(newText, PubSearchListener);
        personListAdapter.filter(newText, personSearchListener);
        return true;
    }


    SearchResultView EventSearchListener = new SearchResultView() {
        @Override
        public void setSearchResultSize(int size) {
            if (size <= 0) {
                noEventsTxt.setVisibility(View.VISIBLE);
                eventsList.setVisibility(View.GONE);
            } else {
                noEventsTxt.setVisibility(View.GONE);
                eventsList.setVisibility(View.VISIBLE);
            }
        }
    };

    SearchResultView PubSearchListener = new SearchResultView() {
        @Override
        public void setSearchResultSize(int size) {
            if (size <= 0) {
                noPubsTxt.setVisibility(View.VISIBLE);
                pubsList.setVisibility(View.GONE);
            } else {
                noPubsTxt.setVisibility(View.GONE);
                pubsList.setVisibility(View.VISIBLE);
            }
        }
    };

    SearchResultView personSearchListener = new SearchResultView() {
        @Override
        public void setSearchResultSize(int size) {
            if (size <= 0) {
                noPersonsTxt.setVisibility(View.VISIBLE);
                personsList.setVisibility(View.GONE);
            } else {
                noPersonsTxt.setVisibility(View.GONE);
                personsList.setVisibility(View.VISIBLE);
            }
        }
    };

    interface SearchResultView {
        void setSearchResultSize(int size);
    }

}
