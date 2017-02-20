package com.ws1617.iosl.pubcrawl20.DataModels;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Gasper Kojek on 3. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class TimeSlot {
    private long pubId;
    private Date startTime;
    private Date endTime;

    public TimeSlot(long pubId, Date startTime, Date endTime) {
        this.pubId = pubId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getPubId() {
        if (pubId == -1) Log.e("TimeSlot", "Invalid pubId");
        return pubId;
    }

    public void setPubId(long pubId) {
        this.pubId = pubId;
    }

    public Date getStartTime() {
        return startTime;
    }

    SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm");

    public String getStartTimeAsString(){
       return localDateFormat.format(startTime);
    }

    public String getEndTimeAsString(){
        return localDateFormat.format(endTime);
    }
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public static TimeSlot getCombinedTimeSlot(ArrayList<TimeSlot> slots) {
        TimeSlot timeSlot = slots.get(0);
        timeSlot.setPubId(-1);
        for (TimeSlot ts : slots) {
            if (ts.startTime.before(timeSlot.startTime)) timeSlot.setStartTime(ts.startTime);
            if (ts.endTime.after(timeSlot.endTime)) timeSlot.setEndTime(ts.endTime);
        }
        return timeSlot;
    }

    public boolean isDateIncluded(Date date) {
        return date.after(startTime) && date.before(endTime);
    }
}
