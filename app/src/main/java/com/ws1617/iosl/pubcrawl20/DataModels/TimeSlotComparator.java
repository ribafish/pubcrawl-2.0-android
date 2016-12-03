package com.ws1617.iosl.pubcrawl20.DataModels;

import java.util.Comparator;

/**
 * Created by Gasper Kojek on 3. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class TimeSlotComparator implements Comparator<TimeSlot> {

    public static final int START = 1;
    public static final int END = 2;
    public static final int ID = 3;

    private boolean ascending;
    private int type;

    public TimeSlotComparator() {
        this.ascending = true;
        this.type = START;
    }

    /**
     * Used for comparing Events, for sorting and such
     * @param ascending bool ascending, used for setting the direction of rder
     * @param type  int type, used for setting on what key the events are compared
     */
    public TimeSlotComparator(boolean ascending, int type) {
        this.ascending = ascending;
        this.type = type;
    }

    @Override
    public int compare(TimeSlot timeSlot, TimeSlot t1) {
        int result = 0;

        switch (type) {
            case START:
                result = timeSlot.getStartTime().compareTo(t1.getStartTime());
                break;
            case END:
                result = timeSlot.getEndTime().compareTo(t1.getEndTime());
                break;
            case ID:
                long l = timeSlot.getPubId() - t1.getPubId();
                if (l > Integer.MAX_VALUE) l = Integer.MAX_VALUE;
                else if (l < Integer.MIN_VALUE) l = Integer.MIN_VALUE;
                result = (int) l;
                break;
            default:
                result = timeSlot.getStartTime().compareTo(t1.getStartTime());
                break;
        }

        if (!ascending) result = -result;
        return result;
    }

}
