package com.ws1617.iosl.pubcrawl20.DataModels;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.text.SimpleDateFormat;

/**
 * Created by Haneen on 12/01/2017.
 */

public class PubMiniModel {

    String name;
    TimeSlot timeSlot;
    long id;
    LatLng latLng;
    Marker marker;

    SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm");

    public PubMiniModel(String name, TimeSlot timeSlot, long id, LatLng latLng) {
        this.name = name;
        this.timeSlot = timeSlot;
        this.id = id;
        this.latLng = latLng;
    }


    public PubMiniModel(Pub pub, TimeSlot timeSlot) {
        this.name = pub.getPubName();
        this.timeSlot = timeSlot;
        this.id = pub.getId();
        this.latLng = pub.getLatLng();
    }

    public String getTimeSlotTimeString() {
        if (timeSlot != null) {
           String start = localDateFormat.format(timeSlot.getStartTime());
            String end = localDateFormat.format(timeSlot.getEndTime());
            return String.format("%s - %s", start, end);
        } else {
            return "";
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this.id == ((PubMiniModel) obj).id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
