package com.ws1617.iosl.pubcrawl20.Details;

import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlotComparator;

import java.util.Comparator;

/**
 * Created by Gasper Kojek on 12. 12. 2016.
 * Github: https://github.com/ribafish/
 */

class PubMiniComparator implements Comparator<PubMini> {
    @Override
    public int compare(PubMini pubMini, PubMini t1) {
        return new TimeSlotComparator(true, TimeSlotComparator.START)
                .compare(pubMini.timeSlot, t1.timeSlot);
    }
}
