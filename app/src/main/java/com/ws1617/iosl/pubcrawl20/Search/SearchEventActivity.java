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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseException;
import com.ws1617.iosl.pubcrawl20.Database.EventDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.PubDbHelper;
import com.ws1617.iosl.pubcrawl20.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Haneen on 04/02/2017.
 */

public class SearchEventActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    static String TAG = SearchEventActivity.class.getName().toString();

    RecyclerView eventsList;
    EventsListAdapter eventsListAdapter;
    RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);


        eventsList = (RecyclerView) findViewById(R.id.events_list);
        layoutManager = new LinearLayoutManager(this);

        eventsListAdapter = new EventsListAdapter();
        eventsList.setAdapter(eventsListAdapter);
        eventsList.setLayoutManager(layoutManager);



        handleIntent(getIntent());
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
        eventsListAdapter.filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        eventsListAdapter.filter(newText);
        return true;
    }


    class EventsListAdapter extends RecyclerView.Adapter<EventItem> {
        List<EventMini> eventListCopy;
        List<EventMini> eventList;

        private void getEvents() {
            eventList = new ArrayList<>();
            EventDbHelper eventDbHelper = new EventDbHelper();
            PubDbHelper pubDbHelper = new PubDbHelper();
            try {
                ArrayList<Event> el = eventDbHelper.getAllEvents();
                for (Event e : el) {
                    EventMini eventMini = new EventMini(e);
                    for (long pubId : e.getPubIds()) {
                        try {
                            Pub p = pubDbHelper.getPub(pubId);
                            eventMini.addPub(new PubMini(p));
                        } catch (Exception exx) {
                            exx.printStackTrace();
                        }
                    }
                    eventList.add(eventMini);
                }
            } catch (DatabaseException ex) {
                ex.printStackTrace();
            }
            Log.d(TAG, "eventAdapter.notifyDataSetChanged()");
            Collections.sort(eventList, new EventMiniComparator(true, EventMiniComparator.NAME));
            notifyDataSetChanged();

        }

        public EventsListAdapter() {
            getEvents();
            eventListCopy = eventList;
        }


        public void filter(String text) {
            eventList = new ArrayList<>();
            if (text.isEmpty()) {
                eventList.addAll(eventListCopy);
            } else {
                text = text.toLowerCase();
                for (EventMini item : eventListCopy) {
                    if (item.getName().toLowerCase().contains(text)) {
                         eventList.add(item);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public EventItem onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_events, parent, false);
            EventItem eventView = new EventItem(view);
            return eventView;
        }

        @Override
        public void onBindViewHolder(EventItem holder, int position) {
            holder.name.setText(eventList.get(position).getName());
            holder.description.setText(eventList.get(position).getDescription());
            if (eventList.get(position).isSelected()) {
                holder.relativeLayout.setBackgroundColor(Color.LTGRAY);
            } else {
                holder.relativeLayout.setBackgroundColor(Color.WHITE);
            }
            SimpleDateFormat localDateFormat = new SimpleDateFormat("E, MMM d, yyyy");
            holder.date.setText(localDateFormat.format(eventList.get(position).getDate()));
        }

        @Override
        public int getItemCount() {
            return eventList.size();
        }


    }

    class EventItem extends RecyclerView.ViewHolder {
        public TextView name, description, date;
        public RelativeLayout relativeLayout;
        public Button btn_details;

        public EventItem(View itemView) {
            super(itemView);
            this.name = (TextView) itemView.findViewById(R.id.event_name);
            this.description = (TextView) itemView.findViewById(R.id.event_description);
            this.relativeLayout = (RelativeLayout) itemView.findViewById(R.id.event_layout);
            this.btn_details = (Button) itemView.findViewById(R.id.event_details_btn);
            this.date = (TextView) itemView.findViewById(R.id.event_date);
        }
    }
}
