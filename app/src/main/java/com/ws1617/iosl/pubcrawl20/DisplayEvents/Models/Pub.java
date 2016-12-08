package com.ws1617.iosl.pubcrawl20.DisplayEvents.Models;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONObject;

/**
 * Created by gaspe on 18. 11. 2016.
 */

public class Pub {
    private long id;
    private String pubName;
    private LatLng latLng;
    private int size;
    private JSONObject rawJson;
    private Marker marker;

    public Pub(long id, String pubName, LatLng latLng, int size) {
        this.id = id;
        this.pubName = pubName;
        this.latLng = latLng;
        this.size = size;
    }

    public String getPubName() {
        return pubName;
    }

    public LatLng getLatLng() {

        return latLng;
    }

    public int getSize() {
        return size;
    }

    public void setPubName(String pubName) {
        this.pubName = pubName;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Pub{" +
                "id=" + id +
                ", pubName='" + pubName + '\'' +
                ", latLng=" + latLng +
                ", size=" + size +
                '}';
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
