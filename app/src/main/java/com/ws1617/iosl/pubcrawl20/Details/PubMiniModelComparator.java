package com.ws1617.iosl.pubcrawl20.Details;

import com.ws1617.iosl.pubcrawl20.DataModels.PubMiniModel;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlotComparator;

import java.util.Comparator;

/**
 * Created by Gasper Kojek on 12. 12. 2016.
 * Github: https://github.com/ribafish/
 */

class PubMiniModelComparator implements Comparator<PubMiniModel> {
    @Override
    public int compare(PubMiniModel pubMini, PubMiniModel t1) {
        return new TimeSlotComparator(true, TimeSlotComparator.START)
                .compare(pubMini.getTimeSlot(), t1.getTimeSlot());
    }
}
