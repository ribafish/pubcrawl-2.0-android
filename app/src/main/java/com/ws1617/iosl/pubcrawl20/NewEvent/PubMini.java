package com.ws1617.iosl.pubcrawl20.NewEvent;

import com.google.android.gms.maps.model.LatLng;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.DataModels.PubMiniModel;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlot;

/**
 * Created by Haneen on 12/01/2017.
 */

class PubMini extends PubMiniModel {
    public PubMini(String name, TimeSlot timeSlot, long id, LatLng latLng) {
        super(name, timeSlot, id, latLng);
    }

    public PubMini(Pub pub, TimeSlot timeSlot) {
        super(pub, timeSlot);
    }
}
