package com.ws1617.iosl.pubcrawl20.NewEvent;

import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Haneen on 11/19/2016.
 */

public class NewEventController {

    public final String EVENT_TAG = "EVENT_TAG";
    private List<Fragment> fragmentsList;


    private static Event mEvent;

    public Event getEvent() {
        return mEvent;
    }


    public NewEventController() {

        fragmentsList = new ArrayList<>();
        fragmentsList.add(new NewEventSettingsFragment());
        fragmentsList.add(new NewEventRouteFragment());
        fragmentsList.add(new NewEventShareFragment());

    }

    public List<Fragment> getFragmentList() {
        return fragmentsList;
    }


    public void restoreOldEvent(Event oldEvent) {
        this.mEvent = oldEvent;
    }

    public void initNewEvent() {
        mEvent = new Event();
    }


    public void collectEventData() {

        mEvent.eventGeneralInfo = ((NewEventSettingsFragment) fragmentsList.get(0)).CollectGeneralInfo();
        mEvent.eventPubInfo = ((NewEventRouteFragment) fragmentsList.get(1)).CollectPublInfo();


    }
}
