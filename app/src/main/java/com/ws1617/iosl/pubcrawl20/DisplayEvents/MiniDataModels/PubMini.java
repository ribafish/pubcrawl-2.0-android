package com.ws1617.iosl.pubcrawl20.DisplayEvents.MiniDataModels;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;

/**
 * Created by Gasper Kojek on 13. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class PubMini {
    private long id;
    private String name;
    private LatLng latLng;
    private Marker marker;

    public PubMini(long id, String name, LatLng latLng) {
        this.id = id;
        this.name = name;
        this.latLng = latLng;
    }

    public PubMini(Pub pub) {
        this.id = pub.getId();
        this.name = pub.getPubName();
        this.latLng = pub.getLatLng();
        this.marker = null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
