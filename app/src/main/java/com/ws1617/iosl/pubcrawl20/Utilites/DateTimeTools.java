package com.ws1617.iosl.pubcrawl20.Utilites;

import java.util.Calendar;

/**
 * Created by Haneen on 12/01/2017.
 */

public class DateTimeTools {

    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return day + "." + month + "." +year;
    }
}
