package com.ws1617.iosl.pubcrawl20.Current;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlot;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseException;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper;
import com.ws1617.iosl.pubcrawl20.Database.EventDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.PubDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.RequestQueueHelper;
import com.ws1617.iosl.pubcrawl20.R;

import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by Gasper Kojek on 9. 11. 2016.
 * Github: https://github.com/ribafish/
 */

public class CurrentFragment extends Fragment {
    public static final String TITLE = "Current";
    private static final String TAG = "CurrentFragment";
    private ArrayList<EventMini> events = new ArrayList<>();
    private EventAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout noEventsLayout;
    private View rootView;
    private BroadcastReceiver receiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_current, container, false);

        noEventsLayout = (LinearLayout) rootView.findViewById(R.id.current_no_events_layout);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        adapter = new EventAdapter(getActivity(), events, metrics, fm);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());

        recyclerView = (RecyclerView) rootView.findViewById(R.id.current_event_recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(llm);

        getEvents();

        Button findEvents = (Button) rootView.findViewById(R.id.current_find_events_btn);
        findEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return rootView;
    }

    private void getEvents() {
        events.clear();
        ArrayList<Event> allEvents = new ArrayList<>();
        EventDbHelper eventDbHelper = new EventDbHelper();
        PubDbHelper pubDbHelper = new PubDbHelper();
        try {
            allEvents.addAll(eventDbHelper.getAllEvents());
        } catch (DatabaseException e) {
            e.printStackTrace();
        }


        for (Event event : allEvents) {
            //TODO: Filter events for current user and current date
            EventMini em = new EventMini(event);
            ArrayList<Long> pubIds = event.getPubIds();
            ArrayList<TimeSlot> timeSlots = event.getTimeSlotList();
            for (Long id : pubIds) {
                try {
                    Pub p = pubDbHelper.getPub(id);
                    for (TimeSlot ts : timeSlots){
                        if (ts.getPubId() == p.getId()) em.addPub(new PubMini(p, ts));
                    }
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }
            events.add(em);
        }
        if (events.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            noEventsLayout.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            noEventsLayout.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(RequestQueueHelper.BROADCAST_INTENT)) {
                    Log.d(TAG, "Got database refreshed broadcast");
                    getEvents();
                }
            }
        };
        IntentFilter filter = new IntentFilter(RequestQueueHelper.BROADCAST_INTENT);
        getContext().registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(receiver);
    }

}
