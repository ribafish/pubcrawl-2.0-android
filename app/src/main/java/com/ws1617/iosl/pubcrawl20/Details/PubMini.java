package com.ws1617.iosl.pubcrawl20.Details;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.DataModels.PubMiniModel;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlot;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Gasper Kojek on 12. 12. 2016.
 * Github: https://github.com/ribafish/
 */

class PubMini  extends PubMiniModel{
    public PubMini(String name, TimeSlot timeSlot, long id, LatLng latLng) {
        super(name, timeSlot, id, latLng);
    }

    public PubMini(Pub pub, TimeSlot timeSlot) {
        super(pub, timeSlot);
    }
}
