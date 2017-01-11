package com.ws1617.iosl.pubcrawl20.DataModels;

import java.util.Comparator;

/**
 * Created by Gasper Kojek on 3. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class EventComparator implements Comparator<Event> {
    public static final int DATE = 1;
    public static final int ID = 2;
    public static final int NAME = 3;

    private boolean ascending;
    private int type;

    public EventComparator() {
        this.ascending = true;
        this.type = DATE;
    }

    /**
     * Used for comparing Events, for sorting and such
     * @param ascending bool ascending, used for setting the direction of rder
     * @param type  int type, used for setting on what key the events are compared
     */
    public EventComparator(boolean ascending, int type) {
        this.ascending = ascending;
        this.type = type;
    }

    @Override
    public int compare(Event event, Event t1) {
        int result = 0;

        switch (type) {
            case DATE:
                result = event.getDate().compareTo(t1.getDate());
                break;
            case ID:
                long l = event.getId() - t1.getId();
                if (l > Integer.MAX_VALUE) l = Integer.MAX_VALUE;
                else if (l < Integer.MIN_VALUE) l = Integer.MIN_VALUE;
                result = (int) l;
                break;
            case NAME:
                result = event.getEventName().compareTo(t1.getEventName());
                break;
            default:
                result = event.getDate().compareTo(t1.getDate());
                break;
        }


        if (!ascending) result = -result;
        return result;
    }
}
