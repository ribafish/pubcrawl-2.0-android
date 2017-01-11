package com.ws1617.iosl.pubcrawl20.DataModels;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.text.SimpleDateFormat;

/**
 * Created by Haneen on 11/01/2017.
 */

public class SelectedPub {

    Pub pub;
    TimeSlot timeSlot;
    Marker marker;


    public SelectedPub(Pub pub, TimeSlot timeSlot) {
        this.pub = pub;
        this.timeSlot = timeSlot;
    }

    public String getTimeSlotTimeString() {
        if (timeSlot != null) {
            SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm");
            String start = localDateFormat.format(timeSlot.getStartTime());
            String end = localDateFormat.format(timeSlot.getEndTime());
            return String.format("%s - %s", start, end);
        } else {
            return "";
        }
    }

    public Pub getPub(){
        return pub;
    }

    public String getName() {
        return pub.getPubName();
    }

    public void setName(String name) {
        pub.setPubName(name);
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public long getId() {
        return pub.getId();
    }

    public void setId(long id) {
        pub.setId(id);
    }

    public LatLng getLatLng() {
        return pub.getLatLng();
    }

    public void setLatLng(LatLng latLng) {
        pub.setLatLng(latLng);
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
