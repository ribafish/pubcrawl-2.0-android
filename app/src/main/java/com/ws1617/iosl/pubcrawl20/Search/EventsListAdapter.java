package com.ws1617.iosl.pubcrawl20.Search;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.ws1617.iosl.pubcrawl20.Details.EventDetailsActivity;
import com.ws1617.iosl.pubcrawl20.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Haneen on 09/02/2017.
 */

public class EventsListAdapter extends RecyclerView.Adapter<EventsListAdapter.EventItem> {

    static final String TAG = EventsListAdapter.class.getName();
    List<EventMini> eventListCopy;
    List<EventMini> eventList;
    Context context;

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

    public EventsListAdapter(Context context) {
        getEvents();
        this.context = context;
        eventListCopy = eventList;
    }


    public void filter(String text,  SearchActivity.SearchResultView searchResultView) {
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
        searchResultView.setSearchResultSize(eventList.size());
        notifyDataSetChanged();
    }

    @Override
    public EventItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_search_list_row, parent, false);
        EventItem eventView = new EventItem(view);
        return eventView;
    }

    @Override
    public void onBindViewHolder(EventItem holder, int position) {
        final EventMini  eventItem = eventList.get(position);
        holder.name.setText(eventItem.getName());
        SimpleDateFormat localDateFormat = new SimpleDateFormat("E, MMM d, yyyy");
        holder.date.setText(localDateFormat.format(eventItem.getDate()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EventDetailsActivity.class);
                intent.putExtra("id",eventItem.getEventId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }


    class EventItem extends RecyclerView.ViewHolder {
        public TextView name, date;

        public EventItem(View itemView) {
            super(itemView);
            this.name = (TextView) itemView.findViewById(R.id.event_name);
          this.date = (TextView) itemView.findViewById(R.id.event_date);
        }
    }



}