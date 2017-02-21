package com.ws1617.iosl.pubcrawl20.Current;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.MapView;
import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.DataModels.EventComparator;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlot;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseException;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper;
import com.ws1617.iosl.pubcrawl20.Database.EventDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.PersonDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.PubDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.RequestQueueHelper;
import com.ws1617.iosl.pubcrawl20.MainActivity;
import com.ws1617.iosl.pubcrawl20.R;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by Gasper Kojek on 9. 11. 2016.
 * Github: https://github.com/ribafish/
 */

public class CurrentFragment extends Fragment {
    private static final String TAG = "CurrentFragment";
    private ArrayList<EventMini> events = new ArrayList<>();
    private EventAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout noEventsLayout;
    private View rootView;
    private BroadcastReceiver receiver;
    private long userId = -1;

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
        GridLayoutManager glm = new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.current_event_recycler_view);
        // Delay attaching adapter to onResume
//        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(glm);
        recyclerView.setHasFixedSize(true);

        Context context = getContext();
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_user), Context.MODE_PRIVATE);
        userId = sharedPref.getLong(context.getString(R.string.user_id), 0);
        getEvents();

        Button findEvents = (Button) rootView.findViewById(R.id.current_find_events_btn);
        findEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ((MainActivity)getActivity()).setTab(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button scanQr = (Button) rootView.findViewById(R.id.current_scan_qr_btn);
        scanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ((MainActivity)getActivity()).startQrScan();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return rootView;
    }

    private void getEvents() {
        events.clear();
        ArrayList<Event> allEvents = new ArrayList<>();
        try {
            PersonDbHelper personDbHelper = new PersonDbHelper();
            EventDbHelper eventDbHelper = new EventDbHelper();
            PubDbHelper pubDbHelper = new PubDbHelper();
            ArrayList<Long> allEventIds = personDbHelper.getEventIds(userId);
            for (Long eventId : allEventIds) {
                Event event = null;
                try {
                    event = eventDbHelper.getEvent(eventId);
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
                ArrayList<TimeSlot> timeSlots = event.getTimeSlotList();
                // Check that the event is current or in the future
                if (event.getDate().after(new Date(System.currentTimeMillis()-24*60*60*1000)) ||
                        TimeSlot.getCombinedTimeSlot(timeSlots).isDateIncluded(new Date(System.currentTimeMillis()))) {
                    EventMini em = new EventMini(event);
                    ArrayList<Long> pubIds = event.getPubIds();
                    for (Long id : pubIds) {
                        boolean added = false;
                        try {
                            Pub p = pubDbHelper.getPub(id);
                            for (TimeSlot ts : timeSlots) {
                                if (ts.getPubId() == p.getId()){
                                    em.addPub(new PubMini(p, ts));
                                    added = true;
                                }
                            }
                            if (!added) em.addPub(new PubMini(p, null));
                        } catch (DatabaseException e) {
                            e.printStackTrace();
                        }
                    }
                    events.add(em);
                }

            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        } catch (SQLiteException ee) {
            return;
        }

        if (events.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            noEventsLayout.setVisibility(View.GONE);
            Collections.sort(events, new EventMiniComparator());
        } else {
            recyclerView.setVisibility(View.GONE);
            noEventsLayout.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (adapter != null) {
            for (MapView m : adapter.getMapViews()) {
                m.onLowMemory();
            }
        }
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
        recyclerView.setAdapter(adapter);
        getContext().registerReceiver(receiver, filter);
        if (adapter != null) {
            for (MapView m : adapter.getMapViews()) {
                m.onResume();
            }
        }
        getEvents();
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(receiver);
        if (adapter != null) {
            for (MapView m : adapter.getMapViews()) {
                m.onPause();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (adapter != null) {
            for (MapView m : adapter.getMapViews()) {
                m.onDestroy();
            }
        }
        super.onDestroy();
    }

}
