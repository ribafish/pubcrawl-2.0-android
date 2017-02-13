package com.ws1617.iosl.pubcrawl20.Current;

import java.util.Comparator;

/**
 * Created by Gasper Kojek on 13. 02. 2017.
 * Github: https://github.com/ribafish/
 */

public class EventMiniComparator implements Comparator<EventMini> {
    public static final int DATE = 1;
    public static final int ID = 2;
    public static final int NAME = 3;

    private boolean ascending;
    private int type;

    public EventMiniComparator() {
        this.ascending = true;
        this.type = DATE;
    }

    /**
     * Used for comparing Events, for sorting and such
     * @param ascending bool ascending, used for setting the direction of rder
     * @param type  int type, used for setting on what key the events are compared
     */
    public EventMiniComparator(boolean ascending, int type) {
        this.ascending = ascending;
        this.type = type;
    }

    @Override
    public int compare(EventMini event, EventMini t1) {
        int result = 0;

        switch (type) {
            case DATE:
                result = event.getDate().compareTo(t1.getDate());
                if (result == 0) result = new EventMiniComparator(true, ID).compare(event,t1);
                break;
            case ID:
                long l = event.getEventId() - t1.getEventId();
                if (l > Integer.MAX_VALUE) l = Integer.MAX_VALUE;
                else if (l < Integer.MIN_VALUE) l = Integer.MIN_VALUE;
                result = (int) l;
                break;
            case NAME:
                result = event.getName().compareTo(t1.getName());
                if (result == 0) result = new EventMiniComparator(true, ID).compare(event,t1);
                break;
            default:
                result = event.getDate().compareTo(t1.getDate());
                break;
        }


        if (!ascending) result = -result;
        return result;
    }
}
