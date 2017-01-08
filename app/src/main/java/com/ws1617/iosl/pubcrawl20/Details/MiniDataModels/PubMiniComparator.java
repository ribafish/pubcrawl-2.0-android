package com.ws1617.iosl.pubcrawl20.details.MiniDataModels;

import com.ws1617.iosl.pubcrawl20.dataModels.TimeSlotComparator;

import java.util.Comparator;

/**
 * Created by Gasper Kojek on 12. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class PubMiniComparator implements Comparator<PubMini> {
    @Override
    public int compare(PubMini pubMini, PubMini t1) {
        return new TimeSlotComparator(true, TimeSlotComparator.START)
                .compare(pubMini.timeSlot, t1.timeSlot);
    }
}
