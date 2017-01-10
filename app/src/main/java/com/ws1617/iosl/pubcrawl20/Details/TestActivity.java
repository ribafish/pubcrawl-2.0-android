package com.ws1617.iosl.pubcrawl20.Details;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlot;
import com.ws1617.iosl.pubcrawl20.Details.MiniDataModels.PubMini;
import com.ws1617.iosl.pubcrawl20.Details.MiniDataModels.PubMiniComparator;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Haneen on 15/12/2016.
 */

public class TestActivity extends FragmentActivity  {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        initRouteFragment();
    }


    void initRouteFragment() {
        // this list should come from the DB and from outside the fragment
        // the fragment it self get the data from outsource

        //View mode
        initVewMode();
        //Edit mode
        //TODO

        RouteFragment routeFragment = RouteFragment.newInstance(RouteFragment.DIALOG_STATUS.VIEW_MODE);
        routeFragment.setListOfPubs(pubs);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.test_place_holder,routeFragment,"Route").commit();

    }


    /*
    THESE DUMMY DATA ARE GPING TO USE IN VIEW MODE : when we have an event and we need to show the route info
     */
    void initVewMode(){
        getEvent(-1);
    }

    Event event;
    List<PubMini> pubs = new ArrayList<>();
    // TODO: Change to get it from database when it will be ready
    private void getEvent(long id) {
        ArrayList<Long> dummyIds = new ArrayList<>();
        dummyIds.add((long)0);
        dummyIds.add((long)1);
        dummyIds.add((long)2);
        dummyIds.add((long)3);
        dummyIds.add((long)4);
        dummyIds.add((long)5);
        dummyIds.add((long)6);


        this.event = new Event(
                "Dummy event " + id,
                new Date(116, 11, 31, 20, 0, 0),
                getResources().getString(R.string.lorem),
                true,
                dummyIds,
                12,
                dummyIds
        );

        this.event.setEventId(id);

        ArrayList<TimeSlot> timeSlots= new ArrayList<>();
        timeSlots.add(new TimeSlot(
                1,
                new Date(116, 11, 31, 21, 0, 0),
                new Date(116, 11, 31, 22, 0, 0)
        ));
        timeSlots.add(new TimeSlot(
                0,
                new Date(116, 11, 31, 22, 0, 0),
                new Date(116, 11, 31, 23, 0, 0)
        ));
        timeSlots.add(new TimeSlot(
                2,
                new Date(116, 11, 31, 20, 0, 0),
                new Date(116, 11, 31, 21, 0, 0)
        ));
        this.event.setTimeSlotList(timeSlots);
        getPubMinis(this.event.getPubIds(), this.event.getTimeSlotList());

    }

    // TODO: Change to get it from database when it will be ready
    private void getPubMinis (ArrayList<Long> ids, ArrayList<TimeSlot> slots) {
        ArrayList<Long> mIds = new ArrayList<>(ids);
        for (TimeSlot t : slots) {
            long id = t.getPubId();
            this.pubs.add(new PubMini("Dummy Pub " + id, t, id, new LatLng(52.5 + Math.random()*0.1, 13.35 + Math.random()*0.1)));
            mIds.remove(id);
        }

        Collections.sort(this.pubs, new PubMiniComparator());

        for (Long id : mIds) {
            this.pubs.add(new PubMini("Dummy Pub " + id, null, id, new LatLng(52.5 + Math.random()*0.1, 13.35 + Math.random()*0.1)));
        }
    }




}
